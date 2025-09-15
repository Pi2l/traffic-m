package m.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.cli.ParseException;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.engine.SimulationEngine;
import m.traffic.core.model.TrafficModel;
import m.traffic.core.model.factory.ModelFactory;
import m.traffic.util.ConfigParser;

public class Main {
  public static void main( String[] args ) throws ParseException {
    int poolSize = Runtime.getRuntime().availableProcessors();
    ExecutorService pool = Executors.newFixedThreadPool(poolSize);

    List<SimulationConfig> configs = ConfigParser.getSimulationConfig( args );
    List<Future<?>> futures = new ArrayList<>(configs.size());
    for (SimulationConfig config : configs) {
      futures.add(pool.submit(() -> runWithSimulationConfig( config )));
    }
    pool.shutdown();

    completeAll(futures);
  }

  private static void runWithSimulationConfig( SimulationConfig config ) {
    TrafficModel model = ModelFactory.createModel( config.getModelType() );
    model.initialise(config);

    SimulationEngine engine = new SimulationEngine( model );
    engine.run();
  }

  private static void completeAll( List<Future<?>> futures ) {
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (Exception e) {
        System.err.println("Error occurred while processing a simulation configuration: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
