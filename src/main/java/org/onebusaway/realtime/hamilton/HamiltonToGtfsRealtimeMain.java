package org.onebusaway.realtime.hamilton;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.log4j.BasicConfigurator;
import org.onebusaway.cli.CommandLineInterfaceLibrary;
import org.onebusaway.gtfs_realtime.exporter.TripUpdatesFileWriter;
import org.onebusaway.gtfs_realtime.exporter.TripUpdatesServlet;
import org.onebusaway.gtfs_realtime.exporter.VehiclePositionsFileWriter;
import org.onebusaway.gtfs_realtime.exporter.VehiclePositionsServlet;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusaway.realtime.hamilton.services.HamiltonToGtfsRealtimeService;
import org.onebusaway.realtime.hamilton.tds.AVLTranslator;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public class HamiltonToGtfsRealtimeMain {
  private static final String ARG_TRIP_UPDATES_PATH = "tripUpdatesPath";

  private static final String ARG_TRIP_UPDATES_URL = "tripUpdatesUrl";

  private static final String ARG_VEHICLE_POSITIONS_PATH = "vehiclePositionsPath";

  private static final String ARG_VEHICLE_POSITIONS_URL = "vehiclePositionsUrl";
  
  private static final String ARG_REFRESH_INTERVAL = "refreshInterval";

  
  private HamiltonToGtfsRealtimeService _provider;
  
  private LifecycleService _lifecycleService;
  
  private static final Logger _log = LoggerFactory.getLogger(HamiltonToGtfsRealtimeMain.class);
  
  public static void main(String[] args) throws Exception {
    HamiltonToGtfsRealtimeMain m = new HamiltonToGtfsRealtimeMain();
    m.run(args);
  }
  
  @Inject
  public void setProvider(HamiltonToGtfsRealtimeService provider) {
    _provider = provider;
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  private AVLTranslator _avl = null;
  @Inject
  public void setAVLTranslator(AVLTranslator avl) {
    _avl = avl;
  }

  public void run(String[] args) throws Exception {
    BasicConfigurator.configure();
    if (args.length == 0 || CommandLineInterfaceLibrary.wantsHelp(args)) {
      printUsage();
      System.exit(-1);
    }

    Options options = new Options();
    buildOptions(options);
    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    HamiltonToGtfsRealtimeModule.addModuleAndDependencies(modules);
    
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    if (cli.hasOption(ARG_TRIP_UPDATES_URL)) {
      URL url = new URL(cli.getOptionValue(ARG_TRIP_UPDATES_URL));
      TripUpdatesServlet servlet = injector.getInstance(TripUpdatesServlet.class);
      _log.error("tripUpdates=" + url);
      servlet.setUrl(url);
    }
    if (cli.hasOption(ARG_TRIP_UPDATES_PATH)) {
      File path = new File(cli.getOptionValue(ARG_TRIP_UPDATES_PATH));
      TripUpdatesFileWriter writer = injector.getInstance(TripUpdatesFileWriter.class);
      writer.setPath(path);
    }

    if (cli.hasOption(ARG_VEHICLE_POSITIONS_URL)) {
      URL url = new URL(cli.getOptionValue(ARG_VEHICLE_POSITIONS_URL));
      VehiclePositionsServlet servlet = injector.getInstance(VehiclePositionsServlet.class);
      servlet.setUrl(url);
    }
    if (cli.hasOption(ARG_VEHICLE_POSITIONS_PATH)) {
      File path = new File(cli.getOptionValue(ARG_VEHICLE_POSITIONS_PATH));
      VehiclePositionsFileWriter writer = injector.getInstance(VehiclePositionsFileWriter.class);
      writer.setPath(path);
    }
    
    if (cli.hasOption(ARG_REFRESH_INTERVAL)) {
      _provider.setRefreshInterval(Integer.parseInt(cli.getOptionValue(ARG_REFRESH_INTERVAL)));
    }

    if (_avl == null) {
	throw new RuntimeException("missing avl dependency");
    }
    _provider.setAVLTranslator(_avl);

    _lifecycleService.start();
  }

  private void printUsage() {
    CommandLineInterfaceLibrary.printUsage(getClass());
  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_TRIP_UPDATES_PATH, true, "trip updates path");
    options.addOption(ARG_TRIP_UPDATES_URL, true, "trip updates url");
    options.addOption(ARG_VEHICLE_POSITIONS_PATH, true,
        "vehicle positions path");
    options.addOption(ARG_VEHICLE_POSITIONS_URL, true, "vehicle positions url");
    options.addOption(ARG_REFRESH_INTERVAL, true, "realtime data refresh interval");

  }

}
