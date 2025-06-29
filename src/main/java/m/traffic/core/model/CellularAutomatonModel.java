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
    for (int i = 0; i < config.roadLength(); ++i) {
      road.add(new Cell(i));
    }
    
    for (int i = 0; i < config.carCount(); ++i) {
      Vehicle car;
      do {
        int speed = (int) (Math.random() * config.maxSpeed());
        int position = (int) (Math.random() * config.roadLength());
        car = new Vehicle(position, speed);

      } while (road.get(car.getRoadPosition()).getItem() != null);
      cars.add(car);
      road.get(car.getRoadPosition()).setItem(car);
    }

    cars.sort((a, b) -> Integer.compare(a.getRoadPosition(), b.getRoadPosition()));
  }

  @Override
  public void nextStep() {
    for (int i = 0; i < cars.size(); ++i) { // TODO: implement in parallel
      int nextCarIndex = (i + 1) == cars.size() ? 0 : i + 1; // cycle to the first car
      Vehicle nextCar = cars.get(nextCarIndex); // NB: should be sorted by road position
      Vehicle currentCar = cars.get(i);

      int distanceToNextCar = getDistanceToNextCar(currentCar, nextCar);

      updateCarVelocity(currentCar, distanceToNextCar);
      ramdomlyBrake(currentCar);
    }
    for (var currentCar : cars) {
      updateCarPosition(currentCar);
    }
  }

  private int getDistanceToNextCar(Vehicle currentCar, Vehicle nextCar) {
    int distanceToNextCar = nextCar.getRoadPosition() - currentCar.getRoadPosition();
    if (distanceToNextCar < 0) {
      distanceToNextCar += config.roadLength(); // cycle around the road
    }
    return distanceToNextCar;
  }

  private void updateCarVelocity(Vehicle currentCar, int distanceToNextCar) {
    // here can be added traffic light logic
    int nextVelocity = Math.min(currentCar.getVelocity() + 1, config.maxSpeed());

    // distanceToNextCar - 1 as we should update position before next car
    currentCar.setVelocity( Math.min(nextVelocity, distanceToNextCar - 1) );
  }
  
  private void ramdomlyBrake(Vehicle currentCar) {
    int carVelocity = currentCar.getVelocity();
    if (randomNextFloat() < config.breakingProbability() && carVelocity > 0) {
      currentCar.setVelocity(carVelocity - 1);
    }
  }

  private void updateCarPosition(Vehicle currentCar) {
    road.get(currentCar.getRoadPosition()).setItem(null);
    int nextPosition = currentCar.getRoadPosition() + currentCar.getVelocity();
    if (nextPosition >= config.roadLength()) {
      nextPosition = nextPosition % config.roadLength(); // cycle around the road
    }
    currentCar.setRoadPosition(nextPosition);
    road.get(currentCar.getRoadPosition()).setItem(currentCar);
  }

  private float randomNextFloat() {
    return (float) Math.random(); // TODO: replace all Math.random() with a better random generator
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
