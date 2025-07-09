package m.traffic.stats;

import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.TrafficSnapshot;

public class StatsCalculator {

  public double calculateDensity(TrafficSnapshot snapshot) {
    return snapshot.getCars().size() / (double) snapshot.getRoad().size();
  }

  public double calculateAverageSpeed(TrafficSnapshot snapshot) {
    return snapshot.getCars().stream().mapToInt(Vehicle::getVelocity).average().orElse(0.0);
  }

  public double calculateFlux(TrafficSnapshot snapshot) {
    return snapshot.getCars().stream().mapToDouble(car -> car.getVelocity() * (1.0 / snapshot.getStepDuration())).sum(); // ?????
  }

}
