from config.config_base import Config, ConfigOptionMap
from config.model_type import ModelType


def parse_config_from_filename(original_config: Config, file_name: str) -> Config:
  # ab_L=1000_N=200_Vmax=5_p=0.33p0=0.70_LST=2_pLST=0.20
  import copy
  config = copy.deepcopy(original_config)

  parts = file_name.split("_")
  for part in parts:
    if "=" not in part: continue
    key, value = part.split("=")
    key = key.strip()
    value = value.strip()
    if key == ConfigOptionMap.ROAD_LENGTH:
      config.roadLength = int(value)
    elif key == ConfigOptionMap.CAR_COUNT:
      config.carCount = int(value)
    elif key == ConfigOptionMap.MAX_SPEED:
      config.maxSpeed = int(value)
    elif key == ConfigOptionMap.BRAKING_PROBABILITY:
      config.brakingProbability = float(value)
    elif key == ConfigOptionMap.P0_PROBABILITY:
      config.startAccelerationProbability = float(value)
    elif key == ConfigOptionMap.LOW_SPEED_THRESHOLD:
      config.lowSpeedThreshold = int(value)
    elif key == ConfigOptionMap.P_LOW_SPEED_THRESHOLD:
      config.lowSpeedThresholdBrakingProbability = float(value)
    else:
      raise ValueError(f"Unknown config key: {key} in file name: {file_name}")

  return config

def read_default_config_from_file(config_path: str) -> Config:
  config = Config()
  with open(config_path, "r") as file:
    lines = file.readlines()
    for line in lines:
      # for property find the first '=' character and split by it
      separator_index = line.find("=")
      if separator_index == -1: raise ValueError(f"Invalid property line: {line}")
      key, value = line[:separator_index], line[separator_index + 1:]
      config.setField(key.strip(), value.strip())
  return config

def read_config(config_path: str) -> list[Config]:
  configs = list()
  # walk through each file under dir name
  # parse each file name to get config params
  import os
  default_config = read_default_config_from_file(config_path)

  dir_name = "/".join(default_config.outputFilePrefix.split("/")[:-1])

  for simulation_result_dir_str in os.listdir(dir_name):
    # dir_name += "/" + build_dir_name_from_params(default_config)
    simulation_result_dir = os.path.join(dir_name, simulation_result_dir_str)
    if not os.path.isdir(simulation_result_dir):
      continue
    configs.append(parse_config_from_filename(default_config, simulation_result_dir_str))
    print(f"Parsed config from dir name: {simulation_result_dir_str} -> {configs[-1]}")
  return configs

def build_dir_name_from_params(config: Config):
  prefix = config.outputFilePrefix.split("/")[-1]
  dir_name = f"{prefix}_L={config.roadLength}_N={config.carCount}_Vmax={config.maxSpeed}_p={config.brakingProbability:.2f}"
  if config.modelType == ModelType.ACCELERATION_BASED_MODEL:
      dir_name += f"p0={config.startAccelerationProbability:.2f}_LST={config.lowSpeedThreshold}_pLST={config.lowSpeedThresholdBrakingProbability:.2f}"
  return dir_name