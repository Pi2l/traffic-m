package m.traffic.core.data.config;

import lombok.Getter;
import lombok.Setter;
import m.traffic.core.model.type.ModelType;

@Getter
@Setter
public class VelocityBasedModelConfig extends SimulationConfig {
  private double startAccelerationProbability;
  private boolean useMaxSpeedBrakingProbability;
  private double maxSpeedBrakingProbability;
    
  public VelocityBasedModelConfig(int roadLength, int carCount, int maxSpeed,
      int stepDuration, double brakingProbability, boolean isCyclic, String outputFilePrefix,
      int stepCount, long randomSeed, ModelType modelType,
      double startAccelerationProbability, boolean useMaxSpeedBrakingProbability, double maxSpeedBrakingProbability) {
    super(roadLength, carCount, maxSpeed, stepDuration, brakingProbability, isCyclic, outputFilePrefix,
        stepCount, randomSeed, modelType);

    this.startAccelerationProbability = startAccelerationProbability;
    this.useMaxSpeedBrakingProbability = useMaxSpeedBrakingProbability;
    this.maxSpeedBrakingProbability = maxSpeedBrakingProbability;
  }

  public static VelocityBasedModelConfig defaultConfig() {
    SimulationConfig baseConfig = SimulationConfig.defaultConfig();
    return new VelocityBasedModelConfig(
      baseConfig.getRoadLength(),
      baseConfig.getCarCount(),
      baseConfig.getMaxSpeed(),
      baseConfig.getStepDuration(),
      baseConfig.getBrakingProbability(),
      baseConfig.isCyclic(),
      baseConfig.getOutputFilePrefix(),
      baseConfig.getStepCount(),
      baseConfig.getRandomSeed(),
      baseConfig.getModelType(),
      0.5f,
      true,
      0.3f
    );
  }

  public static VelocityBasedModelConfig copyConfig(VelocityBasedModelConfig config) {
    return new VelocityBasedModelConfig(
        config.getRoadLength(),
        config.getCarCount(),
        config.getMaxSpeed(),
        config.getStepDuration(),
        config.getBrakingProbability(),
        config.isCyclic(),
        config.getOutputFilePrefix(),
        config.getStepCount(),
        config.getRandomSeed(),
        config.getModelType(),
        config.getStartAccelerationProbability(),
        config.isUseMaxSpeedBrakingProbability(),
        config.getMaxSpeedBrakingProbability()
    );
  }
}