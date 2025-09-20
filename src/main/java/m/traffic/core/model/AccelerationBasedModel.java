package m.traffic.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import m.traffic.core.data.config.AccelerationBasedModelConfig;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Cell;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;
import m.traffic.stats.StatsCollector;

public class AccelerationBasedModel implements TrafficModel {
  private static final int DETECTOR_POSITION = 0;
  private List<Cell> road;
  private List<Vehicle> cars;
  private AccelerationBasedModelConfig config;
  private TrafficSnapshot trafficSnapshot;
  private SimulationStatistics simulationStatistics = new SimulationStatistics(0, 0, 0, 0);
  private StatsCollector statsCollector;
  private Random random;
  private int stepCount = 0;
  private int vehiclesPassedPerStep = 0;

  @Override
  public void initialise(SimulationConfig config) {
    if (!(config instanceof AccelerationBasedModelConfig abmConfig)) {
      throw new IllegalArgumentException("Config should be of type AccelerationBasedModelConfig");
    }
    this.config = abmConfig;
    random = new Random(abmConfig.getRandomSeed());

    statsCollector = new StatsCollector(abmConfig);
    road = new ArrayList<>(abmConfig.getRoadLength());
    cars = new ArrayList<>(abmConfig.getCarCount());
    randomiseCarPositionAndSpeed();
  }

  private void randomiseCarPositionAndSpeed() {
    for (int i = 0; i < config.getRoadLength(); ++i) {
      road.add(new Cell(i));
    }
    
    // randomly place cars on the road
    for (int i = 0; i < config.getCarCount(); ++i) {
      Vehicle car;
      do {
        int position = getRandomInt(config.getRoadLength());
        car = new Vehicle(position, 0);

      } while (road.get(car.getRoadPosition()).getItem() != null);
      cars.add(car);
      road.get(car.getRoadPosition()).setItem(car);
    }

    sortCarsByRoadPosition();

    // randomly set car speeds
    int carCount = cars.size();
    for (int i = 0; i < carCount; ++i) {
      int speed = getRandomInt(config.getMaxSpeed() + 1);

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
    return random.nextInt(0, max);
  }

  @Override
  public void nextStep() {
    int carCount = cars.size();
    for (int i = 0; i < carCount; ++i) {
      Vehicle nextCar = getNextCar(i, carCount); // NB: should be sorted by road position
      Vehicle currentCar = cars.get(i);

      int distanceToNextCar = getDistanceToNextCar(currentCar, nextCar);

      updateCarVelocity(currentCar, distanceToNextCar);
      randomlyBrake(currentCar);
    }

    for (var currentCar : cars) {
      checkIfVehiclePassesDetector(currentCar);
      updateCarPosition(currentCar);
    }

    sortCarsByRoadPosition();
    takeSnapshot();
    updateSimulationStatistics(getSnapshot());
    vehiclesPassedPerStep = 0;
    stepCount++;
  }

  private void checkIfVehiclePassesDetector(Vehicle currentCar) {
    if (config.isCyclic()) {
      int nextCarPosition = getCarNextPosition(currentCar);
      boolean atDetectorNow = currentCar.getRoadPosition() == DETECTOR_POSITION;
      for (int pos = currentCar.getRoadPosition() + 1; (pos % config.getRoadLength()) != ((nextCarPosition + 1) % config.getRoadLength()); pos++) {
        int checkPos = pos % config.getRoadLength();
        if (checkPos == DETECTOR_POSITION) {
          // should pass detector in the next step
          atDetectorNow = true;
          continue;
        }
        if (atDetectorNow) {
          vehiclesPassedPerStep++;
          break;
        }
      }
    } else {
      if (currentCar.getRoadPosition() < DETECTOR_POSITION && 
          currentCar.getRoadPosition() + currentCar.getVelocity() >= DETECTOR_POSITION) {
        vehiclesPassedPerStep++;
      }
    }
  }

  private void updateSimulationStatistics(TrafficSnapshot snapshot) {
    statsCollector.addToStats(simulationStatistics, snapshot);
    statsCollector.writeStatsToFile(simulationStatistics);
  }

  private Vehicle getNextCar(int index, int carCount) {
    boolean lastCar = (index + 1) == carCount;
    if (!lastCar) {
      return cars.get(index + 1);
    } else if (lastCar && config.isCyclic()) {
      return cars.get(0);
    }
    return cars.get(0).getRoadPosition() != 0 
        ? new Vehicle(config.getRoadLength() + config.getMaxSpeed(), config.getMaxSpeed()) // no next car, so car is out of road
        : new Vehicle(config.getRoadLength(), 0); // next car is just before out of road to avoid collision with car tath is at the first place
  }

  private int getDistanceToNextCar(Vehicle currentCar, Vehicle nextCar) {
    int distanceToNextCar = nextCar.getRoadPosition() - currentCar.getRoadPosition();
    if (distanceToNextCar < 0) {
      if (config.isCyclic()) {
        distanceToNextCar += config.getRoadLength();
      } else {
        throw new IllegalStateException("Next car is behind the current car. Current car position: " 
            + currentCar.getRoadPosition() + ", next car position: " + nextCar.getRoadPosition());
      } 
    }
    return distanceToNextCar;
  }

  private void updateCarVelocity(Vehicle currentCar, int distanceToNextCar) {
    int nextVelocity = Math.min(currentCar.getVelocity() + 1, config.getMaxSpeed());

    // distanceToNextCar - 1 as we should update position before next car
    currentCar.setVelocity( Math.min(nextVelocity, Math.max(distanceToNextCar - 1, 0)) );
  }
  
  private void randomlyBrake(Vehicle currentCar) {
    int carAcceleratedVelocity = currentCar.getVelocity();
    int carCurrentVelocity = carAcceleratedVelocity - 1;
    float randomValue = randomNextFloat();
  
    if (carAcceleratedVelocity == 0) {
      return; // cannot brake if car is not moving
    }

    if (carCurrentVelocity == 0 && randomValue < config.getStartAccelerationProbability()) { // slow to start rule
      currentCar.setVelocity(carAcceleratedVelocity - 1);
    } else if (randomValue < config.getLowSpeedThresholdBreakingProbability() && carCurrentVelocity < config.getLowSpeedThreshold()) {
      // low speed car less likely to brake
      currentCar.setVelocity(carAcceleratedVelocity - 1);
    } else if (randomValue < config.getBreakingProbability()) {
      // normal braking
      currentCar.setVelocity(carAcceleratedVelocity - 1);
    } else if (randomValue < Math.min(1, config.getBreakingProbability() + 0.1f) && carCurrentVelocity >= (config.getMaxSpeed() - 1)) {
      // chance to accelerate if car is at max speed should be lower
      currentCar.setVelocity(carAcceleratedVelocity - 1);
    }
  }

  private void updateCarPosition(Vehicle currentCar) {
    road.get(currentCar.getRoadPosition()).setItem(null);
    int nextPosition = getCarNextPosition(currentCar);
    currentCar.setRoadPosition(nextPosition);
    road.get(currentCar.getRoadPosition()).setItem(currentCar);
  }

  private int getCarNextPosition(Vehicle currentCar) {
    int nextPosition = currentCar.getRoadPosition() + currentCar.getVelocity();
    if (nextPosition >= config.getRoadLength()) {
      if (config.isCyclic()) {
        nextPosition = nextPosition % config.getRoadLength(); // cycle around the road
      } else {
        nextPosition = Math.min(getRandomInt(cars.get(0).getRoadPosition()), config.getMaxSpeed() - 1); // if not cyclic, place car randomly on the road
        updateCarVelocity(currentCar, Math.max(nextPosition, 1)); // speed is based on how car is placed on the road. Eg car passed 3 cells from 0, so its speed should be 3
      }
    }
    return nextPosition;
  }

  private float randomNextFloat() {
    return random.nextFloat();
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

  @Override
  public StatsCollector getStatsCollector() {
    return statsCollector;
  }

  private void takeSnapshot() {
    trafficSnapshot = new TrafficSnapshot();
    trafficSnapshot.setRoad(road);
    trafficSnapshot.setCars(cars);
    trafficSnapshot.setStepCount(stepCount);
    trafficSnapshot.setVehiclesPassed(vehiclesPassedPerStep);
  }
}
