package m.traffic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import m.traffic.core.data.config.SimulationConfig;

public class ConfigParser {

  public static SimulationConfig getSimulationConfig( String[] args ) {
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
    try {
      SimulationConfig defaultConfig = SimulationConfig.defaultConfig();
      int roadLength = Integer.parseInt(configMap.getOrDefault("roadLength", defaultConfig.roadLength() + ""));
      int carCount = Integer.parseInt(configMap.getOrDefault("carCount", defaultConfig.carCount() + ""));
      int maxSpeed = Integer.parseInt(configMap.getOrDefault("maxSpeed", defaultConfig.maxSpeed() + ""));
      int stepDuration = Integer.parseInt(configMap.getOrDefault("stepDuration", defaultConfig.stepDuration() + ""));
      float breakingProbability = Float.parseFloat(configMap.getOrDefault("breakingProbability", defaultConfig.breakingProbability() + ""));

      return new SimulationConfig(roadLength, carCount, maxSpeed, stepDuration, breakingProbability);
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
