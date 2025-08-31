package m.traffic.core.engine;

import m.traffic.core.model.TrafficModel;
import m.traffic.stats.StatsCollector;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.state.TrafficSnapshot;

public class SimulationEngine {

  protected final TrafficModel model;
  protected final StatsCollector statsCollector;
  protected boolean running = false;

  public SimulationEngine(TrafficModel model) {
    this.model = model;
    this.statsCollector = new StatsCollector(model.getConfig());
  }

  public void run() {
    running = true;
    while(running) {
      model.nextStep();
      
      SimulationConfig modelConfig = model.getConfig();
      sleepSimulation(modelConfig);
      TrafficSnapshot snapshot = model.getSnapshot();

      // process the snapshot
      statsCollector.writeSnapshotToFile(snapshot);
      // statsCollector.addToStats(model.getSimulationStatistics(), snapshot);

      running = checkIfRunning(snapshot);
    }
  }
  
  private boolean checkIfRunning(TrafficSnapshot snapshot) {
    if (!running) {
      return false;
    }
    SimulationConfig config = model.getConfig();
    int configStepCount = config.getStepCount();
    running = configStepCount == SimulationConfig.INFINITE_STEP_COUNT 
                  || snapshot.getStepCount() <= configStepCount;
    return running;
  }

  private void sleepSimulation(SimulationConfig config) {
    try {
      Thread.sleep(config.getStepDuration());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      running = false;
    }
  }

  public void stop() {
    running = false;
  }

}
