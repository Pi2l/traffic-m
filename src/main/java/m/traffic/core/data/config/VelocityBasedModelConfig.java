package m.traffic.core.data.config;

import lombok.Getter;
import lombok.Setter;
import m.traffic.core.model.type.ModelType;

@Getter
@Setter
public class VelocityBasedModelConfig extends SimulationConfig {
  private double startAccelerationProbability;
  private int lowSpeedThreshold;
  private double lowSpeedThresholdBrakingProbability;
  private boolean useMaxSpeedBrakingProbability;
  private double maxSpeedBrakingProbability;
    
  public VelocityBasedModelConfig(int roadLength, int carCount, int maxSpeed,
      int stepDuration, double brakingProbability, boolean isCyclic, String outputFilePrefix,
      int stepCount, long randomSeed, ModelType modelType,
      double startAccelerationProbability, int lowSpeedThreshold, double lowSpeedThresholdBrakingProbability,
      boolean useLowSpeedBrakingProbability, double lowSpeedBrakingProbability) {
    super(roadLength, carCount, maxSpeed, stepDuration, brakingProbability, isCyclic, outputFilePrefix,
        stepCount, randomSeed, modelType);

    if (lowSpeedThreshold >= maxSpeed) {
      throw new IllegalArgumentException("Low speed threshold should be less than max speed");
    }

    this.startAccelerationProbability = startAccelerationProbability;
    this.lowSpeedThreshold = lowSpeedThreshold;
    this.lowSpeedThresholdBrakingProbability = lowSpeedThresholdBrakingProbability;
    this.useMaxSpeedBrakingProbability = useLowSpeedBrakingProbability;
    this.maxSpeedBrakingProbability = lowSpeedBrakingProbability;
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
      2,
      0.2f,
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
        config.getLowSpeedThreshold(),
        config.getLowSpeedThresholdBrakingProbability(),
        config.isUseMaxSpeedBrakingProbability(),
        config.getMaxSpeedBrakingProbability()
    );
  }
}