package m.traffic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import m.traffic.core.data.config.AccelerationBasedModelConfig;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.model.type.ModelType;

public class ConfigParser {

  public static SimulationConfig getSimulationConfig(String[] args) {
    SimulationConfig config = parseConfig(args);
    if (config == null) {
      config = SimulationConfig.defaultConfig();
    }
    return config;
  }

  private static SimulationConfig parseConfig(String[] args) {
    String fileName = args.length > 0 ? args[0] : null;
    if (fileName == null) {
      return null;
    }
    HashMap<String, String> configMap = getConfigsMap(fileName);
    if (configMap == null) {
      return null;
    }

    SimulationConfig config = getSimulationConfig(configMap);
    return switch (config.getModelType()) {
      case CELLULAR_AUTOMATON, RULE_184 -> config;
      case ACCELERATION_BASED_MODEL -> getAccelerationBasedModelConfig(config, configMap);
    };
  }

  private static SimulationConfig getSimulationConfig(HashMap<String, String> configMap) {
    try {
      SimulationConfig defaultConfig = SimulationConfig.defaultConfig();
      int roadLength = Integer.parseInt(configMap.getOrDefault("roadLength", defaultConfig.getRoadLength() + ""));
      int carCount = Integer.parseInt(configMap.getOrDefault("carCount", defaultConfig.getCarCount() + ""));
      int maxSpeed = Integer.parseInt(configMap.getOrDefault("maxSpeed", defaultConfig.getMaxSpeed() + ""));
      int stepDuration = Integer.parseInt(configMap.getOrDefault("stepDuration", defaultConfig.getStepDuration() + ""));
      float breakingProbability = Float.parseFloat(configMap.getOrDefault("breakingProbability",
                                      defaultConfig.getBreakingProbability() + ""));
      boolean isCyclic = Boolean.parseBoolean(configMap.getOrDefault("isCyclic", defaultConfig.isCyclic() + ""));
      String outputFilePrefix = configMap.getOrDefault("outputFilePrefix", defaultConfig.getOutputFilePrefix());
      int stepCount = Integer.parseInt(configMap.getOrDefault("stepCount", defaultConfig.getStepCount() + ""));
      long randomSeed = Long.parseLong(configMap.getOrDefault("randomSeed", defaultConfig.getRandomSeed() + ""));

      return new SimulationConfig(
                    roadLength,
                    carCount,
                    maxSpeed,
                    stepDuration,
                    breakingProbability,
                    isCyclic,
                    outputFilePrefix,
                    stepCount,
                    randomSeed,
                    ModelType.fromString(configMap.getOrDefault("model", defaultConfig.getModelType().getName()))
      );
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static AccelerationBasedModelConfig getAccelerationBasedModelConfig(SimulationConfig baseConfig,
                                                                            HashMap<String, String> configMap) {
    try {
      AccelerationBasedModelConfig defaultConfig = AccelerationBasedModelConfig.defaultConfig();
      float startAccelerationProbability = Float.parseFloat(configMap.getOrDefault("startAccelerationProbability",
                                      defaultConfig.getStartAccelerationProbability() + ""));
      int lowSpeedThreshold = Integer.parseInt(configMap.getOrDefault("lowSpeedThreshold",
                                      defaultConfig.getLowSpeedThreshold() + ""));
      float lowSpeedThresholdBreakingProbability = Float.parseFloat(configMap.getOrDefault(
                                      "lowSpeedThresholdBreakingProbability",
                                      defaultConfig.getLowSpeedThresholdBreakingProbability() + ""));

      return new AccelerationBasedModelConfig(
                    baseConfig.getRoadLength(),
                    baseConfig.getCarCount(),
                    baseConfig.getMaxSpeed(),
                    baseConfig.getStepDuration(),
                    baseConfig.getBreakingProbability(),
                    baseConfig.isCyclic(),
                    baseConfig.getOutputFilePrefix(),
                    baseConfig.getStepCount(),
                    baseConfig.getRandomSeed(),
                    baseConfig.getModelType(),
                    startAccelerationProbability,
                    lowSpeedThreshold,
                    lowSpeedThresholdBreakingProbability
      );
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static HashMap<String, String> getConfigsMap(String fileName) {
    HashMap<String, String> configMap = new HashMap<>();
    File configFile = new File(fileName);
    if (!configFile.exists()) {
      return null;
    }
    try (Scanner scanner = new Scanner(configFile)) {
      while( scanner.hasNextLine() ) {
        String line = scanner.nextLine();
        String[] parts = line.split("=");
        if (parts.length != 2) {
          continue; // skip invalid lines
        }

        String key = parts[0].trim();
        String value = parts[1].trim();
        configMap.put(key, value);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    return configMap;
  }
}
