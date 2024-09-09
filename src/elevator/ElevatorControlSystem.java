package elevator;

import building.Building;
import building.Floor;

import java.util.LinkedList;

public class ElevatorControlSystem {
    private final Elevator elevator;
    private final Building building;

    public ElevatorControlSystem(Elevator elevator, Building building) {
        this.elevator = elevator;
        this.building = building;
    }

    public void handleElevatorCalls() {
        LinkedList<Floor> destinationFloors = new LinkedList<>();
        detectPressedButtons(destinationFloors);
        if (destinationFloors.isEmpty()) {
            System.out.println("Elevator idling");
            return;
        }
        moveToDestinationFloors(destinationFloors);
    }

    private void detectPressedButtons(LinkedList<Floor> destinationFloors) {
        detectPressedElevatorButtons(destinationFloors);
        detectPressedFloorButtons(destinationFloors);
    }

    private void detectPressedElevatorButtons(LinkedList<Floor> destinationFloors) {
        elevator.elevatorButtons.stream().filter(ElevatorButton::isPressed).forEach(pressedElevatorButton -> {
            Floor matchingFloor = findMatchingFloor(pressedElevatorButton.floorNumber);
            destinationFloors.push(matchingFloor);
            System.out.println("pressed elevator button detected " + pressedElevatorButton.floorNumber);
        });
    }

    private void detectPressedFloorButtons(LinkedList<Floor> destinationFloors) {
        building.floors.stream().filter(floor -> floor.button.isPressed()).forEach(floor -> {
            destinationFloors.push(floor);
            System.out.println("pressed floor button detected on floor " + floor.floorNumber);
        });
    }

    private void moveToDestinationFloors(LinkedList<Floor> destinationFloors) {
        destinationFloors.forEach(floor -> {
            int floorNumber = floor.floorNumber;
            elevator.goToFloor(floorNumber);
            floor.button.release();
            elevator.loadAndUnloadPassengers();
        });
    }

    private Floor findMatchingFloor(int floorNumber) {
        return building.floors.stream().filter(floor -> floor.floorNumber == floorNumber).findFirst().orElseThrow();
    }
}
