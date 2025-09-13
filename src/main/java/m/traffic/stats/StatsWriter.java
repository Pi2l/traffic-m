package m.traffic.stats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Item;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;

@RequiredArgsConstructor
public class StatsWriter {

  private final SimulationConfig simulationConfig;

  public void writePositionsToFile(TrafficSnapshot trafficSnapshot, BufferedWriter writer) {
    List<Integer> positions = trafficSnapshot.getRoad().stream().map(cell -> cell.getItem() != null ? 1 : 0).toList();
    try {
      for (Integer position : positions) {
        writer.write(position.toString() + " ");
      }
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeVelocitiesToFile(TrafficSnapshot trafficSnapshot, BufferedWriter writer) {
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
        writer.write(velocity.toString() + " ");
      }
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeElapsedTimeTickToFile(TrafficSnapshot trafficSnapshot, BufferedWriter writer) {
    try {
      writer.write(String.valueOf(trafficSnapshot.getStepCount()));
      writer.newLine();
      writer.flush();
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

  public void writeDensityToFile(SimulationStatistics simulationStatistics, BufferedWriter densityWriter) {
    try {
      densityWriter.write(String.valueOf(simulationStatistics.getDensity()));
      densityWriter.newLine();
      densityWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeAverageSpeedToFile(SimulationStatistics simulationStatistics, BufferedWriter averageSpeedWriter) {
    try {
      averageSpeedWriter.write(String.valueOf(simulationStatistics.getAverageSpeed()));
      averageSpeedWriter.newLine();
      averageSpeedWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void writeFlowToFile(SimulationStatistics simulationStatistics, BufferedWriter flowWriter) {
    try {
      flowWriter.write(String.valueOf(simulationStatistics.getFlow()));
      flowWriter.newLine();
      flowWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
