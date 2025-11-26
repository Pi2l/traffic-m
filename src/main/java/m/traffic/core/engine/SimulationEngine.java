package m.traffic.core.engine;

import m.traffic.core.model.TrafficModel;
import m.traffic.stats.StatsCollector;
import java.util.logging.Logger;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.state.TrafficSnapshot;

public class SimulationEngine {

  private static final Logger logger = Logger.getLogger(SimulationEngine.class.getName());
  protected final TrafficModel model;
  protected final StatsCollector statsCollector;
  protected boolean running = false;

  public SimulationEngine(TrafficModel model) {
    this.model = model;
    this.statsCollector = model.getStatsCollector();
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
    logger.info("Simulaion %s completed.".formatted(model.getConfig().toString()));
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
    if (config.getStepDuration() == SimulationConfig.NO_STEP_DURATION) {
      return;
    }
    if (config.getStepDuration() < 0) {
      throw new IllegalArgumentException("Тривалість кроку не може бути від'ємною.");
    }

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
