package org.onebusaway.realtime.hamilton.tds;

import static org.apache.commons.math.util.FastMath.atan2;
import static org.apache.commons.math.util.FastMath.cos;
import static org.apache.commons.math.util.FastMath.sin;
import static org.apache.commons.math.util.FastMath.sqrt;
import static org.apache.commons.math.util.FastMath.toRadians;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.hamilton.model.AVLRecord;
import org.onebusaway.realtime.hamilton.model.DBAVLRecord;
import org.onebusaway.realtime.hamilton.model.PositionReport;
import org.onebusaway.realtime.hamilton.model.StopTimeInfo;
import org.onebusaway.realtime.hamilton.model.TripInfo;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
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
  private static SimpleDateFormat _fullDate= new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private static SimpleDateFormat _date= new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat _time= new SimpleDateFormat("HHmm");

  private TransitDataService _tds;
  private CoordinateBounds _agencyBounds;
  private BlockCalendarService _blockCalendarService;  
  private BlockLocationService _blockLocationService;
  private TripBeanService _tripBeanService;
  private TripStopTimesBeanService _tripStopTimesBeanService;
  private BlockGeospatialService _geospatialService;
  private Map<String, VehicleUpdate> updates = new HashMap<String, VehicleUpdate>();
  
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
  public void setBlockLocationService(BlockLocationService bls) {
    _blockLocationService = bls;
  }
  @Autowired
  public void setTripBeanService(TripBeanService tbs) {
   _tripBeanService = tbs;
  }
  protected TripBeanService getTripBeanService() {
    return _tripBeanService;
  }
  @Autowired
  public void setTripStopTimesBeanService(TripStopTimesBeanService tstbs) {
    _tripStopTimesBeanService = tstbs;
  }
  
  public VehicleRecord translate(DBAVLRecord record) {
    VehicleRecord v = new VehicleRecord();
//    String tripStart = record.getLogonTrip();
//    List<TripInfo> tripInfos = getPotentialTrips(tripStart, record.getLogonRoute(), null, record);
//    if (tripInfos == null || tripInfos.isEmpty()) {
//      _log.info("no trips for record=" + record);
//      return null;
//    }
    
    
    
   String tripId = getTripStartingAt(record, System.currentTimeMillis());
   if (tripId == null) {
     return null;
   }
//    BlockInstance block = tripInfo.getBlockInstance();
//    BlockLocation location = tripInfo.getBlockLocation();
//    v.setStopTimeInfos(tripInfo.getStopTimeInfos());
//    v.setFrequency(tripInfo.isFrequency());
    v.setVehicleId("" + record.getId());
//    if (location != null) {
//      v.setDelay((int)location.getScheduleDeviation());
//      v.setBearing((int)location.getLastKnownOrientation());
//      v.setTime(new Timestamp(location.getTime()));
//    } else {
//      v.setTime(new Timestamp(System.currentTimeMillis()));
//    }
    v.setTime(new Timestamp(System.currentTimeMillis()));
    
    v.setLat(record.getLat());
    v.setLon(record.getLon());
    
//    if (location != null && location.getClosestStop() != null) {
//      String stopId = location.getClosestStop().getStopTime().getStop().getId().toString();
//      v.setStopId(stopId);
//      _log.debug("stopId=" + v.getStopId());
//    } else {
//      v.setStopId(tripInfo.getClosestStopId());
//    }
    
    v.setTripId(tripId);
    v.setStopId(findClosestStopId(record, tripId));
//    if (location != null) {
//      AgencyAndId tripId = location.getActiveTrip().getTrip().getId();
//      TripBean tripBean = _tripBeanService.getTripForId(tripId);
//
//      v.setRouteId(tripBean.getRoute().getShortName());
//      v.setTripId(tripId.toString());
//    } else {
//      TripEntry trip = block.getBlock().getTrips().get(0).getTrip();
//      String routeId = trip.getRoute().getId().toString();
//      v.setRouteId(routeId);
//      v.setTripId(trip.getId().toString());
//    }
    return v;
  }

  private String findClosestStopId(DBAVLRecord record, String tripId) {
//    return "PAV_1034";
    return null;
  }
  
  String getTripStartingAt(DBAVLRecord record, long currentTime) {
    return getTripStartingAt(currentTime, ""+record.getId(), record.getReportDate(), record.getLogonTripDate(), record.getLogonTrip(), record.getLogonRoute());
  }
  
  String getTripStartingAt(PositionReport pr, long currentTime) {
    return getTripStartingAt(currentTime, pr.getCellId(), toReportDate(currentTime), toLogonDate(currentTime), pr.getOperatorId(), pr.getCellId());
  }
  
  private Date toLogonDate(long currentTime) {
    _fullDate.format(new Date(currentTime));
    return null;
  }
  private Date toReportDate(long currentTime) {
    _date.format(new Date(currentTime));
    return null;
  }
  // package private for unit tests  
  String  getTripStartingAt(long currentTime, String id, Date reportDate, Date logonTripDate, String logonTrip, String logonRoute) {
    _log.debug("reportdate=" + reportDate + "; logonDate=" + logonTripDate);
    
    // test age of record
    if (reportDate == null || logonTripDate == null)
      return null;
    
    if (Math.abs(currentTime - reportDate.getTime()) > 10 * 60 * 1000) {
      _log.debug("record " + id + " too old at " + reportDate);
      return null;
    }
//    _log.info("record " + id + ":" + logonRoute + " is " 
//        + (currentTime - reportDate.getTime()) + "ms old:"
//        + reportDate);
    
    
    String tripStart = logonTrip;
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
//            _log.info("tripRouteId=" + tripRouteId);
            if (fuzzyRoute != null && fuzzyRoute.equals(tripRouteId)) {
//              _log.info("match for trip=" + trip.getTrip().getId().toString());
              return trip.getTrip().getId().toString();
//              return null;
            }
          }
        } 
       
      }
      // we didn't match a schedule trip, try frequency
      for (BlockInstance block : instances) {
        FrequencyEntry frequency = block.getState().getFrequency();
        if (frequency != null) {
          String tripId = block.getBlock().getTrips().get(0).getTrip().getId().toString();
//          _log.info("freq start:" + frequency.getStartTime() + "?=" + logonStartTime);
          BlockTripEntry trip = block.getBlock().getTrips().get(0);
          String tripRouteId = getRouteNameFromTripId(trip.getTrip().getId().toString());
//          _log.info("fuzzyRoute=" + fuzzyRoute);
//          String direction = getFuzzyDirection(logonRoute);
          String direction = null;
//          _log.info("logonRoute=" + logonRoute + ", tripRouteId=" + tripRouteId + "(" + trip.getTrip().getId().toString() + ")");
          if (logonRoute.equals(tripRouteId)) {
//            _log.info("logonRoute=" + logonRoute + ", tripRouteId=" + tripRouteId);
            if (direction == null || direction.equals(trip.getTrip().getDirectionId())) {
              // the TDS doesn't hand us the exact frequency interval, we need to search through the entries
              long frequencyStartTime = frequency.getStartTime();
              while (frequencyStartTime != logonStartTime) {
                if (frequencyStartTime > frequency.getEndTime()) {
                  break;
                }
//                frequencyStartTime += frequency.getHeadwaySecs(); // TODO the GTFS does not match realtime anymore
                  frequencyStartTime += 300;
              }
              if (frequencyStartTime == logonStartTime) {
                _log.info("match for trip=" + tripId);
                return tripId;
              }
            }
          }
        }

      }
      
      
    }// end agency
    
    return null;
  }

  List<TripInfo> getPotentialTrips(String tripStart, String fuzzyRunRoute, String serviceDate, DBAVLRecord avlRecord) {
    _log.info("fuzzyRunRoute=" + fuzzyRunRoute);
    List<TripInfo> potentials = new ArrayList<TripInfo>();
    if (tripStart == null || tripStart.contains("--")) return potentials;
    long tripStartSeconds = getTripStartSeconds(tripStart);
    // TODO switch this back?
    long queryTime = getStartOfDayInMillis(serviceDate) + tripStartSeconds * 1000;
//    long queryTime = System.currentTimeMillis();
//    long queryStartTime = System.currentTimeMillis() - 60 * 60 * 1000;
//    long queryEndTime = System.currentTimeMillis() + 60 * 60 * 1000;
    for (AgencyWithCoverageBean agency : _tds.getAgenciesWithCoverage())  {
      TripInfo tripInfo = new TripInfo();
      String agencyId = agency.getAgency().getId();

//      List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForAgencyInTimeRange(
//          agencyId, queryStartTime, queryEndTime);
//      _log.debug("instances[" + agencyId + "]=" + instances);
      List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForAgencyInTimeRange(
          agencyId, queryTime, queryTime);

      for (BlockInstance block : instances) {
        
        TripEntry trip = block.getBlock().getTrips().get(0).getTrip();
        String routeId = trip.getRoute().getId().toString();
        String routeName = getRouteNameFromRouteId(routeId);
        String direction = getFuzzyDirection(avlRecord.getLogonRoute());
        String avlRoute = getFuzzyRoute(fuzzyRunRoute);
        
        
        _log.debug("routeName=" + routeName + "=?" + avlRoute);
        if (routeName != null && avlRoute != null && routeName.matches(avlRoute)) {
          _log.debug("MATCH!" + routeName);

          // we are schedule based
          if (direction != null && !direction.equals(trip.getDirectionId())) {
            _log.debug("rejecting route " + fuzzyRunRoute + " because of direction " 
                + direction + "(" + trip.getDirectionId() + ")");
            continue;
          }
          
          if (block.getState().getFrequency() != null) {
//            getOldFrequencyTrips(potentials, tripInfo, avlRecord, block, serviceDate, tripStartSeconds);
            geFrequencyTrips(potentials, tripInfo, avlRecord, block, serviceDate, tripStartSeconds);
          } else {
            /*
             * schedule based matching
             */
            BlockLocation location = _blockLocationService.getScheduledLocationForBlockInstance(
                block, queryTime);
            if (location == null) {
              _log.warn("no location found for fuzzyRunRoute=" + fuzzyRunRoute);
              continue;
            }
            
            long blockDeparture = location.getClosestStop().getStopTime().getDepartureTime();
            _log.debug("location=" + location + ", blockDeparture=" + blockDeparture);
            // we trivially check to see if our block starts when the operator specified
            if (tripStartSeconds == blockDeparture) {
              _log.debug("MATCH!!" + avlRecord.getLogonRoute() + ":" + blockDeparture + ":" + fuzzyRunRoute + ":" + tripStart + " " + block + " direction=" + trip.getDirectionId());
              tripInfo.setBlockLocation(location);
              potentials.add(tripInfo); // TODO ENABLE
            }
          }
        }
        
      }
    }
    return potentials;
  }
  
  
  private void geFrequencyTrips(List<TripInfo> potentials, TripInfo tripInfo,
      DBAVLRecord avlRecord, BlockInstance block, String serviceDate,
      long tripStartSeconds) {
    
    // TODO prune report times older than 10 minutes
    
    TripEntry trip = block.getBlock().getTrips().get(0).getTrip();
    ScheduledBlockLocation closestStopLocation = findClosestStopSequence(block, trip, avlRecord);
    StopTimeInfo lastStu = null; 
    if (closestStopLocation == null || closestStopLocation.getActiveTrip() == null || closestStopLocation.getNextStop() == null) {
      _log.error("no close stops for trip=" + trip.getId().toString());
      return;
    }
    int closestStopSequence = closestStopLocation.getActiveTrip().getSequence();
    
    int stopSeq = closestStopLocation.getStopTimeIndex();
    
    int blockSeq = closestStopLocation.getNextStop().getBlockSequence();
    long blockStartTime = block.getState().getFrequency().getStartTime();
    long blockHeadway = block.getState().getFrequency().getHeadwaySecs();
    
    int nextStopTimeOffset = closestStopLocation.getNextStopTimeOffset();
    
    
    
    //long arrivalTimeInSeconds = this.getStartOfDayInMillis(null)/1000 + closestStopLocation.getScheduledTime();
    long arrivalTimeInSeconds = this.getStartOfDayInMillis(null)/1000 + blockStartTime + (blockSeq * blockHeadway) + nextStopTimeOffset;
    String stopId = closestStopLocation.getNextStop().getStopTime().getStop().getId().toString();
    
    arrivalTimeInSeconds = System.currentTimeMillis() / 1000; // TODO HACK
    
    

    if (Math.abs(arrivalTimeInSeconds * 1000 - System.currentTimeMillis()) > 35*60*1000) {
      // block is more than 35 mins out, ignore
      _log.info("discarting trip (nonsensical arrivalTime)=" + trip.getId().toString());
      return;
    }
    
    StopTimeInfo stu = new StopTimeInfo(stopSeq, arrivalTimeInSeconds, stopId);
    tripInfo.addStopTimeInfos(stu);
    lastStu = stu;
    
    _log.info("stop " + stopId + ", blockStart=" + hour(blockStartTime) + ", headway=" + blockHeadway 
        + ", blockSeq=" + blockSeq + ", nextStop=" + nextStopTimeOffset + ", stopSeq(trip)=" + closestStopSequence + ", stopSeq(stop)=" + stopSeq);
    
    _log.info("arrivalTime[" + avlRecord.getLogonTrip() + "]=" + lastStu.getArrivalTimeInSeconds() 
        + "(" + new java.util.Date(lastStu.getArrivalTimeInSeconds() * 1000) + ")");

    tripInfo.setBlockInstance(block);
    tripInfo.setFrequencyEntry(block.getState().getFrequency());

    tripInfo.setClosestStopId(stopId);
    tripInfo.setScheduleRelationship("UNSCHEDULED");
    tripInfo.setFrequency(true);
    BlockLocation location = new BlockLocation();
    location.setActiveTrip(block.getBlock().getTrips().get(0));

    
    potentials.add(tripInfo);

  }
  private String hour(long blockDeparture) {
    return "" + (blockDeparture/3600) + ":" + (blockDeparture/60)%60;
  }
  private ScheduledBlockLocation findClosestStopSequence(BlockInstance block,
      TripEntry trip, DBAVLRecord avlRecord) {
    double minDistanceAway = Double.POSITIVE_INFINITY;
    int count = 0;
    int stopSequence = 0;
    double minDistanceAlongTrip = 0;
    double distanceAlongTrip = 0;
    double bestDistanceAway = 0;
    String vehicleId = ""+avlRecord.getId();
    ScheduledBlockLocation lastLocation = null;
    
    if (updates.containsKey(vehicleId)) {
      VehicleUpdate vehicleUpdate = updates.get(vehicleId);
      if (vehicleUpdate.getTripId().equals(trip.getId().toString()))
        minDistanceAlongTrip = vehicleUpdate.getDistanceAlongTrip();
    }
    
    for (StopTimeEntry st : trip.getStopTimes()) {
      double distanceAway = SphericalGeometryLibrary.distance(avlRecord.getLat(), avlRecord.getLon(), 
          st.getStop().getStopLat(), st.getStop().getStopLon());
      if (distanceAway < minDistanceAway) {
        minDistanceAway = distanceAway;
        bestDistanceAway = distanceAway;
         ScheduledBlockLocation blockLocation = _geospatialService.getBestScheduledBlockLocationForLocation(block, 
            new CoordinatePoint(avlRecord.getLat(), avlRecord.getLon()), System.currentTimeMillis(), 0, block.getBlock().getTotalBlockDistance());
         distanceAlongTrip = blockLocation.getDistanceAlongBlock();
         // only allow this update if its progress along the trip
           stopSequence = count;
           lastLocation = blockLocation;
//        _log.debug("distanceAway=" + distanceAway + " for stop " + st.getStop().getId() + "(" + stopSequence + ")");
      }
      count++;
    }
    
    if (bestDistanceAway > 5000) return null;
    
    if (distanceAlongTrip > minDistanceAlongTrip) {
      // ensure forward progress
      VehicleUpdate vi = new VehicleUpdate(vehicleId, trip.getId().toString(), distanceAlongTrip);
      if (lastLocation != null) {
        _log.debug("scheduled stop=" + lastLocation.getNextStop() + " has distanceAway=" + bestDistanceAway);
      }
      _log.debug(vi.toString());
      updates.put(vehicleId, vi);
      
      return lastLocation;
    }
    // nothing found

    return null;
  }
  private void getOldFrequencyTrips(List<TripInfo> potentials,
      TripInfo tripInfo, DBAVLRecord avlRecord, BlockInstance block,
      String serviceDate, long tripStartSeconds) {
    TripEntry trip = block.getBlock().getTrips().get(0).getTrip();
    /*
     * frequency based matching -- IN PROGRESS
     */
    long blockDeparture = block.getState().getFrequency().getStartTime();
    
    while (blockDeparture < tripStartSeconds) {
      blockDeparture += block.getState().getFrequency().getHeadwaySecs();
    }
    _log.debug("" + tripStartSeconds + " ?= " + blockDeparture);
    if (tripStartSeconds == blockDeparture) {
      // we are frequency based
      tripInfo.setBlockInstance(block);
      tripInfo.setFrequencyEntry(block.getState().getFrequency());
      String stopId = null;
      double minDistanceAway = Double.POSITIVE_INFINITY;
      for (StopTimeEntry st : trip.getStopTimes()) {
        double distanceAway = SphericalGeometryLibrary.distance(avlRecord.getLon(), avlRecord.getLat(), 
            st.getStop().getStopLon(), st.getStop().getStopLat());
        if (distanceAway < minDistanceAway) {
          minDistanceAway = distanceAway;
          stopId = st.getStop().getId().toString();
        }
      }
      tripInfo.setClosestStopId(stopId);
      
      
      BlockLocation location = new BlockLocation();
      location.setActiveTrip(block.getBlock().getTrips().get(0));
      
//      _log.debug("MATCH-FREQ!!" + avlRecord.getLogonRoute() + ":" + blockDeparture
//          + ":" + tripStart + ":" + trip.getId() + ":" + ",r=" 
//          + routeId + "(" + routeName + ")" + " block=" + block 
//          + ":" + trip.getDirectionId() + " (" + direction + ")");
      
      potentials.add(tripInfo);
    }              
    
    
  }
  
  private String getFuzzyDirection(String s) {
    if (s == null) return null;
    if (s.endsWith("O"))
      return "0";
    if (s.endsWith("I"))
      return "1";
    if (s.equals("52"))
      return "0";
    if (s.equals("52A"))
      return "1";
    return null;
  }
  public String lookupRouteName(String shortName) {
    if ("O ACW".equals(shortName))
      return "52A";
    if ("O CW".equals(shortName))
      return "52";
    return shortName;
  }

  private long getStartOfDayInMillis(String serviceDate) {
    Calendar cal = Calendar.getInstance();
    
    if (serviceDate != null) {
      int year = Integer.parseInt(serviceDate.substring(0, 4));
      int month = Integer.parseInt(serviceDate.substring(5,7)) - 1; // month is 0 based
      int date = Integer.parseInt(serviceDate.substring(8,10));
      //_log.debug("serviceDate=" + year + ", " + month + ", " + date);
      cal.set(year, month, date); 
          
    }
    
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.AM_PM, Calendar.AM);
    cal.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
    long startOfDay = cal.getTimeInMillis();
    //_log.debug("startOfDay=" + startOfDay + "(" + cal.getTime() + ")");
    return startOfDay;
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

  
  private CoordinateBounds getAgencyBounds() {
    if (_agencyBounds == null) {
      CoordinateBounds bounds = new CoordinateBounds();
      for (AgencyWithCoverageBean agency : _tds.getAgenciesWithCoverage()) {
        String agencyId = agency.getAgency().getId();
        for (CoordinateBounds aBounds : _tds.getAgencyIdsWithCoverageArea().get(
            agencyId)) {
          bounds.addBounds(aBounds);
        }
      }
      _agencyBounds = bounds;
    }
    return _agencyBounds;
  }

  private static class VehicleUpdate {
    private String _vehicleId;
    private String _tripId;
    private double _distanceAlongTrip;
    public VehicleUpdate(String vehicleId, String tripId, double distanceAlongTrip) {
      _vehicleId = vehicleId;
      _tripId = tripId;
      _distanceAlongTrip = distanceAlongTrip;
    }
    
    public double getDistanceAlongTrip() {
      return _distanceAlongTrip;
    }
    
    public String getTripId() {
      return _tripId;
    }
    
    public String toString() {
      return _vehicleId+":"+_tripId + ":" + _distanceAlongTrip;
    }
  }

  public VehicleRecord translate(PositionReport pr) {
    long currentTime = System.currentTimeMillis();
    String tripId = this.getTripStartingAt(pr, currentTime);
    if (tripId == null) {
      return null;
    }
    VehicleRecord vr = new VehicleRecord();
    vr.setLat(pr.getLat());
    vr.setLon(pr.getLon());
    return null;
  }
  
}
