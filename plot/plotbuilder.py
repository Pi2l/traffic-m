import argparse
import copy
import numpy as np
import threading
from concurrent.futures import ThreadPoolExecutor
from multiprocessing.pool import ThreadPool

from config.args_parser import get_args, parse_and_filter_configs, parse_secondary_filters
from config.config_utils import read_all_configs
from config.config_base import Config, ConfigOptionMap
from stats.stats_reader import read_stats
from draw.plot_utils import density_average_speed_all_in_one, density_flow_all_in_one, draw_space_time_diagram, density_flow, iterations_global_flow, density_average_speed

def draw_separate_plots(configs_with_stats: dict[Config, set], default_config: Config, delta_param: list[ConfigOptionMap]) -> None:

  densities_per_config = []
  average_speeds_per_config = []
  flows_per_config = []
  # per config plots
  for config, stats in configs_with_stats.items():
    position, velocity, time, average_speed, density, flow = stats
    draw_space_time_diagram(position, velocity, time, config.outputFilePrefix, config)
    iterations_global_flow(time, flow, config)

    densities_per_config.append(density[-1])
    average_speeds_per_config.append(average_speed[-1])
    flows_per_config.append(flow[-1])

  # plots based on delta rho (that is varying density)
  if ConfigOptionMap.ROAD_LENGTH in delta_param or ConfigOptionMap.CAR_COUNT in delta_param:
    density_average_speed(np.array(densities_per_config), np.array(average_speeds_per_config), default_config, delta_param)
    density_flow(np.array(densities_per_config), np.array(flows_per_config), default_config, delta_param)

def draw_combined_plots(grouped_configs: dict[tuple[str, int | float], list[Config]],
                        default_config: Config,
                        configs_with_stats: dict[Config, set]) -> None:

  densities_per_group = []
  average_speeds_per_group = []
  flows_per_group = []
  deltas_names_per_group: set[str] = set()
  deltas_per_group: list[tuple[str, int | float]] = []
  delta = None

  for group_key, group_configs in grouped_configs.items():
    group_configs_with_stats = {config: configs_with_stats[config] for config in group_configs}
    delta = determine_varying_parameter(group_configs)
    if delta not in [ConfigOptionMap.ROAD_LENGTH, ConfigOptionMap.CAR_COUNT]:
      print("Combined plots cannot be drawn when delta is not ROAD_LENGTH or CAR_COUNT")
      continue
    
    densities_per_config = []
    average_speeds_per_config = []
    flows_per_config = []
    for _, stats in group_configs_with_stats.items():
      _, _, _, average_speed, density, flow = stats

      densities_per_config.append(density[-1])
      average_speeds_per_config.append(average_speed[-1])
      flows_per_config.append(flow[-1])

    densities_per_group.append(densities_per_config)
    average_speeds_per_group.append(average_speeds_per_config)
    flows_per_group.append(flows_per_config)
    deltas_names_per_group.add(group_key[0])
    deltas_per_group.append(group_key)
  
  if len(deltas_names_per_group) > 1:
    print("Combined plots cannot be drawn when more than one delta is present")
    return

  config = copy.deepcopy(default_config)
  config.outputFilePrefix += f"_{delta}"
  for group_key, _ in grouped_configs.items():
    config.outputFilePrefix += f"_{group_key[0]}={group_key[1]}"

  density_average_speed_all_in_one(np.array(densities_per_group),
                                   np.array(average_speeds_per_group),
                                   config,
                                   deltas_per_group,
                                   delta)
  density_flow_all_in_one(np.array(densities_per_group),
                          np.array(flows_per_group),
                          config,
                          deltas_per_group,
                          delta)
  pass

def determine_varying_parameter(configs: list[Config]) -> ConfigOptionMap | None:
  # only one parameter should vary
  # if configs length is 1 -> so no varying parameter
  if len(configs) == 1:
    return None

  # find the parameter that varies
  varying_param = None
  for key in configs[0].__dict__.keys():
    if key in Config.KEYS_TO_IGNORE:
      continue
    values = [getattr(c, key) for c in configs]
    if len(set(values)) > 1:
      if varying_param is not None:
        raise ValueError("More than one parameter varies")
      varying_param = key
  if varying_param is None:
    raise ValueError("No varying parameter found")
  varying_param = ConfigOptionMap.from_field_name(varying_param)
  return varying_param

def get_grouped_configs(configs_with_stats: dict[Config, set], args: argparse.Namespace) -> dict[tuple[str, int | float], list[Config]]:
  # group configs by --select parameters
  grouped_configs = dict[tuple[str, int | float], list[Config]]()
  select_filters = parse_secondary_filters(args)
  select_keys = list(select_filters.keys())
  
  for config in configs_with_stats.keys():
    group_key = []
    for key in select_keys:
      field_name = key.to_field_name(key)
      group_key.append(field_name)
      group_key.append(getattr(config, field_name))
    group_key = tuple(group_key)
    if group_key not in grouped_configs:
      grouped_configs[group_key] = []
    grouped_configs[group_key].append(config)

  return grouped_configs

def get_global_plot_dir(default_config: Config) -> Config:
  config = copy.deepcopy(default_config)
  prefix = config.outputFilePrefix.split("/")[-1] # prefix
  global_plots_dir = "/".join(config.outputFilePrefix.split("/")[:-1]) + "/global-plots"
  config.outputFilePrefix = f"{global_plots_dir}/{prefix}"
  return config

def main() -> int:
  # 0:1:0.01
  # plan:
  # 1. read config file (done)
  # 2. parse configs based on files names inside outputFilePrefix dir (done)
  # 3. determine varying (param that changes) parameter based on configs (done)
  # 4. read stats files (done)
  # 5. draw all plots
  #   a. separate plots for each config
  #   b. combined plots for all configs based on varying parameter
  # 6. save plots to outputFilePrefix + "/plots/" + "_plots.png"

  args = get_args()
  configs, default_config = read_all_configs(args.config_file)
  configs.sort()
  print(f"Total configs found: {len(configs)}")
  for config in configs:
    print(config)

  filtered_configs = parse_and_filter_configs(configs, args)
  print(f"Final filtered configs: {len(filtered_configs)}")

  if not filtered_configs:
    print("No configs match the specified criteria!")
    return 1

  configs_with_stats = dict[Config, set]()
  for config in filtered_configs:
    position, velocity, time, average_speed, density, flow = read_stats(config.outputFilePrefix)
    configs_with_stats[config] = (position, velocity, time, average_speed, density, flow)

  grouped_configs = get_grouped_configs(configs_with_stats, args)
  with ThreadPoolExecutor(max_workers=7) as executor:
    futures = [executor.submit(compute_stats_for_config, group_key, group_configs, default_config, configs_with_stats)
                for group_key, group_configs in grouped_configs.items()]

    for f in futures:
      print(f.result())

  config = get_global_plot_dir(default_config)
  draw_combined_plots(grouped_configs, config, configs_with_stats)
  pass

def compute_stats_for_config(group_key, group_configs, default_config, configs_with_stats):
  print(f"Thread: {threading.current_thread().name}")
    
  group_configs_with_stats = {config: configs_with_stats[config] for config in group_configs}
  delta = determine_varying_parameter(group_configs)
  print(f"Drawing plots for group {group_key} with delta {delta}")

  default_config_per_group = get_global_plot_dir(default_config)
  default_config_per_group.outputFilePrefix += f"_{delta}_{group_key[0]}={str(group_key[1])}"  # append first key=value to outputFilePrefix
  deltas = {ConfigOptionMap.from_field_name(group_key[0]), delta}
  draw_separate_plots(group_configs_with_stats, default_config_per_group, list(deltas))
  return f"Completed plots for group {group_key}"

if __name__ == '__main__':
  import sys
  sys.exit(main())
