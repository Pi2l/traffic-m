package m.traffic.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Item;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;

public class StatsCollector {

  private StatsCalculator statsCalculator = new StatsCalculator();
  private BufferedWriter velocityWriter;
  private BufferedWriter positionWriter;
  private BufferedWriter timeWriter;
  private SimulationConfig simulationConfig;

  public StatsCollector(SimulationConfig simulationConfig) {
    this.simulationConfig = simulationConfig;
    String filenamePrefix = simulationConfig.getOutputFilePrefix();
    try {
      File velocityFile = openOrCreateFile(filenamePrefix + "_velocity");
      velocityWriter = new BufferedWriter(new FileWriter(velocityFile));

      File positionFile = openOrCreateFile(filenamePrefix + "_position");
      positionWriter = new BufferedWriter(new FileWriter(positionFile));

      File timeFile = openOrCreateFile(filenamePrefix + "_time");
      timeWriter = new BufferedWriter(new FileWriter(timeFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
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

  public void writeStatsToCsv(SimulationStatistics simulationStatistics) {
    // write density, average speed, and time to CSV3
    // dencity

    // average speed

    // time
  }
  
  public void writeSnapshotToFile(TrafficSnapshot trafficSnapshot) {
    // write position, velocity and elapsed time to file
    writePositionsToFile(trafficSnapshot);
    writeVelocitiesToFile(trafficSnapshot);
    writeElapsedTimeTickToFile(trafficSnapshot);
  }

  private void writePositionsToFile(TrafficSnapshot trafficSnapshot) {
    List<Integer> positions = trafficSnapshot.getRoad().stream().map(cell -> cell.getItem() != null ? 1 : 0).toList();
    try {
      for (Integer position : positions) {
        positionWriter.write(position.toString() + " ");
      }
      positionWriter.newLine();
      positionWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeVelocitiesToFile(TrafficSnapshot trafficSnapshot) {
    int noVelocityMarker = -1;
    List<Integer> velocities = new ArrayList<>(trafficSnapshot.getRoad().stream()
        .map(cell -> {
          Item item = cell.getItem();
          if (item != null && item instanceof Vehicle vehicle) {
            return vehicle.getVelocity();
          } else {
            return noVelocityMarker;
          }
        }).toList());
    
    // fill in the gaps with next car's velocity
    for (int i = 0; i < trafficSnapshot.getRoad().size(); i++) {
      if (velocities.get(i) == noVelocityMarker) {
        // find next car's velocity
        for (int j = 1; j < trafficSnapshot.getRoad().size(); j++) {
          Integer nextCarVelocity = getNextVehicleVelocity(velocities, i + j);
          if (nextCarVelocity != noVelocityMarker) {
            velocities.set(i, nextCarVelocity);
            break;
          }
        }
      }
    }

    try {
      for (Integer velocity : velocities) {
        velocityWriter.write(velocity.toString() + " ");
      }
      velocityWriter.newLine();
      velocityWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Integer getNextVehicleVelocity(List<Integer> velocities, int index) {
    if (simulationConfig.isCyclic()) {
      index = index % velocities.size();
    } else {
      if (index >= velocities.size()) {
        return simulationConfig.getMaxSpeed(); // next car is out of bounds, return max speed
      }
    }
    return velocities.get(index);
  }

  private void writeElapsedTimeTickToFile(TrafficSnapshot trafficSnapshot) {
    try {
      timeWriter.write(String.valueOf(trafficSnapshot.getStepCount()));
      timeWriter.newLine();
      timeWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
