package m.traffic.core.model.factory;

import m.traffic.core.model.VelocityBasedModel;
import m.traffic.core.model.NagelSchreckenbergModel;
import m.traffic.core.model.Rule184;
import m.traffic.core.model.TrafficModel;
import m.traffic.core.model.type.ModelType;

public class ModelFactory {

  public static TrafficModel createModel(String modelType) {
    return createModel(ModelType.fromString(modelType));
  }

  public static TrafficModel createModel(ModelType modelType) {
    switch (modelType) {
      case NAGEL_SCHRECKENBERG:
        return new NagelSchreckenbergModel();
      case RULE_184:
        return new Rule184();
      case VELOCITY_BASED_MODEL:
        return new VelocityBasedModel();
      default:
        throw new IllegalArgumentException("Невідомий тип моделі: " + modelType);
    }
  }
}
