package org.onebusaway.realtime.hamilton;

import java.util.Set;

import org.hibernate.SessionFactory;
import org.onebusaway.guice.jsr250.JSR250Module;
import org.onebusaway.realtime.hamilton.services.HamiltonToGtfsRealtimeService;
import org.onebusaway.realtime.hamilton.tds.AVLTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * 
 * @author bdferris
 * @author sheldonabrown
 *
 */
public class HamiltonToGtfsRealtimeModule extends AbstractModule {

  private static final Logger _log = LoggerFactory.getLogger(HamiltonToGtfsRealtimeModule.class);
  
  public static void addModuleAndDependencies(Set<Module> modules) {
    modules.add(new HamiltonToGtfsRealtimeModule());
//    GtfsRealtimeExporterModule.addModuleAndDependencies(modules);
//    JSR250Module.addModuleAndDependencies(modules);
  }

  @Override
  protected void configure() {
      bind(HamiltonToGtfsRealtimeService.class);
//      String[] files = {"org/onebusaway/realtime/hamilton/application-context-webapp.xml","data-sources.xml"};
//      ApplicationContext appContext = new ClassPathXmlApplicationContext( files );
//      AVLTranslator avl = appContext.getBean(AVLTranslator.class); 
//      bind( BeanFactory.class ).toInstance( appContext );
//      bind( AVLTranslator.class)
//      bind( SessionFactory.class ).toProvider( SpringIntegration.fromSpring( SessionFactory.class, "sessionFactory" ) );
      
//      bind(DataSource.class)
//      .toProvider(fromSpring(DataSource.class, "dataSource"));
  }

}