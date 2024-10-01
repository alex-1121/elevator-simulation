package elevator;

import direction.Direction;

public class ElevatorDirection {
    private Direction direction;

    public ElevatorDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return direction.toString();
    }
}

