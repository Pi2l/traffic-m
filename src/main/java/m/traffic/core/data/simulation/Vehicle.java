package m.traffic.core.data.simulation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vehicle extends Item {
  int roadPosition;
  int velocity;

  public Vehicle(int roadPosition, int velocity) {
    this.roadPosition = roadPosition;
    this.velocity = velocity;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vehicle vehicle)) {
      return false;
    }
    return roadPosition == vehicle.roadPosition && velocity == vehicle.velocity;
  }
}
