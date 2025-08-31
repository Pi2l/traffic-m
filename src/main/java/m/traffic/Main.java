package m.traffic;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.engine.SimulationEngine;
import m.traffic.core.model.TrafficModel;
import m.traffic.core.model.factory.ModelFactory;
import m.traffic.util.ConfigParser;

public class Main {
  public static void main( String[] args ) {
    SimulationConfig config = ConfigParser.getSimulationConfig( args );
    TrafficModel model = ModelFactory.createModel( config.getModelType() );
    model.initialise(config);

    SimulationEngine engine = new SimulationEngine( model );
    engine.run();
  }
}
