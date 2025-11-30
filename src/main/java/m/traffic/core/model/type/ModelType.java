package m.traffic.core.model.type;

public enum ModelType {
  NAGEL_SCHRECKENBERG("Nagel_Schreckenberg"),
  RULE_184("rule184"),
  VELOCITY_BASED_MODEL("velocity_based_model");

  private final String name;

  ModelType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static ModelType fromString(String name) {
    for (ModelType type : ModelType.values()) {
      if (type.name.equalsIgnoreCase(name)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Невідомий тип моделі: " + name);
  }
}
