package m.traffic.core.data.config;

public record SimulationConfig(
  int roadLength,
  int carCount,
  int maxSpeed,
  int stepDuration,
  float breakingProbability,
  boolean isCyclic
) {
  public static SimulationConfig defaultConfig() {
    return new SimulationConfig(20, 3, 5, 300, 0.3f, false);
  }
}
