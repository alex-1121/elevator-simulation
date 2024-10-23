package main.elevator;

import main.Direction;
import main.building.FloorButton;
import main.button.Button;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DestinationFinder {

    public boolean isMoreDestinationsOnTheWay(Elevator elevator, Set<Integer> destinationFloorNumbers) {
        Integer currentFloor = elevator.getCurrentFloorNumber();
        if (elevator.getMovementDirection() == Direction.UP) {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber > currentFloor);
        } else {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber < currentFloor);
        }
    }

    // If any elevator buttons pressed - find first pressed elevator button above current floor
    // Else find the highest floor button pressed
    public Optional<Integer> lookUp(Elevator elevator, Set<ElevatorButton> pressedElevatorButtons, Set<Integer> destinationFloorNumbers) {
        Optional<Integer> nextDestination = pressedElevatorButtons.stream()
                .filter(button -> button.getFloorNumber() >= elevator.getCurrentFloorNumber())
                .min(Button.buttonComparator)
                .map(Button::getFloorNumber);


        return nextDestination.isPresent() ? nextDestination :
                destinationFloorNumbers.stream()
                        .filter(floorNumber -> floorNumber >= elevator.getCurrentFloorNumber())
                        .max(Integer::compareTo);
    }

    // Find first button pressed below current floor
    public Optional<Integer> lookBelow(Elevator elevator, Set<Integer> destinationFloorNumbers) {
        return destinationFloorNumbers.stream()
                .filter(floorNumber -> floorNumber <= elevator.getCurrentFloorNumber())
                .max(Integer::compareTo);
    }

    public Set<Integer> getDestinationFloorNumbers(Set<ElevatorButton> pressedElevatorButtons, Set<FloorButton> pressedFloorButtons) {
        return Stream.concat(
                pressedFloorButtons.stream().map(Button::getFloorNumber),
                pressedElevatorButtons.stream().map(Button::getFloorNumber)
        ).collect(Collectors.toSet());
    }
}
