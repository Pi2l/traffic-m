package m.traffic.core.data.config;

import m.traffic.core.model.type.ModelType;

public record SimulationConfig(
  int roadLength,
  int carCount,
  int maxSpeed,
  int stepDuration,
  float breakingProbability,
  boolean isCyclic,
  String outputFilePrefix,
  int stepCount,
  long randomSeed,
  ModelType modelType
) {
  public static final int INFINITE_STEP_COUNT = -1;

  public static SimulationConfig defaultConfig() {
    return new SimulationConfig(20, 3, 5, 100, 0.3f, false, "out/test", 200, 394, ModelType.CELLULAR_AUTOMATON);
  }
}
