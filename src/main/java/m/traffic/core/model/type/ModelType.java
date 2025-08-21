package m.traffic.core.model.type;

public enum ModelType {
  CELLULAR_AUTOMATON("Nagel_Schreckenberg"),
  RULE_184("rule184");

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
    throw new IllegalArgumentException("Unknown model type: " + name);
  }
}
