package main.building;

import main.button.Button;
import main.Direction;

public class FloorButton extends Button {
    private final Direction direction;

    public FloorButton(int floorNumber, Direction direction) {
        super(floorNumber);
        this.direction = direction;
    }

    public FloorButton(int floorNumber) {
        super(floorNumber);
        this.direction = null;
    }

    public Direction getDirection() {
        return direction;
    }
}
