package m.traffic.core.data.config;

public record SimulationConfig(
  int roadLength,
  int carCount,
  int maxSpeed,
  int stepDuration,
  float breakingProbability
) { }
