package m.traffic.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.data.simulation.Cell;
import m.traffic.core.data.simulation.Vehicle;
import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;
import m.traffic.stats.StatsCollector;

public class Rule184 implements TrafficModel {
  private static final int DETECTOR_POSITION = 0;

  private List<Cell> road;
  private List<Cell> nextStepRoad;
  private List<Vehicle> cars;
  private SimulationConfig config;
  private TrafficSnapshot trafficSnapshot;
  private SimulationStatistics simulationStatistics = new SimulationStatistics(0, 0, 0, 0);
  private StatsCollector statsCollector;
  private int stepCount = 0;
  private int vehiclesPassedPerStep = 0;
  private Random random;

  @Override
  public void initialise(SimulationConfig config) {
    if (config.getMaxSpeed() != 1) {
      throw new IllegalArgumentException("Rule 184 requires max speed to be 1");
    }
    this.config = config;
    random = new Random(config.getRandomSeed());

    statsCollector = new StatsCollector(config);
    road = new ArrayList<>(config.getRoadLength());
    nextStepRoad = new ArrayList<>(config.getRoadLength());
    cars = new ArrayList<>(config.getCarCount());
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
        car = new Vehicle(position, config.getMaxSpeed()); // Rule 184 uses max speed of 1

      } while (road.get(car.getRoadPosition()).getItem() != null);
      cars.add(car);
      road.get(car.getRoadPosition()).setItem(car);
    }
    copyRoad(road, nextStepRoad);
  }

  private int getRandomInt(int max) {
    return random.nextInt(0, max);
  }

  private void copyRoad(List<Cell> source, List<Cell> target) {
    target.clear();
    for (Cell cell : source) {
      Cell newCell = new Cell(cell.getPosition());
      newCell.setItem(cell.getItem());
      target.add(newCell);
    }
  }

  @Override
  public void nextStep() {
    for (int i = 0; i < config.getRoadLength(); ++i) {
      Vehicle leftCar = getVehicle(i - 1);
      Vehicle currentCar = getVehicle(i);
      Vehicle rightCar = getVehicle(i + 1);

      checkIfVehiclePassedDetector(currentCar, rightCar);
      // Rule 184 logic: if the left car is 1 and current car is 0 then set current car to 1
      // or if the current car is 1 and right car is 0 then set current car to 0
      // else keep the current car's velocity
      if (leftCar != null && currentCar == null) {
        nextStepRoad.get(i).setItem(leftCar);
      } else if (currentCar != null && rightCar == null) {
        nextStepRoad.get(i).setItem(null);
      } else {
        nextStepRoad.get(i).setItem(currentCar);
      }
    }

    copyRoad(nextStepRoad, road);
    takeSnapshot();
    updateSimulationStatistics(getSnapshot());
    vehiclesPassedPerStep = 0;
    stepCount++;
  }

  private void checkIfVehiclePassedDetector(Vehicle currentCar, Vehicle rightCar) {
    if (currentCar != null && rightCar == null && currentCar.getRoadPosition() == DETECTOR_POSITION) {
      vehiclesPassedPerStep++;
    }
  }

  private Vehicle getVehicle(int i) {
    int roadLength = config.getRoadLength();
    int index = 0;
    if (config.isCyclic()) {
      index = (i + roadLength) % roadLength; // wrap around if negative
    } else {
      // TODO: this code can cause random spawning of vehicle on the road and also can remove vehicle from it
      // cannot be implemented in not cyclic mode?
      if (i < 0 || i >= roadLength) { // randomly return a vehicle with max speed if out of bounds
        int randomNum = getRandomInt(config.getMaxSpeed() + 1); // maxSpeed is 1, so randomNum can be 0 or 1
        return randomNum == 1 ? new Vehicle(i, randomNum) : null;
      } else {
        index = i;
      }
    }

    return (Vehicle) road.get(index).getItem(); // null if no vehicle is present
  }

  private void takeSnapshot() {
    trafficSnapshot = new TrafficSnapshot();
    trafficSnapshot.setRoad(road);
    trafficSnapshot.setCars(cars);
    trafficSnapshot.setStepCount(stepCount);
    trafficSnapshot.setVehiclesPassed(vehiclesPassedPerStep);
  }

  private void updateSimulationStatistics(TrafficSnapshot snapshot) {
    statsCollector.addToStats(simulationStatistics, snapshot);
    statsCollector.writeStatsToFile(simulationStatistics);
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
