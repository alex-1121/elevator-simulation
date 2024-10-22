package main.elevator;

import main.building.Building;
import main.building.Floor;
import main.building.FloorButton;

import java.util.Set;
import java.util.stream.Collectors;

public class ButtonReader {

    public Set<ElevatorButton> detectPressedElevatorButtons(Elevator elevator) {
        synchronized (elevator.getElevatorButtons()) {
            return elevator.getElevatorButtons().stream()
                    .filter(ElevatorButton::isPressed)
                    .collect(Collectors.toSet());
        }
    }

    public Set<FloorButton> detectPressedFloorButtons(Building building) {
        synchronized (building.getFloors()) {
            return building.getFloors().stream()
                    .map(Floor::getButton)
                    .filter(FloorButton::isPressed)
                    .collect(Collectors.toSet());
        }
    }
}
