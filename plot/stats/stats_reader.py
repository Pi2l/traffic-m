import numpy as np

def read_stats(file_name_prefix: str):
  position_file = open(f"{file_name_prefix}/position", "r")
  velocity_file = open(f"{file_name_prefix}/velocity", "r")
  time_file = open(f"{file_name_prefix}/time", "r")
  average_speed_file = open(f"{file_name_prefix}/average_speed", "r")
  density_file = open(f"{file_name_prefix}/density", "r")
  flow_file = open(f"{file_name_prefix}/flow", "r")

  position = np.loadtxt(position_file, dtype=int)
  velocity = np.loadtxt(velocity_file, dtype=int)
  time = np.loadtxt(time_file, dtype=int)
  average_speed = np.loadtxt(average_speed_file, dtype=float)
  density = np.loadtxt(density_file, dtype=float)
  flow = np.loadtxt(flow_file, dtype=float)

  return position, velocity, time, average_speed, density, flow