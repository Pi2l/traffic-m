package m.traffic.core.data.config;

import lombok.Getter;
import lombok.Setter;
import m.traffic.core.model.type.ModelType;

@Getter
@Setter
public class AccelerationBasedModelConfig extends SimulationConfig {
  private double startAccelerationProbability;
  private int lowSpeedThreshold;
  private double lowSpeedThresholdBrakingProbability;
    
  public AccelerationBasedModelConfig(int roadLength, int carCount, int maxSpeed,
      int stepDuration, double brakingProbability, boolean isCyclic, String outputFilePrefix,
      int stepCount, long randomSeed, ModelType modelType,
      double startAccelerationProbability, int lowSpeedThreshold, double lowSpeedThresholdBrakingProbability) {
    super(roadLength, carCount, maxSpeed, stepDuration, brakingProbability, isCyclic, outputFilePrefix,
        stepCount, randomSeed, modelType);

    if (lowSpeedThreshold >= maxSpeed) {
      throw new IllegalArgumentException("Low speed threshold should be less than max speed");
    }

    this.startAccelerationProbability = startAccelerationProbability;
    this.lowSpeedThreshold = lowSpeedThreshold;
    this.lowSpeedThresholdBrakingProbability = lowSpeedThresholdBrakingProbability;
  }

  public static AccelerationBasedModelConfig defaultConfig() {
    SimulationConfig baseConfig = SimulationConfig.defaultConfig();
    return new AccelerationBasedModelConfig(
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
      0.2f
    );
  }

  public static AccelerationBasedModelConfig copyConfig(AccelerationBasedModelConfig config) {
    return new AccelerationBasedModelConfig(
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
        config.getLowSpeedThresholdBrakingProbability()
    );
  }
}