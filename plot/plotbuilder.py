import numpy as np

from config.config_utils import read_config
from config.config_base import Config
from stats.stats_reader import read_stats
from draw.plot_utils import draw_space_time_diagram, flow_density, time_global_flow, density_average_speed

def draw_separate_plots(configs_with_stats: dict[Config, set], delta_param: str) -> None:

  densities_per_config = []
  average_speeds_per_config = []
  flows_per_config = []
  # per config plots
  for config, stats in configs_with_stats.items():
    position, velocity, time, average_speed, density, flow = stats
    draw_space_time_diagram(position, velocity, time, config.outputFilePrefix, config)
    time_global_flow(time, flow, config)

    densities_per_config.append(density[-1])
    average_speeds_per_config.append(average_speed[-1])
    flows_per_config.append(flow[-1])

  # plots based on delta rho (that is varying density)
  if delta_param == 'roadLength' or delta_param == 'carCount':
    density_average_speed(np.array(densities_per_config), np.array(average_speeds_per_config), config)
    flow_density(np.array(flows_per_config), np.array(densities_per_config), config)

def draw_combined_plots(configs_with_stats: dict[Config, set], delta: str) -> None:
  # TODO: draw combined plots based on varying parameter delta
  # e.g. if delta is brakingProbability, then draw average speed vs brakingProbability for all configs
  # from plot.draw.plot_utils import draw_space_time_diagram
  # for config, stats in configs_with_stats.items():
  #   position, velocity, time, average_speed, density, flow = stats
  #   draw_space_time_diagram(position, velocity, time, config.outputFilePrefix)
  pass

def determine_varying_parameter(configs: list) -> str:
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
  return varying_param

def main(args) -> int:
  # plan:
  # 1. read config file (done)
  # 2. parse configs based on files names inside outputFilePrefix dir (done)
  # 3. determine varying (param that changes) parameter based on configs (done)
  # 4. read stats files (done)
  # 5. draw all plots
  #   a. separate plots for each config
  #   b. combined plots for all configs based on varying parameter
  # 6. save plots to outputFilePrefix + "/plots/" + "_plots.png"
  config_file_path = args[1] if len(args) > 1 else "./src/main/resources/acceleration-based-config"
  configs = read_config(config_file_path)
  configs.sort()
  print(configs)

  delta = determine_varying_parameter(configs)
  print(f"Varying parameter: {delta}")

  configs_with_stats = dict[Config, set]()
  for config in configs:
    position, velocity, time, average_speed, density, flow = read_stats(config.outputFilePrefix)
    configs_with_stats[config] = (position, velocity, time, average_speed, density, flow)

  draw_separate_plots(configs_with_stats, delta)
  # draw_combined_plots(configs_with_stats, delta)
  pass

if __name__ == '__main__':
  import sys
  main(sys.argv)
