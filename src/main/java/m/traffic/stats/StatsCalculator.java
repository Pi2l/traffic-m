package m.traffic.stats;

import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.TrafficSnapshot;

public class StatsCalculator {

  // Global flow, J(p) = Number of vehicles passing a point / Number of time steps
  public double calculateDensity(TrafficSnapshot snapshot) { // p = Number of vehicles / Length of road
    return snapshot.getCars().size() / (double) snapshot.getRoad().size();
  }

  public double calculateAverageSpeed(TrafficSnapshot snapshot) {
    // Vsp = Sum of vehicle speeds / Number of vehicles
    return snapshot.getCars().stream().mapToInt(Vehicle::getVelocity).average().orElse(0.0); // this is SMS (space-mean speed)
  }

  public double calculateFlow(TrafficSnapshot snapshot) {
    // q = Number of vehicles passing a point / Number of time steps
    // Assuming that point of measurement is at the start of the road
    return snapshot.getVehiclesPassed();
  }

}
