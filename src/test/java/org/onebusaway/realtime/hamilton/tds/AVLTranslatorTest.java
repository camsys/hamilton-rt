package org.onebusaway.realtime.hamilton.tds;

import static org.junit.Assert.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.hamilton.model.DBAVLRecord;
import org.onebusaway.realtime.hamilton.model.TripInfo;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AVLTranslatorTest {

  private static final Logger _log = LoggerFactory.getLogger(AVLTranslatorTest.class);
  private static SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testInference() throws Exception {
    //BasicConfigurator.configure();
    _log.error("starting spring");
    System.out.println("starting spring");
    String[] files = {"org/onebusaway/realtime/hamilton/application-context-webapp.xml","data-sources.xml"};
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(files);
    _log.error("spring load complete");
    
    AVLTranslator t = new AVLTranslator();
    TransitDataService tds = context.getBean(TransitDataService.class);
    BlockCalendarService bcs = context.getBean(BlockCalendarService.class);
    BlockLocationService bls = context.getBean(BlockLocationService.class);
    TripBeanService tbs = context.getBean(TripBeanService.class);
    TripStopTimesBeanService tstbs = context.getBean(TripStopTimesBeanService.class);
    assertNotNull(tds);
    t.setTransitDataService(tds);
    t.setBlockCalendarService(bcs);
    t.setBlockLocationService(bls);
    t.setTripBeanService(tbs);
    t.setTripStopTimesBeanService(tstbs);
    
    
    
    DBAVLRecord r = new DBAVLRecord();
    r.setId(1);
    r.setBusId(2);
    r.setLogonTrip("0715");
    r.setLogonRoute("15A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 08:20").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 07:40").getTime()));
    long currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_GOBUS_15I_SV01_01", t.getTripStartingAt(r, currentTime));

    r.setId(1);
    r.setBusId(2);
    r.setLogonTrip("0715");
    r.setLogonRoute("15A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 08:20").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 07:40").getTime()));
    currentTime = r.getReportDate().getTime() + (11 * 60 * 1000); //record is 11m old
    assertEquals(null, t.getTripStartingAt(r, currentTime));

    
    
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0740");
    r.setLogonRoute("15A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 08:20").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 07:40").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_GOBUS_15I_SV01_02", t.getTripStartingAt(r, currentTime));

    
    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0900");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 09:00").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 09:00").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));


    // frequency route
    r.setId(5);
    r.setBusId(6);
    r.setLogonTrip("0900");
    r.setLogonRoute("52");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 09:00").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 09:00").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52C_SV01_01", t.getTripStartingAt(r, currentTime));
    
    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0920");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 09:21").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 09:20").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));

    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0940");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 09:41").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 09:40").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));
    

    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("1000");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 10:02").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 10:00").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));

    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("1500");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 14:02").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 15:00").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));

    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("1459");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 15:00").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 14:59").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals(null, t.getTripStartingAt(r, currentTime));
    
    
    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("1515");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 15:17").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 15:15").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));
    
    
    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("1530");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-05-23 15:32").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-05-23 15:30").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV01_01", t.getTripStartingAt(r, currentTime));

    
    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0750");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-12-27 07:55").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-12-27 07:50").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV05_02", t.getTripStartingAt(r, currentTime));

    // frequency route
    r.setId(3);
    r.setBusId(4);
    r.setLogonTrip("0825");
    r.setLogonRoute("52A");
    r.setReportDate(new Date(_sdf.parse("2014-12-27 08:26").getTime()));
    r.setLogonTripDate(new Date(_sdf.parse("2014-12-27 08:25").getTime()));
    currentTime = r.getReportDate().getTime() + (30 * 1000); //record is 30s old
    assertEquals("PAV_PAV_52AC_SV05_02", t.getTripStartingAt(r, currentTime));

    
//    matchRoute(t, "15", null, t.getPotentialTrips("0715", "15A", "2014-05-23", null));
//    matchRoute(t, "8", null, t.getPotentialTrips("0850", "8", "2014-05-23", null));
//    matchRoute(t, "12", null, t.getPotentialTrips("0820", "12", "2014-05-23", null));
//    matchRoute(t, "13", null, translator.getPotentialTrips("0840", "13A", "2014-05-23", null));
//    matchRoute(t, "12", null, translator.getPotentialTrips("0823", "12A", "2014-05-23", null));
//    matchRoute(t, "5", null, translator.getPotentialTrips("0850", "5A", "2014-05-23", null));
//    matchRoute(t, "13", null, translator.getPotentialTrips("0840", "13", "2014-05-23", null));
//    matchRoute(t, "7", null, t.getPotentialTrips("0830", "7", "2014-05-23", null));
//    matchRoute(t, "9", null, t.getPotentialTrips("0850", "9", "2014-05-23", null));
//    matchRoute(t, "4", null, t.getPotentialTrips("1620", "4A", "2014-05-23", null));
//    matchRoute(t, "9", null, t.getPotentialTrips("0845", "9A", "2014-05-23", null));
//    matchRoute(t, "3", null, translator.getPotentialTrips("0850", "3", "2014-05-23", null));
//    matchRoute(t, "17", null, translator.getPotentialTrips("0825", "17", "2014-05-23", null));
//    matchRoute(t, "8", null, t.getPotentialTrips("0840", "8A", "2014-05-23", null));
//    matchRoute(t, "1", null, t.getPotentialTrips("0815", "1", "2014-05-23", null));
//    matchRoute(t, "16", null, t.getPotentialTrips("0835", "16A", "2014-05-23", null));
//    matchRoute(t, "57", null, translator.getPotentialTrips("0725", "57", "2014-05-23", null));
//    matchRoute(t, "7", null, t.getPotentialTrips("0838", "7A", "2014-05-23", null));
//    matchRoute(t, "2", null, t.getPotentialTrips("0820", "2A", "2014-05-23", null));
//    matchRoute(t, "26", null, t.getPotentialTrips("0852", "26A", "2014-05-23", null));
//    matchRoute(t, "2", null, t.getPotentialTrips("0820", "2", "2014-05-23", null));
//    matchRoute(t, "2", null, t.getPotentialTrips("0800", "2A", "2014-05-23", null));
//    matchRoute(t, "3", null, translator.getPotentialTrips("0740", "3", "2014-05-23", null));
//    matchRoute(t, "58", null, translator.getPotentialTrips("0840", "58", "2014-05-23", null));
//    matchRoute(t, "15", null, translator.getPotentialTrips("0820", "15A", "2014-05-23", null));
//    matchRoute(t, "57", null, translator.getPotentialTrips("0805", "57", "2014-05-23", null));
//    matchRoute(t, "6", null, translator.getPotentialTrips("0823", "6A", "2014-05-23", null));
//    matchRoute(t, "16", null, t.getPotentialTrips("0830", "16", "2014-05-23", null));
//    matchRoute(t, "21", null, translator.getPotentialTrips("0740", "21D", "2014-05-23", null)); // EXCEPTION
//    matchRoute(t, "21", null, translator.getPotentialTrips("0845", "21B", "2014-05-23", null)); // EXCEPTION
//    matchRoute(t, "52", null, translator.getPotentialTrips("0820", "52", "2014-05-22", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0074", "52", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0730", "52", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0840", "52A", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0820", "52A", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0805", "52A", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0805", "52", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0700", "52A", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0715", "52", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0751", "52", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0730", "52A", "2014-05-23", null));
//    matchRoute(t, "52", null, translator.getPotentialTrips("0725", "52", "2014-05-23", null));
//    matchRoute(t, "51", null, translator.getPotentialTrips("0830", "51", "2014-05-23", null));
//    matchRoute(t, "51", null, translator.getPotentialTrips("0700", "51", "2014-05-23", null));
//    matchRoute(t, "1", null, t.getPotentialTrips("0820", "1A", "2014-05-23", null));
//    matchRoute(t, "26", null, t.getPotentialTrips("0822", "26A", "2014-05-23", null));
//    matchRoute(t, "15", null, translator.getPotentialTrips("0820", "15", "2014-05-23", null));
//    matchRoute(t, "6", null, t.getPotentialTrips("0820", "6", "2014-05-23", null));
//    matchRoute(t, "8", null, translator.getPotentialTrips("1945", "4A", "2014-05-23", null));
    context.close();
  }
  
  private void matchRoute(AVLTranslator t, String expectedRoute, String expectedTripId, List<TripInfo> tripInfos) {
    assertNotNull(tripInfos);
    if (tripInfos.isEmpty()) {
      _log.error("no match for " + expectedRoute);
//      return;
    }
    assertTrue(tripInfos.size() >= 1);

    BlockLocation location = tripInfos.get(0).getBlockLocation();
    AgencyAndId tripId = location.getActiveTrip().getTrip().getId();
    String routeId = location.getActiveTrip().getTrip().getRoute().getId().toString();
    TripBean trip = t.getTripBeanService().getTripForId(tripId);
    String routeName = t.lookupRouteName(trip.getRoute().getShortName());
    _log.error("route found=" + routeName
        + " route expected=" + expectedRoute + " of " + tripInfos.size());
    //assertEquals(expectedRoute, routeName);
    assertTrue(routeName.contains(expectedRoute));
    if (expectedTripId != null) {
      assertEquals(expectedTripId, tripId);
    } else {
      _log.error("actualTripId=" + tripId + " for route " + expectedRoute);
    }
  }
}
