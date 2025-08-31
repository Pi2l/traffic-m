package m.traffic.core.data.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import m.traffic.core.model.type.ModelType;

@Getter
@Setter
@RequiredArgsConstructor
public class SimulationConfig {
  public static final int INFINITE_STEP_COUNT = -1;

  private final int roadLength;
  private final int carCount;
  private final int maxSpeed;
  private final int stepDuration;
  private final float breakingProbability;
  private final boolean isCyclic;
  private final String outputFilePrefix;
  private final int stepCount;
  private final long randomSeed;
  private final ModelType modelType;

  public static SimulationConfig defaultConfig() {
    return new SimulationConfig(20, 3, 5, 100, 0.3f, false, "out/test", 200, 394, ModelType.CELLULAR_AUTOMATON);
  }
}
