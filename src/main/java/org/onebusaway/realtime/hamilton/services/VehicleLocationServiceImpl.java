package org.onebusaway.realtime.hamilton.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.realtime.hamilton.model.TripMatch;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class VehicleLocationServiceImpl implements VehicleLocationService {

  
  private static final int MAX_SEARCH_SIZE = 5;
  private static final long MAX_CACHE_AGE = 90; // seconds
  private Cache<String, VehicleLocation> _cache;
  
  /**
   * Service to keep track of last stop positions of each vehicle across a trip, 
   * so as to improve accuracy of distance along block calculations.
   * 
   * This algorithm could be trivially expanded to track across entire block, but
   * that would not allow for dispatch/run changes. 
   */
  public VehicleLocationServiceImpl() {
    _cache = CacheBuilder.newBuilder().expireAfterWrite(MAX_CACHE_AGE, TimeUnit.SECONDS).build();
  }
  
  public Double computeDistanceAlongBlock(TripMatch tripMatch, String vehicleId, BlockTripEntry trip, Double lat, Double lon) {
    
    VehicleLocation vl = _cache.getIfPresent(vehicleId);
    Integer bestStopSeq = null;
    if (vl == null || !vl.getTripId().equals(trip.getTrip().getId().toString())) {
      /*
       * we don't have a previous observation or the trip has changed!
       * simply find the closest across the entire trip
       * keep in mind this answer might be wrong!
       */
      bestStopSeq = findClosestStopSequence(trip, 0, trip.getStopTimes().size(), lat,lon);
      if (bestStopSeq == null) {
        return null;
      }
    } else {
      // here we have a previous observation, so limit our search around that observation
      bestStopSeq = findClosestStopSequence(trip, vl.getStopSequence(), MAX_SEARCH_SIZE, lat, lon);
      if (bestStopSeq == null) {
        // if we didn't find a stop, we should remove our previous observation as well
        // our next observation will search the entire trip, no need to re-search
        _cache.invalidate(vehicleId);
        return null;
      }
    }
    
    BlockStopTimeEntry blockStopTimeEntry = trip.getStopTimes().get(bestStopSeq);
    StopEntry stop = blockStopTimeEntry.getStopTime().getStop();
    tripMatch.setStopId(stop.getId().toString());
    // update the cache with our latest result
    _cache.put(vehicleId, new VehicleLocation(trip.getTrip().getId().toString(), bestStopSeq));
    return trip.getStopTimes().get(bestStopSeq).getDistanceAlongBlock();

}

  private Integer findClosestStopSequence(BlockTripEntry trip, int beginSequence, int searchSize, Double lat, Double lon) {
    int endSequence = Math.min(beginSequence + searchSize, trip.getStopTimes().size());
    Integer bestStopSeq = null;
    Double closestStop = Double.MAX_VALUE;
    for (int i = beginSequence; i < endSequence; i++) {
        BlockStopTimeEntry blockStopTimeEntry = trip.getStopTimes().get(i);
        StopEntry stop = blockStopTimeEntry.getStopTime().getStop();
        double distanceAway = SphericalGeometryLibrary.distance(lat, lon, 
            stop.getStopLat(), stop.getStopLon());
        if (Math.abs(distanceAway) < closestStop) {
          closestStop = Math.abs(distanceAway);
          bestStopSeq = i;
        }
    }
    return bestStopSeq;
  }

  private static class VehicleLocation {
    private String tripId;
    private int stopSequence;
    public VehicleLocation(String tripId, int stopSequence) {
      this.tripId = tripId;
      this.stopSequence = stopSequence;
    }
    public String getTripId() {
      return tripId;
    }
    public int getStopSequence() {
      return stopSequence;
    }
  }
  
}
