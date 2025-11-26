import matplotlib.pyplot as plt
from matplotlib.ticker import MultipleLocator
import numpy as np

from config.config_base import Config, ConfigOptionMap

SAVE_DIR = "plots"

def draw_space_time_diagram(position, velocity, time, output_prefix, params: Config) -> None:

  figure, (ax1, ax2) = plt.subplots(1, 2)
  figure.set_size_inches(16, 8)

  figure.suptitle(f"Параметри конфігурації:\n{params.get_short_description()}")
  # src_start = 8000 # to get sample of vehicles' positions in later iterations
  # src_end_inclusive = 9000
  # position = position[src_start:src_end_inclusive+1].copy()
  # time = time[src_start:src_end_inclusive+1].copy()
  # velocity = velocity[src_start:src_end_inclusive+1].copy()
  # extents_position = get_position_extents_2(position, src_start, src_end_inclusive)
  # extents_velocity = get_position_extents_2(velocity, src_start, src_end_inclusive)

  extents_position = get_position_extents(position, time)
  extents_velocity = get_position_extents(velocity, time)
  im1 = ax1.imshow(velocity, extent=extents_velocity, origin='lower', aspect='auto', cmap='coolwarm')
  plt.colorbar(im1, ax=ax1, orientation='vertical')
  ax1.set_title("Швидкість ТЗ")
  ax1.set_xlabel("Поздовжня позиція")
  ax1.set_ylabel("Час")

  im2 = ax2.imshow(position, extent=extents_position, origin='lower', aspect='auto')
  plt.colorbar(im2, ax=ax2, orientation='vertical')
  ax2.set_title("Поздовжня позиція ТЗ")
  ax2.set_xlabel("Поздовжня позиція")
  ax2.set_ylabel("Час")

  plt.tight_layout()
  save_plot_as_png(plt, output_prefix, "space-time-diagram")

# number of iteration vs global flow. TODO: Can be combined for multiple configs
def iterations_global_flow(time: np.ndarray, flow: np.ndarray, config: Config, skip_fraction: float = 0.001) -> None:
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Параметри конфігурації:\n{config.get_short_description()}")

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
  plt.title("Глобальний потік по ітераціях")
  plt.xlabel("Ітерація")
  plt.ylabel("Глобальний потік")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "iterations-global-flow")

def get_position_extents_2(position: np.ndarray, start, end) -> tuple:
  x1 = 0
  x2 = position.shape[1] - 0
  t1 = start
  t2 = end
  extents = [x1, x2, t1, t2]
  return extents

def get_position_extents(position: np.ndarray, time: np.ndarray) -> tuple:
  x1 = 0
  x2 = position.shape[1] - 0
  t1 = 0
  t2 = time.shape[0] - 1
  extents = [x1, x2, t1, t2]
  return extents

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
  print(f"Графік збережено у {os.path.join(dir_to_save, file_name)}.png")
  plt.close()

# this diagram can be created only when we have varying density (N changing)
# density is collection of last density values from each iteration; 
# average_speed is collection of last average speed values from each iteration
def density_average_speed(density: np.ndarray, average_speed: np.ndarray, config: Config, varying_param: list[ConfigOptionMap]) -> None:
  """ density vs average speed diagram """
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Параметри конфігурації:\n{config.get_short_description(varying_param)}")

  ax = plt.gca()
  ax.xaxis.set_major_locator(MultipleLocator(0.2))
  ax.xaxis.set_minor_locator(MultipleLocator(0.1))
  ax.yaxis.set_major_locator(MultipleLocator(1.0))
  ax.yaxis.set_minor_locator(MultipleLocator(0.5))
  plt.grid(True, linestyle='--', linewidth=0.5, alpha=0.7, which='both')
  plt.scatter(density, average_speed, c='blue', s=3)
  plt.plot(density, average_speed, color='red', linewidth=1)  # draw line thru points
  plt.title("Щільність - середня швидкість")
  plt.xlabel("Щільність")
  plt.ylabel("Середня швидкість")
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
  figure.suptitle(f"Параметри конфігурації:\n{config.get_short_description(deltas)}")
  
  ax = plt.gca()
  ax.xaxis.set_major_locator(MultipleLocator(0.2))
  ax.xaxis.set_minor_locator(MultipleLocator(0.1))
  ax.yaxis.set_major_locator(MultipleLocator(1.0))
  ax.yaxis.set_minor_locator(MultipleLocator(0.5))
  plt.grid(True, linestyle='--', linewidth=0.5, alpha=0.7, which='both')

  for i in range(len(densities)):
    label = f"{ConfigOptionMap.from_field_name(varying_param[i][0])}={varying_param[i][1]}"
    plt.plot(densities[i], average_speeds[i], linewidth=1, label=label)
  plt.legend()
  plt.title("Щільність - середня швидкість")
  plt.xlabel("Щільність")
  plt.ylabel("Середня швидкість")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-average-speed-all-in-one")

def write_flow_capacity_stats(flows: np.ndarray, config: Config, configs_with_stats_items: list[set[Config, set]], velocities: np.ndarray) -> None:
  max_flow = np.max(flows)
  capacity_index = np.argmax(flows)
  capacity_flow = max_flow

  max_velocity = np.max(velocities)
  max_velocity_index = np.argmax(velocities)

  with open(f"{config.outputFilePrefix}/flow-capacity-stats.txt", "w") as f:
    f.write(f"Максимальний потік: {max_flow}\n")
    f.write(f"Потік пропускної здатності: {capacity_flow} на індексі {capacity_index}\n")
    f.write(f"Конфігурація при потоці пропускної здатності:\n")
    config_at_capacity = configs_with_stats_items[capacity_index][0]
    f.write(config_at_capacity.get_short_description())

    f.write(f"\nМаксимальна швидкість: {max_velocity} на індексі {max_velocity_index}\n")
    f.write(f"Конфігурація при максимальній швидкості:\n")
    config_at_max_velocity = configs_with_stats_items[max_velocity_index][0]
    f.write(config_at_max_velocity.get_short_description())
    
  print(f"Статистика пропускної здатності потоку збережена у {config.outputFilePrefix}/flow-capacity-stats.txt")
  pass

def density_flow(density: np.ndarray, flow: np.ndarray, config: Config, varying_param: list[ConfigOptionMap]) -> None:
  """ density vs flow diagram """
  figure = plt.figure()
  figure.set_size_inches(8, 6)
  figure.suptitle(f"Параметри конфігурації:\n{config.get_short_description(varying_param)}")

  ax = plt.gca()
  ax.xaxis.set_major_locator(MultipleLocator(0.2))
  ax.xaxis.set_minor_locator(MultipleLocator(0.1))
  plt.grid(True, linestyle='--', linewidth=0.5, alpha=0.7, which='both')

  plt.scatter(density, flow, c='blue', s=3)
  plt.plot(density, flow, color='red', linewidth=1)
  plt.title("Щільність - потік")
  plt.xlabel("Щільність")
  plt.ylabel("Потік")
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
  figure.suptitle(f"Параметри конфігурації:\n{config.get_short_description(deltas)}")

  ax = plt.gca()
  ax.xaxis.set_major_locator(MultipleLocator(0.2))
  ax.xaxis.set_minor_locator(MultipleLocator(0.1))
  plt.grid(True, linestyle='--', linewidth=0.5, alpha=0.7, which='both')

  for i in range(len(densities)):
    label = f"{ConfigOptionMap.from_field_name(varying_param[i][0])}={varying_param[i][1]}"
    plt.plot(densities[i], flows[i], linewidth=1, label=label)
  plt.legend()
  plt.title("Щільність - потік")
  plt.xlabel("Щільність")
  plt.ylabel("Потік")
  plt.tight_layout()
  save_plot_as_png(plt, config.outputFilePrefix, "density-flow-all-in-one")
