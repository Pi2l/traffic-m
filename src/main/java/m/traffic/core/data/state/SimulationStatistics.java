package m.traffic.core.data.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimulationStatistics {

  private double densitySum; // sum of densities over all iterations
  private double averageSpeedSum; // sum of average speeds over all iterations
  private double flowSum; // sum of flows over all iterations

  private long iterationCount;

  public double getDensity() {
    return densitySum / iterationCount;
  }

  public double getAverageSpeed() {
    return averageSpeedSum / iterationCount;
  }

  public double getFlow() {
    return flowSum / iterationCount;
  }

  public void addDensity(double density) {
    densitySum += density;
  }

  public void addAverageSpeed(double averageSpeed) {
    averageSpeedSum += averageSpeed;
  }

  public void addFlow(double flow) {
    flowSum += flow;
  }

  public void incrementIterationCount() {
    iterationCount++;
  }
}
