package m.traffic.core.model.factory;

import m.traffic.core.model.AccelerationBasedModel;
import m.traffic.core.model.CellularAutomatonModel;
import m.traffic.core.model.Rule184;
import m.traffic.core.model.TrafficModel;
import m.traffic.core.model.type.ModelType;

public class ModelFactory {

  public static TrafficModel createModel(String modelType) {
    return createModel(ModelType.fromString(modelType));
  }

  public static TrafficModel createModel(ModelType modelType) {
    switch (modelType) {
      case CELLULAR_AUTOMATON:
        return new CellularAutomatonModel();
      case RULE_184:
        return new Rule184();
      case ACCELERATION_BASED_MODEL:
        return new AccelerationBasedModel();
      default:
        throw new IllegalArgumentException("Unknown model type: " + modelType);
    }
  }
}
