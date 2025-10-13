import matplotlib.pyplot as plt
import numpy as np

from config.config_base import Config, ConfigOptionMap

SAVE_DIR = "plots"

def draw_space_time_diagram(position, velocity, time, output_prefix, params: Config) -> None:

  figure, (ax1, ax2) = plt.subplots(1, 2)
  figure.set_size_inches(16, 8)

  figure.suptitle(f"Config params:\n{params.get_short_description()}")
  extents_position = get_extents(position, time)

  extents_velocity = get_extents(velocity, time)
  im1 = ax1.imshow(velocity, extent=extents_velocity, origin='lower', aspect='auto', cmap='coolwarm')
  plt.colorbar(im1, ax=ax1, orientation='vertical')
  ax1.set_title("Vehicle velocity")
  ax1.set_xlabel("Position")
  ax1.set_ylabel("Time")

  im2 = ax2.imshow(position, extent=extents_position, origin='lower', aspect='auto')
  plt.colorbar(im2, ax=ax2, orientation='vertical')
  ax2.set_title("Vehicle position")
  ax2.set_xlabel("Position")
  ax2.set_ylabel("Time")

  plt.tight_layout()
  save_plot_as_png(plt, output_prefix, "space-time-diagram")

# number of iteration vs global flow. TODO: Can be combined for multiple configs
def iterations_global_flow(time: np.ndarray, flow: np.ndarray, config: Config, skip_fraction: float = 0.001) -> None:
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Config params:\n{config.get_short_description()}")

  time_copy = np.copy(time)
  flow_copy = np.copy(flow)

  # skip initialisation phase: first iteration
  time_copy = time_copy[int(len(time_copy) * skip_fraction):]
  flow_copy = flow_copy[int(len(flow_copy) * skip_fraction):]

  time_vs_flow = np.stack((time_copy, flow_copy))
  # skip 0 at lower and 99 at upper percentiles
  lower, upper = np.percentile(flow_copy, [0, 99])

  plt.scatter(time_vs_flow[0], time_vs_flow[1], c='blue', s=1)
  plt.ylim(lower, upper)
  plt.title("Global flow over iterations")
  plt.xlabel("Iterations")
  plt.ylabel("Global flow")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "iterations-global-flow")

def get_extents(position: np.ndarray, time: np.ndarray) -> tuple:
  x1 = 0
  x2 = position.shape[0] - 1
  t1 = 0
  t2 = time.shape[0] - 1
  extents = [x1, x2, t1, t2]
  return extents

def save_plot_as_png(plt, output_prefix: str, file_name: str, dpi: int = 300) -> None:
  import os

  dir_to_save = os.path.join(output_prefix, SAVE_DIR)
  if not os.path.exists(dir_to_save):
    os.makedirs(dir_to_save)

  plt.savefig(os.path.join(dir_to_save, file_name) + '.png', dpi=dpi)
  print(f"Plot saved to {os.path.join(dir_to_save, file_name)}.png")
  plt.close()

# this diagram can be created only when we have varying density (N changing)
# density is collection of last density values from each iteration; 
# average_speed is collection of last average speed values from each iteration
def density_average_speed(density: np.ndarray, average_speed: np.ndarray, config: Config, varying_param: ConfigOptionMap) -> None:
  """ density vs average speed diagram """
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Config params:\n{config.get_short_description([varying_param])}")

  plt.scatter(density, average_speed, c='blue', s=3)
  plt.plot(density, average_speed, color='red', linewidth=1)  # draw line thru points
  plt.title("Density vs Average Speed")
  plt.xlabel("Density")
  plt.ylabel("Average Speed")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-average-speed")

def density_average_speed_all_in_one(densities: np.ndarray,
                                     average_speeds: np.ndarray,
                                     config: Config,
                                     varying_param: list[tuple[str, int | float]],
                                     delta: ConfigOptionMap
                                    ) -> None:
  """ densities vs average speeds diagram for all configs in one plot"""
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  deltas = [ConfigOptionMap.from_field_name(varying_param[0][0])]
  if delta not in deltas:
    deltas.append(delta)
  figure.suptitle(f"Config params:\n{config.get_short_description(deltas)}")

  for i in range(len(densities)):
    plt.plot(densities[i], average_speeds[i], linewidth=1, label=f"{varying_param[i][0]}={varying_param[i][1]}")
  plt.legend()
  plt.title("Densities vs Average Speeds (all in one)")
  plt.xlabel("Density")
  plt.ylabel("Average Speed")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-average-speed-all-in-one")

def density_flow(density: np.ndarray, flow: np.ndarray, config: Config, varying_param: ConfigOptionMap) -> None:
  """ density vs flow diagram """
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Config params:\n{config.get_short_description([varying_param])}")

  plt.scatter(density, flow, c='blue', s=3)
  plt.plot(density, flow, color='red', linewidth=1)
  plt.title("Density vs Flow")
  plt.xlabel("Density")
  plt.ylabel("Flow")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-flow")

def density_flow_all_in_one(densities: np.ndarray,
                            flows: np.ndarray,
                            config: Config,
                            varying_param: list[tuple[str, int | float]],
                            delta: ConfigOptionMap
                          ) -> None:
  """ density vs flow diagram for all configs in one plot """
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  deltas = [ConfigOptionMap.from_field_name(varying_param[0][0])]
  if delta not in deltas:
    deltas.append(delta)
  figure.suptitle(f"Config params:\n{config.get_short_description(deltas)}")

  for i in range(len(densities)):
    plt.plot(densities[i], flows[i], linewidth=1, label=f"{varying_param[i][0]}={varying_param[i][1]}")
  plt.legend()
  plt.title("Densities vs Flows (all in one)")
  plt.xlabel("Density")
  plt.ylabel("Flow")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-flow-all-in-one")
