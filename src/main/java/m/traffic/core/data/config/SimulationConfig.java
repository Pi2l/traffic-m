package m.traffic.core.data.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import m.traffic.core.model.type.ModelType;

@Getter
@Setter
@AllArgsConstructor
public class SimulationConfig {
  public static final int INFINITE_STEP_COUNT = -1;

  private int roadLength;
  private int carCount;
  private int maxSpeed;
  private int stepDuration;
  private float breakingProbability;
  private boolean isCyclic;
  private String outputFilePrefix;
  private int stepCount;
  private long randomSeed;
  private ModelType modelType;

  public static SimulationConfig defaultConfig() {
    return new SimulationConfig(20, 3, 5, 100, 0.3f, false, "out/test", 200, 394, ModelType.CELLULAR_AUTOMATON);
  }

  public static SimulationConfig copyConfig(SimulationConfig config) {
    return new SimulationConfig(
        config.roadLength,
        config.carCount,
        config.maxSpeed,
        config.stepDuration,
        config.breakingProbability,
        config.isCyclic,
        config.outputFilePrefix,
        config.stepCount,
        config.randomSeed,
        config.modelType
    );
  }
}
