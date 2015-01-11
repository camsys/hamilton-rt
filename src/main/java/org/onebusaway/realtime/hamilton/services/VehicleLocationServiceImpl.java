package org.onebusaway.realtime.hamilton.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.onebusaway.collections.Min;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.hamilton.model.TripMatch;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointsLibrary;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class VehicleLocationServiceImpl implements VehicleLocationService {

  private static Logger _log = LoggerFactory.getLogger(VehicleLocationServiceImpl.class);
  private static final int MAX_SEARCH_SIZE = 5;
  private static final long MAX_CACHE_AGE = 90; // seconds
  private Cache<String, VehicleLocation> _cache;
  private ShapePointsLibrary _shapePointsLibrary = new ShapePointsLibrary();
  private ShapePointHelper _shapePointHelper;
  @Autowired
  public void setShapePointHelper(ShapePointHelper sps) {
    _shapePointHelper = sps;
  }
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
  
  public Double computeDistanceAlongBlock(String vehicleId, BlockTripEntry trip, Double lat, Double lon) {
    
    VehicleLocation vl = _cache.getIfPresent(vehicleId);
//    Integer bestStopSeq = null;
//    if (vl == null || !vl.getTripId().equals(trip.getTrip().getId().toString())) {
//      /*
//       * we don't have a previous observation or the trip has changed!
//       * simply find the closest across the entire trip
//       * keep in mind this answer might be wrong!
//       */
//      bestStopSeq = findClosestStopSequence(trip, 0, trip.getStopTimes().size(), lat,lon);
//      if (bestStopSeq == null) {
//        return null;
//      }
//    } else {
//      // here we have a previous observation, so limit our search around that observation
//      bestStopSeq = findClosestStopSequence(trip, vl.getStopSequence(), MAX_SEARCH_SIZE, lat, lon);
//      if (bestStopSeq == null) {
//        // if we didn't find a stop, we should remove our previous observation as well
//        // our next observation will search the entire trip, no need to re-search
//        _cache.invalidate(vehicleId);
//        return null;
//      }
//    }
//    
//    BlockStopTimeEntry blockStopTimeEntry = trip.getStopTimes().get(bestStopSeq);
//    StopEntry stop = blockStopTimeEntry.getStopTime().getStop();
//    tripMatch.setStopId(stop.getId().toString());
    // update the cache with our latest result
    Double distanceAlongShape = getDistanceAlongShape(trip, lat, lon);
    
    if (distanceAlongShape == null) { 
      _cache.invalidate(vehicleId);
      return null;
    }
    
//    double dab = trip.getStopTimes().get(bestStopSeq).getDistanceAlongBlock();
    double dab = trip.getDistanceAlongBlock() + distanceAlongShape;
    _log.error("dab for trip " + trip.getTrip().getId() + " is " + dab + "(base block=" + trip.getDistanceAlongBlock() + ")");
    
    _cache.put(vehicleId, new VehicleLocation(trip.getTrip().getId().toString(), 0, dab, 0.0));
    return dab;

}

  private Double getDistanceAlongShape(BlockTripEntry trip, Double lat,
      Double lon) {
    AgencyAndId shapeId = trip.getTrip().getShapeId();
    if (shapeId == null) {
      _log.error("no shape for trip=" + trip);
      return null;
    }
    ShapePoints shapePoints = _shapePointHelper.getShapePointsForShapeId(shapeId);
    UTMProjection projection = UTMLibrary.getProjectionForPoint(lat, lon);
    List<XYPoint> projectedShapePoints = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);
    double[] shapePointsDistTraveled = shapePoints.getDistTraveled();
    List<List<PointAndIndex>> possibleAssignments = computePotentialAssignments(
        projection, projectedShapePoints, shapePointsDistTraveled, lat ,lon);
    _log.error("" + possibleAssignments.size() + " possible assignments for trip=" + trip.getTrip().getId());
//    Min<Assignment> bestAssignments = new Min<Assignment>();
//    constructAssignments(possibleAssignments, bestAssignments, lat, lon);
    if (!possibleAssignments.isEmpty()) {
      List<PointAndIndex> list = possibleAssignments.get(0);
      PointAndIndex p = list.get(0);
//      _log.error("best assignment=" + list + " against projection=" + projection.forward(new CoordinatePoint(lat, lon)));
      _log.error("best assignment of " + lat + ", " + lon + " is " + projection.reverse(p.point) + " with DAS=" + p.distanceAlongShape);
      _log.error("assignments=" + list);
      return possibleAssignments.get(0).get(0).distanceAlongShape;
    }
    return null;
  }

//  private void constructAssignments(
//      List<List<PointAndIndex>> possibleAssignments,
//      Min<Assignment> bestAssignments, Double lat, Double lon) {
//    for (List<PointAndIndex> current : possibleAssignments) {
//      double score = 0;
//      for (PointAndIndex p : current) {
//        score += p.distanceFromTarget;
//      }
//    }
//    
//  }
  
  
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

  // this came from DistanceAlongShapeLibrary -- thanks Brian
  private List<List<PointAndIndex>> computePotentialAssignments(
      UTMProjection projection, List<XYPoint> projectedShapePoints,
      double[] shapePointDistance, double lat , double lon) {

    List<List<PointAndIndex>> possibleAssignments = new ArrayList<List<PointAndIndex>>();

    XYPoint stopPoint = projection.forward(new CoordinatePoint(lat, lon)); //stop.getStopLocation()

    List<PointAndIndex> assignments = _shapePointsLibrary.computePotentialAssignments(
        projectedShapePoints, shapePointDistance, stopPoint, 0,
        projectedShapePoints.size());

    possibleAssignments.add(assignments);
    return possibleAssignments;
  }

  
    private static class VehicleLocation {
    private String tripId;
    private int stopSequence;
    private double distanceAlongBlock;
    private double lastDistanceAway;
    public VehicleLocation(String tripId, int stopSequence, double distanceAlongBlock, double lastDistanceAway) {
      this.tripId = tripId;
      this.stopSequence = stopSequence;
      this.distanceAlongBlock = distanceAlongBlock;
      this.lastDistanceAway = lastDistanceAway;
    }
    public String getTripId() {
      return tripId;
    }
    public int getStopSequence() {
      return stopSequence;
    }
    public double getDistanceAlongBlock() {
      return distanceAlongBlock;
    }
    public double getLastDistanceAway() {
      return lastDistanceAway;
    }
  }

    private static class Assignment implements Comparable<Assignment> {
      private final List<PointAndIndex> assigment;
      private final double score;

      public Assignment(List<PointAndIndex> assignment, double score) {
        this.assigment = assignment;
        this.score = score;
      }

      public int compareTo(Assignment o) {
        return Double.compare(score, o.score);
      }
    }

}
