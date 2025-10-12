from config.config_base import Config, ConfigOptionMap
import argparse

def get_args(param: str = None) -> argparse.Namespace:
  parser = argparse.ArgumentParser()
  
  # primary filtering arguments
  parser.add_argument("-l", "--road-length", type=str, help="Road length (50:200:10)")
  parser.add_argument("-n", "--car-count", type=str, help="Car count (10:100:10)")
  parser.add_argument("-v", "--max-speed", type=str, help="Max speed (1:10:1)")
  parser.add_argument("-p", "--braking-probability", type=str, help="Braking probability (0.0:1.0:0.1)")
  parser.add_argument("-s", "--start-acceleration-probability", type=str, help="Start acceleration probability (0.0:1.0:0.1)")
  parser.add_argument("-t", "--low-speed-threshold", type=str, help="Low speed threshold (1:10:1)")
  parser.add_argument("-q", "--low-speed-threshold-braking-probability", type=str, help="Low speed threshold braking probability (0.0:1.0:0.1)")
  
  # Secondary filtering arguments
  parser.add_argument("--select-road-length", type=str, help="Select road length (50:200:10)")
  parser.add_argument("--select-car-count", type=str, help="Select car count (10:100:10)")
  parser.add_argument("--select-max-speed", type=str, help="Select max speed (1:10:1)")
  parser.add_argument("--select-braking-probability", type=str, help="Select braking probability (0.0:1.0:0.1)")
  parser.add_argument("--select-start-acceleration-probability", type=str, help="Select start acceleration probability (0.0:1.0:0.1)")
  parser.add_argument("--select-low-speed-threshold", type=str, help="Select low speed threshold (1:10:1)")
  parser.add_argument("--select-low-speed-threshold-braking-probability", type=str, help="Select low speed threshold braking probability (0.0:1.0:0.1)")
  
# configuration arguments
  parser.add_argument("-c", "--config-file", type=str, required=True, help="Path to config file")
  parser.add_argument("-i", "--step-count", type=int, help="Number of steps to simulate (overrides config file)")

  if param:
    args = parser.parse_args(param.split())
  else:
    args = parser.parse_args()
  return args

def parse_range_value(range_str: str) -> list[float | int]:
  """Parse range string like '2:100:2' or '0.0:1.0:0.1' into list of values"""
  if ':' not in range_str:
    # Single value
    return [float(range_str) if '.' in range_str else int(range_str)]
  
  parts = range_str.split(':')
  if len(parts) != 3:
    raise ValueError(f"Range format should be 'start:end:step', got: {range_str}")
  
  start, end, step = float(parts[0]), float(parts[1]), float(parts[2])
  values = []
  current = start
  while current <= end:
    if step < 1:
      values.append(round(current, 10))
    else:
      values.append(int(current) if current == int(current) else current)
    current += step
  
  return values

def parse_primary_filters(args) -> dict[ConfigOptionMap, list[float | int]]:
  # parse primary filtering arguments (--from section)
  filters = {}
  
  if args.road_length:
    filters[ConfigOptionMap.ROAD_LENGTH] = parse_range_value(args.road_length)
  if args.car_count:
    filters[ConfigOptionMap.CAR_COUNT] = parse_range_value(args.car_count)
  if args.max_speed:
    filters[ConfigOptionMap.MAX_SPEED] = parse_range_value(args.max_speed)
  if args.braking_probability:
    filters[ConfigOptionMap.BRAKING_PROBABILITY] = parse_range_value(args.braking_probability)
  if args.start_acceleration_probability:
    filters[ConfigOptionMap.START_ACCELERATION_PROBABILITY] = parse_range_value(args.start_acceleration_probability)
  if args.low_speed_threshold:
    filters[ConfigOptionMap.LOW_SPEED_THRESHOLD] = parse_range_value(args.low_speed_threshold)
  if args.low_speed_threshold_braking_probability:
    filters[ConfigOptionMap.LOW_SPEED_THRESHOLD_BRAKING_PROBABILITY] = parse_range_value(args.low_speed_threshold_braking_probability)
    
  return filters

def parse_secondary_filters(args) -> dict[ConfigOptionMap, list[float | int]]:
  # parse secondary filtering arguments (--select section)
  filters = {}
  
  if args.select_road_length:
    filters[ConfigOptionMap.ROAD_LENGTH] = parse_range_value(args.select_road_length)
  if args.select_car_count:
    filters[ConfigOptionMap.CAR_COUNT] = parse_range_value(args.select_car_count)
  if args.select_max_speed:
    filters[ConfigOptionMap.MAX_SPEED] = parse_range_value(args.select_max_speed)
  if args.select_braking_probability:
    filters[ConfigOptionMap.BRAKING_PROBABILITY] = parse_range_value(args.select_braking_probability)
  if args.select_start_acceleration_probability:
    filters[ConfigOptionMap.START_ACCELERATION_PROBABILITY] = parse_range_value(args.select_start_acceleration_probability)
  if args.select_low_speed_threshold:
    filters[ConfigOptionMap.LOW_SPEED_THRESHOLD] = parse_range_value(args.select_low_speed_threshold)
  if args.select_low_speed_threshold_braking_probability:
    filters[ConfigOptionMap.LOW_SPEED_THRESHOLD_BRAKING_PROBABILITY] = parse_range_value(args.select_low_speed_threshold_braking_probability)
    
  return filters

def filter_configs_by_criteria(configs: list[Config], filters: dict[ConfigOptionMap, list[float | int]]) -> list[Config]:
  filtered_configs = []
  
  for config in configs:
    matches = True
    
    for option_map, allowed_values in filters.items():
      config_value = None
      
      if option_map == ConfigOptionMap.ROAD_LENGTH:
        config_value = config.roadLength
      elif option_map == ConfigOptionMap.CAR_COUNT:
        config_value = config.carCount
      elif option_map == ConfigOptionMap.MAX_SPEED:
        config_value = config.maxSpeed
      elif option_map == ConfigOptionMap.BRAKING_PROBABILITY:
        config_value = config.brakingProbability
      elif option_map == ConfigOptionMap.START_ACCELERATION_PROBABILITY:
        config_value = config.startAccelerationProbability
      elif option_map == ConfigOptionMap.LOW_SPEED_THRESHOLD:
        config_value = config.lowSpeedThreshold
      elif option_map == ConfigOptionMap.LOW_SPEED_THRESHOLD_BRAKING_PROBABILITY:
        config_value = config.lowSpeedThresholdBrakingProbability
      
      # check if config value matches any of the allowed values
      value_matches = False
      for allowed_value in allowed_values:
        if isinstance(allowed_value, (int, float)) and isinstance(config_value, (int, float)):
          if abs(config_value - allowed_value) < 1e-9:
            value_matches = True
            break
        elif config_value == allowed_value:
          value_matches = True
          break
      
      if not value_matches:
        matches = False
        break
    
    if matches:
      filtered_configs.append(config)
  
  return filtered_configs

def parse_and_filter_configs(configs: list[Config], args) -> list[Config]:
  # apply two-stage filtering based on parsed arguments
  filtered_configs = configs
  
  primary_filters = parse_primary_filters(args)
  if primary_filters:
    print(f"Applying primary filters: {primary_filters}")
    filtered_configs = filter_configs_by_criteria(filtered_configs, primary_filters)
    print(f"After primary filtering: {len(filtered_configs)} configs remaining")
  
  secondary_filters = parse_secondary_filters(args)
  if secondary_filters:
    print(f"Applying secondary filters: {secondary_filters}")
    filtered_configs = filter_configs_by_criteria(filtered_configs, secondary_filters)
    print(f"After secondary filtering: {len(filtered_configs)} configs remaining")
  
  return filtered_configs
