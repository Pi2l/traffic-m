import matplotlib.pyplot as plt
import numpy as np

from config.config_base import Config

SAVE_DIR = "plots"

def draw_space_time_diagram(position, velocity, time, output_prefix, params: Config) -> None:

  figure, (ax1, ax2) = plt.subplots(1, 2)
  figure.set_size_inches(16, 8)

  figure.suptitle(f"Config params:\n{params.get_short_description()}")
  extends_position = get_extends(position, time)

  extends_velocity = get_extends(velocity, time)
  im1 = ax1.imshow(velocity, extent=extends_velocity, origin='lower', aspect='auto', cmap='coolwarm')
  plt.colorbar(im1, ax=ax1, orientation='vertical')
  ax1.set_title("Vehicle velocity")
  ax1.set_xlabel("Position")
  ax1.set_ylabel("Time")

  im2 = ax2.imshow(position, extent=extends_position, origin='lower', aspect='auto')
  plt.colorbar(im2, ax=ax2, orientation='vertical')
  ax2.set_title("Vehicle position")
  ax2.set_xlabel("Position")
  ax2.set_ylabel("Time")

  plt.tight_layout()
  save_plot_as_png(plt, output_prefix, "space-time-diagram")

def get_extends(position: np.ndarray, time: np.ndarray) -> tuple:
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