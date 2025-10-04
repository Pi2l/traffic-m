from enum import StrEnum

from config.model_type import ModelType


class Config:
  def __init__(self):
    self.roadLength = 0
    self.carCount = 0
    self.maxSpeed = 0
    self.stepDuration = 0
    self.brakingProbability = 0.0
    self.isCyclic = True
    self.outputFilePrefix = ""
    self.stepCount = 0
    self.randomSeed = 0
    self.modelType = ModelType.CELLULAR_AUTOMATON
    self.startAccelerationProbability = 0.0 # p0
    self.lowSpeedThreshold = 0
    self.lowSpeedThresholdBrakingProbability = 0.0

  def setField(self, key: str, value: str):
    if key == "roadLength":
      self.roadLength = int(value)
    elif key == "carCount":
      self.carCount = int(value)
    elif key == "maxSpeed":
      self.maxSpeed = int(value)
    elif key == "stepDuration":
      self.stepDuration = int(value)
    elif key == "brakingProbability":
      self.brakingProbability = float(value)
    elif key == "isCyclic":
      self.isCyclic = value.lower() in ("true", "1", "yes")
    elif key == "outputFilePrefix":
      self.outputFilePrefix = value
    elif key == "stepCount":
      self.stepCount = int(value)
    elif key == "randomSeed":
      self.randomSeed = int(value)
    elif key == "model":
      self.modelType = ModelType(value)
    elif key == "startAccelerationProbability":
      self.startAccelerationProbability = float(value)
    elif key == "lowSpeedThreshold":
      self.lowSpeedThreshold = int(value)
    elif key == "lowSpeedThresholdBrakingProbability":
      self.lowSpeedThresholdBrakingProbability = float(value)
      
    else:
      raise ValueError(f"Unknown config key: {key}")

  def __str__(self):
    return (f"Config(roadLength={self.roadLength}, carCount={self.carCount}, maxSpeed={self.maxSpeed}, "
            f"stepDuration={self.stepDuration}, brakingProbability={self.brakingProbability}, isCyclic={self.isCyclic}, "
            f"outputFilePrefix='{self.outputFilePrefix}', stepCount={self.stepCount}, randomSeed={self.randomSeed}, "
            f"modelType={self.modelType}, startAccelerationProbability={self.startAccelerationProbability}, "
            f"lowSpeedThreshold={self.lowSpeedThreshold}, lowSpeedThresholdBrakingProbability={self.lowSpeedThresholdBrakingProbability})")

  # operator to compare two configs that whould be used for sorting
  def __lt__(self, other):
    if not isinstance(other, Config):
      return NotImplemented

    if self.roadLength != other.roadLength:
      return self.roadLength < other.roadLength
    if self.carCount != other.carCount:
      return self.carCount < other.carCount
    if self.maxSpeed != other.maxSpeed:
      return self.maxSpeed < other.maxSpeed
    if self.brakingProbability != other.brakingProbability:
      return self.brakingProbability < other.brakingProbability
    if self.startAccelerationProbability != other.startAccelerationProbability:
      return self.startAccelerationProbability < other.startAccelerationProbability
    if self.lowSpeedThreshold != other.lowSpeedThreshold:
      return self.lowSpeedThreshold < other.lowSpeedThreshold
    if self.lowSpeedThresholdBrakingProbability != other.lowSpeedThresholdBrakingProbability:
      return self.lowSpeedThresholdBrakingProbability < other.lowSpeedThresholdBrakingProbability
    return False

class ConfigOptionMap(StrEnum):
  # ab_L=1000_N=200_Vmax=5_p=0.33p0=0.70_LST=2_pLST=0.20
  ROAD_LENGTH = "L"
  CAR_COUNT = "N"
  MAX_SPEED = "Vmax"
  BRAKING_PROBABILITY = "p"
  P0_PROBABILITY = "p0"
  LOW_SPEED_THRESHOLD = "LST"
  P_LOW_SPEED_THRESHOLD = "pLST"