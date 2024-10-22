package main.elevator;

import main.Direction;

import java.util.concurrent.atomic.AtomicReference;

public class ElevatorDirection {
    private final AtomicReference<Direction> direction;

    public ElevatorDirection(Direction direction) {
        this.direction = new AtomicReference<>(direction);
    }

    public Direction getDirection() {
        return direction.get();
    }

    public void setDirection(Direction direction) {
        this.direction.set(direction);
    }

    @Override
    public String toString() {
        return direction.get().toString();
    }
}

