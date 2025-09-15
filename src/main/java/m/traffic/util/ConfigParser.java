package m.traffic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import m.traffic.core.data.config.AccelerationBasedModelConfig;
import m.traffic.core.data.config.SimulationConfig;
import m.traffic.core.model.type.ModelType;

enum OptionType {
  CAR_NUMBER("car", "deltaCarNumber"), //format: min:max:delta
  ROAD_LENGTH("l", "deltaRoadLength"), //format: min:max:delta
  MAX_SPEED("s", "deltaMaxSpeed"), //format: min:max:delta
  BREAKING_PROBABILITY("b", "deltaBreakingProbability"),
  P0_PROBABILITY("p0", "deltaP0Probability"),
  LOW_SPEED_THRESHOLD("t", "deltaLowSpeedThreshold"),
  LOW_SPEED_THRESHOLD_BREAKING_PROBABILITY("tp", "deltaLowSpeedThresholdBreakingProbability"),
  STEP_COUNT("n", "stepCount"),
  CONFIG_FILE("c", "configFile");

  String shortName;
  String longName;

  OptionType(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }
}

public class ConfigParser {
  private static final Options options = new Options();
  private static final CommandLineParser commandLineParser = new DefaultParser();
  private static final String DELIMITER = ":";

  static {
    options
        .addOption( createOption(OptionType.CAR_NUMBER.shortName,
                                 OptionType.CAR_NUMBER.longName,
                                 OptionType.CAR_NUMBER.name(), "delta car number", false) )
        .addOption( createOption(OptionType.ROAD_LENGTH.shortName,
                                 OptionType.ROAD_LENGTH.longName,
                                 OptionType.ROAD_LENGTH.name(), "delta road length", false) )
        .addOption( createOption(OptionType.MAX_SPEED.shortName,
                                 OptionType.MAX_SPEED.longName,
                                 OptionType.MAX_SPEED.name(), "delta max speed", false) )
        .addOption( createOption(OptionType.BREAKING_PROBABILITY.shortName,
                                 OptionType.BREAKING_PROBABILITY.longName,
                                 OptionType.BREAKING_PROBABILITY.name(), "delta breaking probability", false) )
        .addOption( createOption(OptionType.P0_PROBABILITY.shortName,
                                 OptionType.P0_PROBABILITY.longName,
                                 OptionType.P0_PROBABILITY.name(), "delta initial acceleration probability", false) )
        .addOption( createOption(OptionType.LOW_SPEED_THRESHOLD.shortName,
                                 OptionType.LOW_SPEED_THRESHOLD.longName,
                                 OptionType.LOW_SPEED_THRESHOLD.name(), "delta low speed (t)hreshold", false) )
        .addOption( createOption(OptionType.LOW_SPEED_THRESHOLD_BREAKING_PROBABILITY.shortName,
                                 OptionType.LOW_SPEED_THRESHOLD_BREAKING_PROBABILITY.longName,
                                 OptionType.LOW_SPEED_THRESHOLD_BREAKING_PROBABILITY.name(),
                                 "delta low speed threshold breaking probability", false) )
        .addOption( createOption(OptionType.STEP_COUNT.shortName,
                                 OptionType.STEP_COUNT.longName,
                                 OptionType.STEP_COUNT.name(), "number of steps to simulate", false) )
        .addOption( createOption(OptionType.CONFIG_FILE.shortName,
                                 OptionType.CONFIG_FILE.longName,
                                 OptionType.CONFIG_FILE.name(), "Path to config file", false) );
  }

  public static List<SimulationConfig> getSimulationConfig(String[] args) throws ParseException {
    CommandLine commandLine = commandLineParser.parse(options, args);
    List<SimulationConfig> configs = new ArrayList<>();

    SimulationConfig config = parseConfig(commandLine);
    if (config == null) {
      config = SimulationConfig.defaultConfig();
    }
    for (OptionType optionType : OptionType.values()) {
      switch (optionType) {
        case CAR_NUMBER -> configs.addAll(parseIntConfig(commandLine, optionType, config, SimulationConfig::setCarCount));
        case ROAD_LENGTH -> configs.addAll(parseIntConfig(commandLine, optionType, config, SimulationConfig::setRoadLength));
        case MAX_SPEED -> configs.addAll(parseIntConfig(commandLine, optionType, config, SimulationConfig::setMaxSpeed));
        case BREAKING_PROBABILITY -> configs.addAll(parseFloatConfig(commandLine, optionType, config, SimulationConfig::setBreakingProbability));
        case P0_PROBABILITY -> configs.addAll(parseFloatConfig(commandLine, optionType, config,
          (conf, value) -> {
            if (conf instanceof AccelerationBasedModelConfig abmc) {
              abmc.setStartAccelerationProbability(value);
            }
          }));
        case LOW_SPEED_THRESHOLD -> configs.addAll(parseIntConfig(commandLine, optionType, config,
          (conf, value) -> {
            if (conf instanceof AccelerationBasedModelConfig abmc) {
              abmc.setLowSpeedThreshold(value);
            }
          }));
        case LOW_SPEED_THRESHOLD_BREAKING_PROBABILITY -> configs.addAll(parseFloatConfig(commandLine, optionType, config,
          (conf, value) -> {
            if (conf instanceof AccelerationBasedModelConfig abmc) {
              abmc.setLowSpeedThresholdBreakingProbability(value);
            }
          }));
        case STEP_COUNT -> configs.addAll(parseIntConfig(commandLine, optionType, config, SimulationConfig::setStepCount));
        default -> {
          // No action needed for CONFIG_FILE or unhandled options
        }
      }
    }
    if (configs.isEmpty()) {
      configs.add(config);
    }
    return configs;
  }

  private static List<? extends SimulationConfig> parseIntConfig(CommandLine commandLine, OptionType optionType,
      SimulationConfig config, BiConsumer<SimulationConfig, Integer> setter) {
    List<SimulationConfig> configs = new ArrayList<>();

    if (commandLine.hasOption(optionType.shortName)) {
      String value = commandLine.getOptionValue(optionType.shortName);
      int minValue = Integer.parseInt(getValue(value, 0));
      int maxValue = Integer.parseInt(getValue(value, 1));
      int deltaValue = Integer.parseInt(getValue(value, 2));
      for (int i = minValue; i <= maxValue; i += deltaValue) {
        SimulationConfig newConfig = copyConfig(config);
        setter.accept(newConfig, i);
        configs.add(newConfig);
      }
    }
    return configs;
  }

  private static SimulationConfig copyConfig(SimulationConfig config) {
    return switch (config.getModelType()) {
      case CELLULAR_AUTOMATON, RULE_184 -> SimulationConfig.copyConfig(config);
      case ACCELERATION_BASED_MODEL -> AccelerationBasedModelConfig.copyConfig((AccelerationBasedModelConfig) config);
    };
  }

  private static List<? extends SimulationConfig> parseFloatConfig(CommandLine commandLine, OptionType optionType,
      SimulationConfig config, BiConsumer<SimulationConfig, Float> setter) {
    List<SimulationConfig> configs = new ArrayList<>();

    if (commandLine.hasOption(optionType.shortName)) {
      String value = commandLine.getOptionValue(optionType.shortName);
      float minValue = Float.parseFloat(getValue(value, 0));
      float maxValue = Float.parseFloat(getValue(value, 1));
      float deltaValue = Float.parseFloat(getValue(value, 2));
      for (float i = minValue; i <= maxValue; i += deltaValue) {
        SimulationConfig newConfig = copyConfig(config);
        setter.accept(newConfig, i);
        configs.add(newConfig);
      }
    }
    return configs;
  }

  private static String getValue(String value, int index) {
    String[] parts = value.split(DELIMITER);
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid parameter format, expected min:max:delta");
    }
    return parts[index];
  }

  private static SimulationConfig parseConfig(CommandLine commandLine) {
    String fileName = commandLine.getOptionValue(OptionType.CONFIG_FILE.shortName);
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

  private static Option createOption(String shortName, String longName, String argName, String description, boolean required) {
    return Option.builder(shortName)
      .longOpt(longName)
      .argName(argName)
      .desc(description)
      .hasArg()
      .required(required)
      .get();
  }
}
