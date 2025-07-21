import numpy as np

KEYS_TO_IGNORE = {"outputFilePrefix", "isCyclic", "stepDuration"}

def read_config(file_name: str) -> dict:
  configs = dict()

  with open(file_name, "r") as file:
    lines = file.readlines()
    for line in lines:
      key, value = line.split("=")
      configs[key.strip()] = value.strip()
  return configs

def get_extends(position: np.ndarray, time: np.ndarray) -> tuple:
  x1 = 0
  x2 = position.shape[0] - 1
  t1 = 0
  t2 = time.shape[0] - 1
  extents = [x1, x2, t1, t2]

def read_stats(file_name_prefix: str):
  position_file = open(f"./{file_name_prefix}_position", "r")
  velocity_file = open(f"./{file_name_prefix}_velocity", "r")
  time_file = open(f"./{file_name_prefix}_time", "r")

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

def main() -> int:
  config_file_path = "./src/main/resources/config"
  configs = read_config(config_file_path)
  print(configs)

  position, velocity, time = read_stats(configs["outputFilePrefix"])

  draw_plots(position, velocity, time, configs)
  pass

if __name__ == '__main__':
  main()
