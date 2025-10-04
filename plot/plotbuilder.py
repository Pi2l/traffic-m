import numpy as np

from config.config_utils import read_config

KEYS_TO_IGNORE = {"outputFilePrefix", "isCyclic", "stepDuration"}


def get_extends(position: np.ndarray, time: np.ndarray) -> tuple:
  x1 = 0
  x2 = position.shape[0] - 1
  t1 = 0
  t2 = time.shape[0] - 1
  extents = [x1, x2, t1, t2]

def read_stats(file_name_prefix: str):
  position_file = open(f"./{file_name_prefix}/position", "r")
  velocity_file = open(f"./{file_name_prefix}/velocity", "r")
  time_file = open(f"./{file_name_prefix}/time", "r")

  position = np.loadtxt(position_file, dtype=int)
  velocity = np.loadtxt(velocity_file, dtype=int)
  time = np.loadtxt(time_file, dtype=int)
  print("Position shape:", np.shape(position))
  print("Velocity shape:", np.shape(velocity))
  print("Time shape:", np.shape(time))

  return position, velocity, time

def draw_plots(position: np.ndarray, velocity: np.ndarray, time: np.ndarray, params: dict = {}):
  import matplotlib.pyplot as plt
  output_prefix = params['outputFilePrefix']

  figure, (ax1, ax2) = plt.subplots(1, 2)
  figure.set_size_inches(16, 8)
  params = {k: v for k, v in params.items() if k not in KEYS_TO_IGNORE}
  params = ", ".join([f"{k} = {v}" for k, v in params.items()])

  figure.suptitle(f"Config params:\n{params}")
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
  plt.savefig(output_prefix + "_plots.png", dpi=300)

def determine_varying_parameter(configs: list) -> str:
  # only one parameter should vary
  # if configs length is 1 -> so no varying parameter
  if len(configs) == 1:
    return None

  # find the parameter that varies
  varying_param = None
  for key in configs[0].__dict__.keys():
    if key in KEYS_TO_IGNORE:
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
  # 4. read stats files
  # 5. draw all plots
  # 6. save plots to outputFilePrefix + "/plots/" + "_plots.png"
  config_file_path = args[1] if len(args) > 1 else "./src/main/resources/acceleration-based-config"
  configs = read_config(config_file_path)
  configs.sort()
  print(configs)

  delta = determine_varying_parameter(configs)
  print(f"Varying parameter: {delta}")
# determine varying parameter as delta

  # position, velocity, time = read_stats(configs["outputFilePrefix"])

  # draw_plots(position, velocity, time, configs)
  pass

if __name__ == '__main__':
  import sys
  main(sys.argv)
