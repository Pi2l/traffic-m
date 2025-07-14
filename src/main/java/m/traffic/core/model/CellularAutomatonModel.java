package m.traffic.core.model;

import java.util.ArrayList;
import java.util.List;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Cell;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;
import m.traffic.stats.StatsCollector;

public class CellularAutomatonModel implements TrafficModel {
  private List<Cell> road;
  private List<Vehicle> cars;
  private SimulationConfig config;
  private TrafficSnapshot trafficSnapshot;
  private SimulationStatistics simulationStatistics = new SimulationStatistics(0, 0, 0, 0);
  private StatsCollector statsCollector;
  private int stepCount = 0;

  @Override
  public void initialise(SimulationConfig config) {
    this.config = config;
    statsCollector = new StatsCollector(config);
    road = new ArrayList<>(config.roadLength());
    cars = new ArrayList<>(config.carCount());
    randomiseCarPositionAndSpeed();
  }

  private void randomiseCarPositionAndSpeed() {
    for (int i = 0; i < config.roadLength(); ++i) {
      road.add(new Cell(i));
    }
    
    // randomly place cars on the road
    for (int i = 0; i < config.carCount(); ++i) {
      Vehicle car;
      do {
        int position = getRandomInt(config.roadLength());
        car = new Vehicle(position, 0);

      } while (road.get(car.getRoadPosition()).getItem() != null);
      cars.add(car);
      road.get(car.getRoadPosition()).setItem(car);
    }

    sortCarsByRoadPosition();

    // randomly set car speeds
    int carCount = cars.size();
    for (int i = 0; i < carCount; ++i) {
      int speed = getRandomInt(config.maxSpeed() + 1);

      Vehicle currentCar = cars.get(i);
      int distanceToNextCar = getDistanceToNextCar(currentCar, getNextCar(i, carCount));
      if (speed >= Math.max(0, distanceToNextCar - 1)) {
        speed = Math.max(0, distanceToNextCar - 1); // ensure that the car does not collide with the next car
      }
      currentCar.setVelocity(speed);
    }
  }

  private void sortCarsByRoadPosition() {
    cars.sort((a, b) -> Integer.compare(a.getRoadPosition(), b.getRoadPosition()));
  }

  private int getRandomInt(int max) {
    return (int) (Math.random() * (max));
  }

  @Override
  public void nextStep() {
    int carCount = cars.size();
    for (int i = 0; i < carCount; ++i) { // TODO: implement in parallel
      Vehicle nextCar = getNextCar(i, carCount); // NB: should be sorted by road position
      Vehicle currentCar = cars.get(i);

      int distanceToNextCar = getDistanceToNextCar(currentCar, nextCar);

      updateCarVelocity(currentCar, distanceToNextCar);
      ramdomlyBrake(currentCar);
    }

    for (var currentCar : cars) {
      updateCarPosition(currentCar);
    }
    sortCarsByRoadPosition();
    takeSnapshot();
    updateSimulationStatistics(getSnapshot());
    stepCount++;
  }

  private void updateSimulationStatistics(TrafficSnapshot snapshot) {
    statsCollector.addToStats(simulationStatistics, snapshot);
    statsCollector.writeStatsToCsv(simulationStatistics);
  }

  private Vehicle getNextCar(int index, int carCount) {
    boolean lastCar = (index + 1) == carCount;
    if (!lastCar) {
      return cars.get(index + 1);
    } else if (lastCar && config.isCyclic()) {
      return cars.get(0);
    }
    return cars.get(0).getRoadPosition() != 0 
        ? new Vehicle(config.roadLength() + config.maxSpeed(), config.maxSpeed()) // no next car, so car is out of road
        : new Vehicle(config.roadLength(), 0); // next car is just before out of road to avoid collision with car tath is at the first place
  }

  private int getDistanceToNextCar(Vehicle currentCar, Vehicle nextCar) {
    int distanceToNextCar = nextCar.getRoadPosition() - currentCar.getRoadPosition();
    if (distanceToNextCar < 0) {
      if (config.isCyclic()) {
        distanceToNextCar += config.roadLength();
      } else {
        throw new IllegalStateException("Next car is behind the current car. Current car position: " 
            + currentCar.getRoadPosition() + ", next car position: " + nextCar.getRoadPosition());
      } 
    }
    return distanceToNextCar;
  }

  private void updateCarVelocity(Vehicle currentCar, int distanceToNextCar) {
    // here can be added traffic light logic
    int nextVelocity = Math.min(currentCar.getVelocity() + 1, config.maxSpeed());

    // distanceToNextCar - 1 as we should update position before next car
    currentCar.setVelocity( Math.min(nextVelocity, Math.max(distanceToNextCar - 1, 0)) );
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
      if (config.isCyclic()) {
        nextPosition = nextPosition % config.roadLength(); // cycle around the road
      } else {
        nextPosition = Math.min(getRandomInt(cars.get(0).getRoadPosition()), config.maxSpeed() - 1); // if not cyclic, place car randomly on the road
        updateCarVelocity(currentCar, Math.max(nextPosition, 1)); // speed is based on how car is placed on the road. Eg car passed 3 cells from 0, so its speed should be 3
      }
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

  private void takeSnapshot() {
    trafficSnapshot = new TrafficSnapshot();
    trafficSnapshot.setRoad(road);
    trafficSnapshot.setCars(cars);
    trafficSnapshot.setStepCount(stepCount);
  }
}
