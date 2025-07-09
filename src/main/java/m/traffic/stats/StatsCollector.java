package m.traffic.stats;

import m.traffic.core.data.state.SimulationStatistics;
import m.traffic.core.data.state.TrafficSnapshot;

public class StatsCollector {

  private StatsCalculator statsCalculator = new StatsCalculator();

  public void collectStats() {
  }

  public void addToStats(SimulationStatistics simulationStatistics, TrafficSnapshot snapshot) {
    double density = statsCalculator.calculateDensity(snapshot);
    double averageSpeed = statsCalculator.calculateAverageSpeed(snapshot);
    double flux = statsCalculator.calculateFlux(snapshot);

    simulationStatistics.addDencity(density);
    simulationStatistics.addAverageSpeed(averageSpeed);
    simulationStatistics.addFlux(flux);
    simulationStatistics.incrementIterationCount();
  }

  public void writeStatsToCsv(SimulationStatistics simulationStatistics) {
  }
}
