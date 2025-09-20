package m.traffic.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import m.traffic.core.data.config.AccelerationBasedModelConfig;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;
import m.traffic.core.model.type.ModelType;

public class StatsCollector {

  private StatsCalculator statsCalculator = new StatsCalculator();
  private BufferedWriter velocityWriter;
  private BufferedWriter positionWriter;
  private BufferedWriter timeWriter;
  private BufferedWriter densityWriter;
  private BufferedWriter averageSpeedWriter;
  private BufferedWriter flowWriter;
  private StatsWriter statsWriter;

  public StatsCollector(SimulationConfig simulationConfig) {
    this.statsWriter = new StatsWriter(simulationConfig);
    String directoryName = getPrefix(simulationConfig);
    initDirectory(directoryName);

    try {
      velocityWriter = initFile(directoryName + "/velocity");
      positionWriter = initFile(directoryName + "/position");
      timeWriter = initFile(directoryName + "/time");
      densityWriter = initFile(directoryName + "/density");
      averageSpeedWriter = initFile(directoryName + "/average_speed");
      flowWriter = initFile(directoryName + "/flow");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getPrefix(SimulationConfig config) {
    // file name should be: file_prefix + road length + car count + max speed + breaking p. + other configs from specific model confings
    String filePrefix = "%s_L=%d_N=%d_Vmax=%d_p=%.2f".formatted(
          config.getOutputFilePrefix(),
          config.getRoadLength(),
          config.getCarCount(),
          config.getMaxSpeed(),
          config.getBreakingProbability()
        );

    if (config.getModelType() == ModelType.ACCELERATION_BASED_MODEL && 
        config instanceof AccelerationBasedModelConfig abmConfig) {
      // startAccelerationProbability, lowSpeedThreshold, lowSpeedThresholdBreakingProbability
      filePrefix += "p0=%.2f_LST=%d_pLST=%.2f".formatted(
          abmConfig.getStartAccelerationProbability(),
          abmConfig.getLowSpeedThreshold(),
          abmConfig.getLowSpeedThresholdBreakingProbability()
      );
    }
    return filePrefix;
  }

  private void initDirectory(String directoryName) {
    File directory = new File(directoryName);
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new RuntimeException("Failed to create directory: " + directoryName);
      }
    }
  }

  private BufferedWriter initFile(String filenamePrefix) throws IOException {
    File file = openOrCreateFile(filenamePrefix);
    return new BufferedWriter( new FileWriter(file) );
  }

  private File openOrCreateFile(String filename) throws IOException {
    File file = new File(filename);
    if (!file.exists()) {
      file.createNewFile();
    }
    return file;
  }

  public void collectStats() {
  }

  public void addToStats(SimulationStatistics simulationStatistics, TrafficSnapshot snapshot) {
    double density = statsCalculator.calculateDensity(snapshot);
    double averageSpeed = statsCalculator.calculateAverageSpeed(snapshot);
    double flow = statsCalculator.calculateFlow(snapshot);

    simulationStatistics.addDensity(density);
    simulationStatistics.addAverageSpeed(averageSpeed);
    simulationStatistics.addFlow(flow);
    simulationStatistics.incrementIterationCount();
  }

  public void writeStatsToFile(SimulationStatistics simulationStatistics) {
    // write density, average speed, and flow to file
    statsWriter.writeDensityToFile(simulationStatistics, densityWriter);
    statsWriter.writeAverageSpeedToFile(simulationStatistics, averageSpeedWriter);
    statsWriter.writeFlowToFile(simulationStatistics, flowWriter);
  }
  
  public void writeSnapshotToFile(TrafficSnapshot trafficSnapshot) {
    // write position, velocity and elapsed time to file
    statsWriter.writePositionsToFile(trafficSnapshot, positionWriter);
    statsWriter.writeVelocitiesToFile(trafficSnapshot, velocityWriter);
    statsWriter.writeElapsedTimeTickToFile(trafficSnapshot, timeWriter);
  }
}
