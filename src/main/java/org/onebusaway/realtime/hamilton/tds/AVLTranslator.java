package org.onebusaway.realtime.hamilton.tds;

import static org.apache.commons.math.util.FastMath.atan2;
import static org.apache.commons.math.util.FastMath.cos;
import static org.apache.commons.math.util.FastMath.sin;
import static org.apache.commons.math.util.FastMath.sqrt;
import static org.apache.commons.math.util.FastMath.toRadians;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.hamilton.model.AVLRecord;
import org.onebusaway.realtime.hamilton.model.TripInfo;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AVLTranslator {
  private static final Logger _log = LoggerFactory.getLogger(AVLTranslator.class);
  private static final Pattern RUN_ROUTE = Pattern.compile("(\\d+)([AIO])?");
  private static final Pattern ROUTE_ID = Pattern.compile("(.*)_(\\d+)([AIOC]*)?");

  private TransitDataService _tds;
  private CoordinateBounds _agencyBounds;
  private BlockCalendarService _blockCalendarService;  
  private BlockLocationService _blockLocationService;
  private TripBeanService _tripBeanService;
  private TripStopTimesBeanService _tripStopTimesBeanService;
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
  
  public VehicleRecord translate(AVLRecord record) {
    VehicleRecord v = new VehicleRecord();
    String tripStart = record.getLogonTrip();
    List<TripInfo> tripInfos = getPotentialTrips(tripStart, record.getLogonRoute(), null, record);
    if (tripInfos == null || tripInfos.isEmpty()) {
      _log.debug("no trips for record=" + record);
      return null;
    }
   
    TripInfo tripInfo = tripInfos.get(0);
    if (tripInfos.size() > 1)
      _log.error("multiple trips for record=" + record.getLogonRoute() + ":" + record.getLogonTrip());
    BlockInstance block = tripInfo.getBlockInstance();
    BlockLocation location = tripInfo.getBlockLocation();
    
    v.setVehicleId("" + record.getId());
    if (location != null) {
      v.setDelay((int)location.getScheduleDeviation());
      v.setBearing((int)location.getLastKnownOrientation());
      v.setTime(new Timestamp(location.getTime()));
    } else {
      v.setTime(new Timestamp(System.currentTimeMillis()));
    }
    
    v.setLat(record.getLat());
    v.setLon(record.getLon());
    
    if (location != null && location.getClosestStop() != null) {
      String stopId = location.getClosestStop().getStopTime().getStop().getId().toString();
      v.setStopId(stopId);
      _log.debug("stopId=" + v.getStopId());
    } else {
      v.setStopId(tripInfo.getClosestStopId());
    }
    
    if (location != null) {
      AgencyAndId tripId = location.getActiveTrip().getTrip().getId();
      TripBean tripBean = _tripBeanService.getTripForId(tripId);

      v.setRouteId(tripBean.getRoute().getShortName());
      v.setTripId(tripId.toString());
    } else {
      TripEntry trip = block.getBlock().getTrips().get(0).getTrip();
      String routeId = trip.getRoute().getId().toString();
      v.setRouteId(routeId);
      v.setTripId(trip.getId().toString());
    }
    return v;
  }

  List<TripInfo> getPotentialTrips(String tripStart, String fuzzyRunRoute, String serviceDate, AVLRecord avlRecord) {
    _log.debug("fuzzyRunRoute=" + fuzzyRunRoute);
    List<TripInfo> potentials = new ArrayList<TripInfo>();
    if (tripStart == null || tripStart.contains("--")) return potentials;
    long tripStartSeconds = getTripStartSeconds(tripStart);
    long queryTime = getStartOfDayInMillis(serviceDate) + tripStartSeconds * 1000;
    for (AgencyWithCoverageBean agency : _tds.getAgenciesWithCoverage())  {
      TripInfo tripInfo = new TripInfo();
      String agencyId = agency.getAgency().getId();

      List<BlockInstance> instances = _blockCalendarService.getActiveBlocksForAgencyInTimeRange(
          agencyId, queryTime, queryTime);
      //_log.debug("instances=" + instances);
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
            /*
             * frequence based matching -- IN PROGRESS
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
                double distanceAway = distance(avlRecord.getLon(), avlRecord.getLat(), 
                    st.getStop().getStopLon(), st.getStop().getStopLat());
                if (distanceAway < minDistanceAway) {
                  minDistanceAway = distanceAway;
                  stopId = st.getStop().getId().toString();
                }
              }
              tripInfo.setClosestStopId(stopId);
              
              
              BlockLocation location = new BlockLocation();
              location.setActiveTrip(block.getBlock().getTrips().get(0));
              
              _log.debug("MATCH-FREQ!!" + avlRecord.getLogonRoute() + ":" + blockDeparture
                  + ":" + tripStart + ":" + trip.getId() + ":" + ",r=" 
                  + routeId + "(" + routeName + ")" + " block=" + block 
                  + ":" + trip.getDirectionId() + " (" + direction + ")");
              
              potentials.add(tripInfo);
            }              
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
              potentials.add(tripInfo);
            }
          }
        }
        
      }
    }
    return potentials;
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
    }
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

  public static final double RADIUS_OF_EARTH_IN_KM = 6371.01;
  public static final double distance(double lat1, double lon1, double lat2,
      double lon2) {
    return distance(lat1, lon1, lat2, lon2, RADIUS_OF_EARTH_IN_KM * 1000);
  }
  public static final double distance(double lat1, double lon1, double lat2,
      double lon2, double radius) {

    // http://en.wikipedia.org/wiki/Great-circle_distance
    lat1 = toRadians(lat1); // Theta-s
    lon1 = toRadians(lon1); // Lambda-s
    lat2 = toRadians(lat2); // Theta-f
    lon2 = toRadians(lon2); // Lambda-f

    double deltaLon = lon2 - lon1;

    double y = sqrt(p2(cos(lat2) * sin(deltaLon))
        + p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
    double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);

    return radius * atan2(y, x);
  }

  private static final double p2(double a) {
    return a * a;
  }

}
