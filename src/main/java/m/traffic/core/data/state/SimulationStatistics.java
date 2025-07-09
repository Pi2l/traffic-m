package m.traffic.core.data.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimulationStatistics {

  private double dencitySum;
  private double averageSpeedSum;
  private double fluxSum;
  // private double averageDistanceToNextCarSum;
  
  private long iterationCount;

  public double getDencity() {
    return dencitySum / iterationCount;
  }

  public double getAverageSpeed() {
    return averageSpeedSum / iterationCount;
  }

  public double getFlux() {
    return fluxSum / iterationCount;
  }

  public void addDencity(double dencity) {
    dencitySum += dencity;
  }

  public void addAverageSpeed(double averageSpeed) {
    averageSpeedSum += averageSpeed;
  }

  public void addFlux(double flux) {
    fluxSum += flux;
  }

  public void incrementIterationCount() {
    iterationCount++;
  }
}
