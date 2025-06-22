package m.traffic.core.model;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;

public interface TrafficModel {

  void initialise(SimulationConfig config);
  SimulationConfig getConfig();

  void nextStep();

  TrafficSnapshot getSnapshot();

  SimulationStatistics getStatistics();

}
