package org.onebusaway.realtime.hamilton.services;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onebusaway.realtime.hamilton.model.AVLRecord;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;
import org.onebusaway.realtime.hamilton.tds.AVLTranslator;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HamiltonToGtfsRealtimeServiceTest {

  private static final Logger _log = LoggerFactory.getLogger(HamiltonToGtfsRealtimeServiceTest.class);
  
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
  public void noop() {
    
  }
  
  //@Test
  public void testConnection() throws Exception {
    HamiltonToGtfsRealtimeService service = new HamiltonToGtfsRealtimeService();
    Connection connection = service.getConnection(service.getConnectionProperties());
    assertNotNull(connection);
    connection.close();
  }
  
  //@Test
  public void testData() throws Exception {
    HamiltonToGtfsRealtimeService service = new HamiltonToGtfsRealtimeService();
    Connection connection = service.getConnection(service.getConnectionProperties());
    List<AVLRecord> records = service.getAVLRecords(connection);
    assertNotNull(records);
    System.out.println("records=" + records);
    connection.close();
  }
  
  //@Test
  public void testInference() throws Exception {
    BasicConfigurator.configure();
    _log.error("starting spring");
    System.out.println("starting spring");
    String[] files = {"org/onebusaway/realtime/hamilton/application-context-webapp.xml","data-sources.xml"};
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(files);
    _log.error("spring load complete");
    AVLRecord record = getTestAVLRecord();
    AVLTranslator translator = new AVLTranslatorTest();
    TransitDataService tds = context.getBean(TransitDataService.class);
    BlockCalendarService bcs = context.getBean(BlockCalendarService.class);
    BlockLocationService bls = context.getBean(BlockLocationService.class);
    TripBeanService tbs = context.getBean(TripBeanService.class);
    TripStopTimesBeanService tstbs = context.getBean(TripStopTimesBeanService.class);

    assertNotNull(tds);
    translator.setTransitDataService(tds);
    translator.setBlockCalendarService(bcs);
    translator.setBlockLocationService(bls);
    translator.setTripBeanService(tbs);
    translator.setTripStopTimesBeanService(tstbs);

    
    VehicleRecord br = translator.translate(record);
    assertNotNull(br);
    assertEquals("87", br.getVehicleId());
    assertEquals("CAMB_1861", br.getStopId());
    assertEquals("GOBUS_15O_SV01_01", br.getTripId());
    assertEquals("15", br.getRouteId());
    assertEquals(-37.799281, br.getLat(), 0.000001);
    assertEquals(175.259078, br.getLon(), 0.000001);
    assertEquals(0, br.getBearing());
    assertEquals(0, br.getDelay());
    
    
    HamiltonToGtfsRealtimeService service = new HamiltonToGtfsRealtimeService();
    service.setAVLTranslator(translator);
    
    List<VehicleRecord> records = new ArrayList<VehicleRecord>();
    records.add(br);
    
    service.writeGtfsRealtimeOutput(records);
    
    context.close();
  }

  private AVLRecord getTestAVLRecord() {
  //AVL(87, 3087, 1970-01-01, -37.799281, 175.259078, 15A, 0715, null, 2014-05-19)
  AVLRecord record = new AVLRecord();
  record.setId(87);
  record.setBusId(87);
  record.setReportTime(new Date(System.currentTimeMillis()));
  record.setLat(-37.799281);
  record.setLon(175.259078);
  record.setLogonRoute("15A");
  record.setLogonTrip("0715");
  record.setBusNumber(null);
  record.setReportDate(new Date(System.currentTimeMillis()));
  return record; 
  }

  public class AVLTranslatorTest extends AVLTranslator {
    
  }
  
}
