package building;

import button.Button;
import direction.Direction;

public class FloorButton extends Button {
    private final Direction direction;

    public FloorButton(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
