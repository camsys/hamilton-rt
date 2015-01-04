package org.onebusaway.realtime.hamilton.tds;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.realtime.hamilton.model.DBAVLRecord;
import org.onebusaway.realtime.hamilton.model.Logon;
import org.onebusaway.realtime.hamilton.model.PositionReport;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AVLTranslator {
  private static final Logger _log = LoggerFactory.getLogger(AVLTranslator.class);
  private static final Pattern RUN_ROUTE = Pattern.compile("(\\d+)([AIO])?");
  private static final Pattern ROUTE_ID = Pattern.compile("(.*)_(\\d+)([AIOC]*)?");
  private static final Pattern TRIP_ID = Pattern.compile("([^_]*)_([^_]*)_(\\d+)([AIOC]*)?_([^_]*)_([^_]*)");

  private TransitDataService _tds;
  private BlockCalendarService _blockCalendarService;  
  private TripBeanService _tripBeanService;
  private BlockGeospatialService _geospatialService;
  
  @Autowired
  public void setBlockGeospatialService(BlockGeospatialService bgs) {
    _geospatialService = bgs;
  }
  @Autowired
  public void setTransitDataService(TransitDataService tds) {
    _tds = tds;
  }
  public TransitDataService getTransitDataService() {
    return _tds;
  }
  @Autowired
  public void setBlockCalendarService(BlockCalendarService bcs) {
    _blockCalendarService = bcs;
  }
  @Autowired
  public void setTripBeanService(TripBeanService tbs) {
   _tripBeanService = tbs;
  }
  protected TripBeanService getTripBeanService() {
    return _tripBeanService;
  }
  
  public VehicleRecord translate(DBAVLRecord record) {
   VehicleRecord v = new VehicleRecord();
    
   TripMatch stuff = getTripStartingAt(record, System.currentTimeMillis());
   if (stuff == null) {
     return null;
   }
   String tripId = stuff.getTripId();
   boolean isFrequency = stuff.isFrequency();
   if (tripId == null) {
     return null;
   }
   if (stuff.getStopId() == null) {
     String stopId = getClosestStopId(record, System.currentTimeMillis(), tripId);
     v.setStopId(stopId);
   } else {
     v.setStopId(stuff.getStopId());
   }
   v.setFrequency(isFrequency);
   
   String routeId = tripId.split("_")[2];  
    v.setVehicleId("" + record.getBusId());
    
    v.setTime(new Timestamp(record.getReportDate().getTime()));
    
    v.setLat(record.getLat());
    v.setLon(record.getLon());
    
    v.setTripId(tripId);
    v.setBlockId(stuff.getBlockId());
    v.setRouteId(routeId);
    return v;
  }
  
  // TODO this fakes out the closes stop by returned the last stop on the trip
  private String getClosestStopId(DBAVLRecord record, long currentTimeMillis, String tripId) {
    return getClosestStopId(tripId);
  }
  
//TODO this fakes out the closes stop by returned the last stop on the trip
  private String getClosestStopId(String tripId) {
    TripDetailsQueryBean query = new TripDetailsQueryBean();
    query.setTripId(tripId);
    ListBean<TripDetailsBean> tripDetails = _tds.getTripDetails(query);
    if (tripDetails != null && !tripDetails.getList().isEmpty())
    {
      // TODO this isn't actually implemented!
      List<TripStopTimeBean> stopTimes = tripDetails.getList().get(0).getSchedule().getStopTimes();
      int lastStop = stopTimes.size()-1;
      return stopTimes.get(lastStop).getStop().getId();
    }
    return null;
  }
  
  TripMatch getTripStartingAt(DBAVLRecord record, long currentTime) {
    return getTripStartingAt(currentTime, ""+record.getBusId(), record.getReportDate(), record.getLogonTripDate(), record.getLogonTrip(), record.getLogonRoute(), record.getLat(), record.getLon());
  }
  
  // package private for unit tests  
  TripMatch getTripStartingAt(long currentTime, String id, Date reportDate, Date logonTripDate, String logonTrip, String logonRoute, Double lat, Double lon) {
    
    List<TripMatch> stuffs = new ArrayList<TripMatch>();
    
    // test age of record
    if (reportDate == null || logonTripDate == null)
      return null;
    
    if (reportDate.getDay() != logonTripDate.getDay()) {
      _log.info("discarding old record for id=" + id);
      return null;
    }
    
    if (Math.abs(currentTime - reportDate.getTime()) > 10 * 60 * 1000) {
      _log.debug("record " + id + " too old at " + reportDate);
      return null;
    }
    
    for (AgencyWithCoverageBean agency : _tds.getAgenciesWithCoverage()) {
      long queryTime = logonTripDate.getTime();
      String agencyId = agency.getAgency().getId();
      List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForAgencyInTimeRange(
          agencyId, queryTime, queryTime+(60*1000));
      
      long logonStartTime = getTripStartSeconds(logonTrip);
      String fuzzyRoute = getFuzzyRoute(logonRoute);
      
      for (BlockInstance block : instances) {
        for (BlockTripEntry trip : block.getBlock().getTrips()) {
          int departureTimeForIndex = trip.getDepartureTimeForIndex(0);
          long tripStartTime = departureTimeForIndex;
          if (tripStartTime == logonStartTime) {

            String tripRouteId = getRouteNameFromRouteId(trip.getTrip().getRoute().getId().toString());
            if (fuzzyRoute != null && fuzzyRoute.equals(tripRouteId)) {
                String logonDirection = this.getDirectionFromLogonRoute(logonRoute);
                String tripDirection = this.getDirectionFromTripId(trip.getTrip().getId().toString());
                if (logonDirection == null || logonDirection.equals(tripDirection)) {
                  stuffs.add(new TripMatch(trip.getTrip().getId().toString(), false, null, logonRoute, block.getBlock().getBlock().getId().toString()));
                } else {
                  _log.error(id + " rejected as logonDirectin=" + logonDirection + " and tripDirection=" + tripDirection);
                }
                
            } 
          }
        } 
       
      }

      // we didn't match a schedule trip, try frequency
      for (BlockInstance block : instances) {
        FrequencyEntry frequency = block.getState().getFrequency();
        if (frequency != null) {
          String tripId = block.getBlock().getTrips().get(0).getTrip().getId().toString();
          BlockTripEntry trip = block.getBlock().getTrips().get(0);
          String tripRouteId = getRouteNameFromTripId(trip.getTrip().getId().toString());
          String direction = null;
          if (logonRoute.equals(tripRouteId)) {
            if (direction == null || direction.equals(trip.getTrip().getDirectionId())) {
              // the TDS doesn't hand us the exact frequency interval, we need to search through the entries
              long frequencyStartTime = frequency.getStartTime();
              while (frequencyStartTime != logonStartTime) {
                if (frequencyStartTime > frequency.getEndTime()) {
                  break;
                }
//                frequencyStartTime += frequency.getHeadwaySecs(); // TODO the GTFS does not match realtime anymore
                  frequencyStartTime += 60;
              }
              if (frequencyStartTime == logonStartTime) {
                String stopId = findNextStop(block, trip, lat, lon);
                stuffs.add(new TripMatch(tripId, true, stopId, logonRoute, block.getBlock().getBlock().getId().toString()));
              }
            }
          }
        }
      }
      
    }// end agency
    
    if (stuffs.size() > 1) {
      _log.error("vehicle " + id + " qualifed for " + stuffs.size() + " trips:" + stuffs);
      return stuffs.get(0);
    }
    if (stuffs.isEmpty())
      return null;
    
    _log.info("vehicle " + id + " trip:" + stuffs + "(" + lat + "," + lon + ")");
    return stuffs.get(0);
  }


private String getDirectionFromLogonRoute(String logonRoute) {
  if (logonRoute.endsWith("A"))
    return "O";
  return "I";
  }
private String getDirectionFromTripId(String tripId) {
  if (tripId == null) return null;
  String direction = tripId.split("_")[2].replaceAll("\\d", "");
  return direction;
  }

private String findNextStop(BlockInstance block, BlockTripEntry trip, double lat, double lon) {
  ScheduledBlockLocation closestStopLocation = findClosestStopSequence(block, trip.getTrip(), lat, lon);
  if (closestStopLocation == null || closestStopLocation.getActiveTrip() == null || closestStopLocation.getNextStop() == null) {
    _log.error("no close stops for trip=" + trip.toString());
    return null;
  }
  
  String stopId = closestStopLocation.getNextStop().getStopTime().getStop().getId().toString();
  return stopId;
}
  
  
  private ScheduledBlockLocation findClosestStopSequence(BlockInstance block,
      TripEntry trip, double lat, double lon) {
    double minDistanceAway = Double.POSITIVE_INFINITY;
    double bestDistanceAway = 0;
    ScheduledBlockLocation lastLocation = null;
    
    for (StopTimeEntry st : trip.getStopTimes()) {
      double distanceAway = SphericalGeometryLibrary.distance(lat, lon, 
          st.getStop().getStopLat(), st.getStop().getStopLon());
      if (distanceAway < minDistanceAway) {
        minDistanceAway = distanceAway;
        bestDistanceAway = distanceAway;
         ScheduledBlockLocation blockLocation = _geospatialService.getBestScheduledBlockLocationForLocation(block, 
            new CoordinatePoint(lat, lon), System.currentTimeMillis(), 0, block.getBlock().getTotalBlockDistance());
         // only allow this update if its progress along the trip
           lastLocation = blockLocation;
      }
    }
    
    if (bestDistanceAway > 1000) return null;
    
    if (lastLocation != null) {
      _log.debug("scheduled stop=" + lastLocation.getNextStop() + " has distanceAway=" + bestDistanceAway);
    }
    return lastLocation;
  }
  
  public String lookupRouteName(String shortName) {
    if ("O ACW".equals(shortName))
      return "52A";
    if ("O CW".equals(shortName))
      return "52";
    return shortName;
  }

  public long getTripStartSeconds(String s) {
    if (s == null || s.length() != 4) {
      _log.warn("malformed trip start=" + s);
      return 0;
    }
      
    return Integer.parseInt(s.substring(0, 2)) * 60 * 60 + Integer.parseInt(s.substring(2,4)) * 60;
  }
  
  public String getFuzzyRoute(String s) {
    if (s == null) return null;
    String fuzzy = null;
    Matcher runRouteMatcher = RUN_ROUTE.matcher(s);
    if (runRouteMatcher.matches()) {
      fuzzy = runRouteMatcher.group(1);
    }
    return fuzzy;
  }

  public String getRouteNameFromRouteId(String s) {
    if (s == null) return null;
    String fuzzy = null;
    Matcher runRouteMatcher = ROUTE_ID.matcher(s);
    if (runRouteMatcher.matches()) {
      fuzzy = runRouteMatcher.group(2);
//      if (runRouteMatcher.groupCount() > 2)
//      fuzzy += runRouteMatcher.group(3);
    }
    return fuzzy;
  }
  
  public String getRouteNameFromTripId(String s) {
    if (s == null) return null;
    String fuzzy = null;
    Matcher runRouteMatcher = TRIP_ID.matcher(s);
    if (runRouteMatcher.matches()) {
      fuzzy = runRouteMatcher.group(3);
      if (runRouteMatcher.groupCount() > 3)
      fuzzy += runRouteMatcher.group(4);
    }
    if ("52AC".equals(fuzzy))
      return "52A";
    if ("52C".equals(fuzzy))
      return "52";

    return fuzzy;
  }


  public VehicleRecord translate(PositionReport pr, Map<String, Logon> cache) {
    long currentTime = System.currentTimeMillis();
    VehicleRecord vr = new VehicleRecord();
    vr.setVehicleId(pr.getId());
    vr.setLat(pr.getLat());
    vr.setLon(pr.getLon());
    Date now = new Date(System.currentTimeMillis());
    Logon l = cache.get(pr.getId());
    if (l == null) return null;
    TripMatch stuff = this.getTripStartingAt(currentTime, pr.getId(), now, now, l.getTime(), l.getRoute(), pr.getLat(), pr.getLon());
    if (stuff == null) {
      _log.error("could not match " + pr.getId() + " to a trip");
    }
    vr.setTripId(stuff.getTripId());
    return vr;
  }

  private static class TripMatch {
    private String tripId;
    private boolean isFrequency;
    private String stopId;
    private String routeId;
    private String blockId;
    public TripMatch(String tripId, boolean isFrequency, String stopId, String routeId, String blockId) {
      this.tripId = tripId;
      this.isFrequency = isFrequency;
      this.stopId = stopId;
      this.routeId = routeId;
      this.blockId = blockId;
    }
    public String getTripId() {
      return tripId;
    }
    public boolean isFrequency() {
      return isFrequency;
    }
    public String getStopId() {
      return stopId;
    }
    public String getBlockId() {
      return blockId;
    }
    public String toString() {
      return "{" + tripId + "[" + routeId + "]" + "}";
    }
  }
}
