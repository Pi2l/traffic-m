package m.traffic.core.engine;

import m.traffic.core.model.TrafficModel;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.state.TrafficSnapshot;

public class SimulationEngine {

  protected final TrafficModel model;
  protected boolean running = false;

  public SimulationEngine(TrafficModel model) {
    this.model = model;
  }

  public void run() {
    while(running) {
      model.nextStep();
      
      sleepSimulation(model.getConfig());
      TrafficSnapshot snapshot = model.getSnapshot();

      // process the snapshot, e.g., update UI or log data
    }
  }
  
  private void sleepSimulation(SimulationConfig config) {
    try {
      Thread.sleep(config.stepDuration());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      running = false;
    }
  }

  public void stop() {
    running = false;
  }

}
