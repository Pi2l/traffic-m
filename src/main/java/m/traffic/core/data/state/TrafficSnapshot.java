package m.traffic.core.data.state;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import m.traffic.core.data.simulation.Cell;
import m.traffic.core.data.simulation.Vehicle;

@Getter
@Setter
public class TrafficSnapshot {

  private List<Cell> road;
  private List<Vehicle> cars;
  private int stepDuration;

}
