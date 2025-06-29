package m.traffic.core.data.simulation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cell {
  private final int position;
  private Item item;

  public Cell(int position) {
    this.position = position;
  }
}
