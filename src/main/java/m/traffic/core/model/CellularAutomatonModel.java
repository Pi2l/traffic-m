package m.traffic.core.model;

import java.util.ArrayList;
import java.util.List;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Cell;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;

public class CellularAutomatonModel implements TrafficModel {
  private List<Cell> road;
  private List<Vehicle> cars;
  private SimulationConfig config;
  private TrafficSnapshot trafficSnapshot;
  private SimulationStatistics simulationStatistics;

  @Override
  public void initialise(SimulationConfig config) {
    this.config = config;
    road = new ArrayList<>(config.roadLength());
    cars = new ArrayList<>(config.carCount());
    randomiseCarPositionAndSpeed();
  }

  private void randomiseCarPositionAndSpeed() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getConfig'");
  }

  @Override
  public void nextStep() {
    for (int i = 0; i < cars.size(); ++i) { // TODO: implement in parallel
      Vehicle nextCar = cars.get(i + 1);
      Vehicle currentCar = cars.get(i);

      int distanceToNextCar = nextCar.getRoadPosition() - currentCar.getRoadPosition();

      updateCarVelocity(currentCar, distanceToNextCar);
      ramdomlyBrake(currentCar);
    }
    for (var currentCar : cars) {
      updateCarPosition(currentCar);
    }
  }

  private void updateCarVelocity(Vehicle currentCar, int distanceToNextCar) {
    // here can be added traffic light logic
    int nextVelocity = Math.min(currentCar.getVelocity() + 1, config.maxSpeed());

    currentCar.setVelocity( Math.min(nextVelocity, distanceToNextCar - 1) );// distanceToNextCar-1 as we should update position before next car
  }
  
  private void ramdomlyBrake(Vehicle currentCar) {
    int carVelocity = currentCar.getVelocity();
    if (randomNextFloat() < config.breakingProbability() && carVelocity > 0) {
      currentCar.setVelocity(carVelocity - 1);
    }
  }

  private void updateCarPosition(Vehicle currentCar) {
    currentCar.setRoadPosition(currentCar.getRoadPosition() + currentCar.getVelocity());
  }

  private float randomNextFloat() {
    return 1;
  }

  @Override
  public SimulationConfig getConfig() {
    return config;
  }

  @Override
  public TrafficSnapshot getSnapshot() {
    return trafficSnapshot;
  }

  @Override
  public SimulationStatistics getStatistics() {
    return simulationStatistics;
  }

}
