package org.onebusaway.realtime.hamilton.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeLibrary;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeMutableProvider;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeProviderImpl;
import org.onebusaway.realtime.hamilton.model.AVLRecord;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.realtime.hamilton.sql.ResultSetMapper;
import org.onebusaway.realtime.hamilton.tds.AVLTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ServletContextAware;

import com.google.inject.Inject;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class HamiltonToGtfsRealtimeService implements ServletContextAware {
  public static final String DB_URL = "url";
  public static final String QUERY_STRING = "select ID as Id, BusID as busId, RptTime as reportTime, "
      + "LatDD as lat, LonDD as lon, LogonRoute as logonRoute, LogonTrip as logonTrip, "
      + "BusNum as busNumber, RptDate as reportDate from tblbuses;";
  
  private final String TRIP_UPDATE_PREFIX = "trip_update_";
  private final String VEHICLE_POSITION_PREFIX = "vehicle_position_";
  private static final Logger _log = LoggerFactory.getLogger(HamiltonToGtfsRealtimeService.class);
  private AVLTranslator _avlTranslator = null;
  private GtfsRealtimeMutableProvider _gtfsRealtimeProvider;
  private ScheduledExecutorService _refreshExecutor;
  private ScheduledExecutorService _delayExecutor;
  private String _url = null;
  private int _refreshInterval = 60;
  public void setRefreshInterval(int interval) {
   _refreshInterval = interval; 
  }
  public void setAVLTranslator(AVLTranslator translator) {
    _avlTranslator = translator;
  }
  
  public void setGtfsRealtimeProvider(GtfsRealtimeMutableProvider p) {
    _gtfsRealtimeProvider = p;
  }
  
  public void setConnectionUrl(String jdbcConnectionString) {
    _url = jdbcConnectionString;
  }
  
  @PostConstruct
  public void start() throws Exception {
    _log.info("starting GTFS-realtime service");
    if (_gtfsRealtimeProvider != null) {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      _refreshExecutor = Executors.newSingleThreadScheduledExecutor();
      _refreshExecutor.scheduleAtFixedRate(new RefreshTransitData(), 0,
          _refreshInterval, TimeUnit.SECONDS);
      
      _delayExecutor = Executors.newSingleThreadScheduledExecutor();
      _delayExecutor.scheduleAtFixedRate(new DelayThread(), _refreshInterval, _refreshInterval/4, TimeUnit.SECONDS);
    } else {
      _log.error("Testing mode.  RefreshInterval ignored!");

      new Thread(new RefreshTransitData()).run();
      
    }
  }

  // TODO replace with guice SpringIntegration
  // now moved into app-context; only here for testing (TODO move to testing)
  private void springSetup() {
    if (this._avlTranslator == null || this._avlTranslator.getTransitDataService() == null) {
      _log.warn("Starting spring manually");
      String[] files = {"org/onebusaway/realtime/hamilton/application-context-webapp.xml","data-sources.xml"};
      ApplicationContext appContext = new ClassPathXmlApplicationContext( files );
      AVLTranslator avl = appContext.getBean(AVLTranslator.class);
      this.setAVLTranslator(avl);
      GtfsRealtimeMutableProvider provider = appContext.getBean(GtfsRealtimeProviderImpl.class);
      this.setGtfsRealtimeProvider(provider);
      _log.info("Spring configured with gtfsRealtimeProvider=" + this._gtfsRealtimeProvider);
    }

  }
  
  @PreDestroy
  public void stop() {
    _log.info("stopping GTFS-realtime service");
    if (_refreshExecutor != null) {
      _refreshExecutor.shutdownNow();
    }
  }
  
  // package private for unit tests
  Map getConnectionProperties() {
    HashMap<String, String> properties = new HashMap<String, String>();
    properties.put(DB_URL, _url);
    return properties;
  }
  
  // package private for unit tests
  Connection getConnection(Map<String, String> properties) throws Exception {
    
    return DriverManager.getConnection(properties.get(DB_URL));
  }
  
  List<AVLRecord> getAVLRecords(Connection connection) throws Exception {
    ResultSet rs = null;
    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    rs = statement.executeQuery(QUERY_STRING);
    ResultSetMapper mapper = new ResultSetMapper();
    return mapper.map(rs);
  }
  
  List<VehicleRecord> getBlockRecords(List<AVLRecord> input) {
    List<VehicleRecord> output = new ArrayList<VehicleRecord>();
    for (AVLRecord record: input) {
      output.add(_avlTranslator.translate(record));
    }
    return output;
  }
  
  void writeGtfsRealtimeOutput(List<VehicleRecord> records) {
    FeedMessage tripUpdates = buildTripUpdates(records);
    if (_gtfsRealtimeProvider != null) {
      _gtfsRealtimeProvider.setTripUpdates(tripUpdates);
      // update metrics
      _gtfsRealtimeProvider.setLastUpdateTimestamp(System.currentTimeMillis());
    }
    FeedMessage vehicleUpdates = buildVehiclePositions(records);
    if (_gtfsRealtimeProvider != null)
      _gtfsRealtimeProvider.setVehiclePositions(vehicleUpdates);
    
  }
  
  public void writeGtfsRealtimeOutput() throws Exception {
    Connection conn = null;
    try {
      conn = getConnection(getConnectionProperties());
      List<VehicleRecord> records = getBlockRecords(getAVLRecords(conn));
      writeGtfsRealtimeOutput(records);
      _log.info("found " + records.size() + " updates");
    } catch (Exception any) {
      _log.error("exception writing GTFS data:", any);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception any) {
        // bury
      }
    }
  }
  
  private FeedMessage buildTripUpdates(List<VehicleRecord> records) {
    FeedMessage.Builder tripUpdates = GtfsRealtimeLibrary.createFeedMessageBuilder();
    ArrayList<StopTimeUpdate> stopTimeUpdateSet = new ArrayList<StopTimeUpdate>();
    
    for (VehicleRecord record : records) {
      if (record == null) continue;
      String vehicleId = record.getVehicleId();
      int delay = record.getDelay();
      double lat = record.getLat();
      double lon = record.getLon();
      int speed = record.getSpeed();
      int bearing = record.getBearing();
      int seq = record.getSeq();
      Timestamp time = record.getTime();
      String stopId = record.getStopId();
      String routeId = record.getRouteId();
      String tripId = record.getTripId();
      
      /**
       * StopTime Event
       */
      StopTimeEvent.Builder arrival = StopTimeEvent.newBuilder();
      arrival.setDelay(delay);
      arrival.setUncertainty(30);
      
      /**
       * StopTime Update
       */
      StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
      if(stopId == null){
        continue;
      }
      stopTimeUpdate.setStopSequence(seq);
      stopTimeUpdate.setStopId(stopId);
      stopTimeUpdate.setArrival(arrival);
      // Google requested adding departure delays for Google Transit (Issue #7).
      // Since we don't have explicit departure delay info from OrbCAD,
      // at the suggestion of Google we will just use arrival delay as a substitute
      stopTimeUpdate.setDeparture(arrival);  
      
      stopTimeUpdateSet.add(stopTimeUpdate.build());


      /**
       * Trip Descriptor
       */
      TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
      tripDescriptor.setTripId(tripId);
      tripDescriptor.setRouteId(routeId);
      /**
       * Vehicle Descriptor
       */
      VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
      if(vehicleId!=null && !vehicleId.isEmpty()) {
        vehicleDescriptor.setId(vehicleId);
      }
      
      TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
      tripUpdate.addAllStopTimeUpdate(stopTimeUpdateSet);
      stopTimeUpdateSet.clear();
      tripUpdate.setTrip(tripDescriptor);
      if(vehicleId!=null && !vehicleId.isEmpty()) {
        tripUpdate.setVehicle(vehicleDescriptor);
      }
      
      FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
      tripUpdateEntity.setId(TRIP_UPDATE_PREFIX+tripId);
      tripUpdateEntity.setTripUpdate(tripUpdate);
      tripUpdates.addEntity(tripUpdateEntity);

    }
    return tripUpdates.build();
  }

  private FeedMessage buildVehiclePositions(List<VehicleRecord> records) {
    FeedMessage.Builder vehiclePositions = GtfsRealtimeLibrary.createFeedMessageBuilder();
    
    HashSet<String> vehicleIdSet = new HashSet<String>();
    
    for (VehicleRecord record : records) {
      if (record == null) continue;
      String vehicleId = record.getVehicleId();
      int delay = record.getDelay();
      double lat = record.getLat();
      double lon = record.getLon();
      int speed = record.getSpeed();
      int bearing = record.getBearing();
      int seq = record.getSeq();
      Timestamp time = record.getTime();
      String stopId = record.getStopId();
      String routeId = record.getRouteId();
      String tripId = record.getTripId();
      if(!vehicleIdSet.contains(vehicleId)){
        vehicleIdSet.add(vehicleId);
      } else {
        continue;
      }

      /**
       * Trip Descriptor
       */
      TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
      tripDescriptor.setTripId(tripId);

      /**
       * Vehicle Descriptor
       */
      VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
      vehicleDescriptor.setId(vehicleId);

      /**
       * To construct our VehiclePosition, we create a position for the vehicle.
       * We add the position to a VehiclePosition builder, along with the trip
       * and vehicle descriptors.
       */
      Position.Builder position = Position.newBuilder();
      position.setLatitude((float) lat);
      position.setLongitude((float) lon);
      position.setSpeed((float) speed);
      position.setBearing((float) bearing);

      VehiclePosition.Builder vehiclePosition = VehiclePosition.newBuilder();
      vehiclePosition.setPosition(position);
      vehiclePosition.setTrip(tripDescriptor);
      vehiclePosition.setVehicle(vehicleDescriptor);

      FeedEntity.Builder vehiclePositionEntity = FeedEntity.newBuilder();
      vehiclePositionEntity.setId(VEHICLE_POSITION_PREFIX+vehicleId);
      vehiclePositionEntity.setVehicle(vehiclePosition);

      vehiclePositions.addEntity(vehiclePositionEntity);
    }

    return vehiclePositions.build();

  }

  public void setServletContext(ServletContext context) {
    if (context != null) {
      String url = context.getInitParameter("hamilton.jdbc");
      if (url != null) {
        _log.info("init with connection info: " + url);
        this.setConnectionUrl(url);
      } else {
        _log.warn("missing expected init param: hamilton.jdbc");
      }
    }
  }

  
  private class RefreshTransitData implements Runnable {
    public void run() {
      try {
        _log.info("refreshing vehicles");
        writeGtfsRealtimeOutput();
      } catch (Exception ex) {
        _log.error("Failed to refresh TransitData: " + ex.getMessage());
        _log.error(ex.toString(), ex);
      }
    }
  }
	private class DelayThread implements Runnable {
		  public void run() {
		    long hangTime = (System.currentTimeMillis() - _gtfsRealtimeProvider.getLastUpdateTimestamp()) / 1000;
		    if (hangTime> ((_refreshInterval * 2) - (_refreshInterval / 2))) {
	  	    // if we've reached here, the connection to the database has hung
		      // we assume a service-based configuration and simply exit
		      // TODO adjust network/driver timeouts instead!
		      _log.error("Connection hung with delay of " + hangTime + ".  Exiting!");
		      System.exit(1);
		    } else {
		      _log.info("hangTime:" + hangTime);
		    }
		  }
		}

}
