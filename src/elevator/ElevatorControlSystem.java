package elevator;

import building.FloorButton;
import button.Button;
import customLogger.CustomLogger;
import building.Building;
import building.Floor;
import direction.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class ElevatorControlSystem implements Runnable {

    private final CustomLogger logger;

    private static final long POLLING_DELAY = 100;
    private boolean shouldRun = true;

    private final Elevator elevator;
    private final Building building;

    private final Set<Integer> destinationFloorNumbers = new TreeSet<>();
    private final Set<FloorButton> pressedFloorButtons = new TreeSet<>(Button.buttonComparator);
    private final Set<ElevatorButton> pressedElevatorButtons = new TreeSet<>(Button.buttonComparator);

    public ElevatorControlSystem(Elevator elevator, Building building, CustomLogger logger) {
        this.logger = logger;
        this.elevator = elevator;
        this.building = building;
    }

    private void handleElevatorCalls() {
        detectPressedButtons();

        int nextDest = findNextDestination();
        if (nextDest == elevator.getCurrentFloorNumber() || nextDest == elevator.getDestinationFloorNumber()) {
            return;
        }

        sendDestination(nextDest);
    }

    private int findNextStopInCurrentDirection() {
        Direction currentDirection = elevator.getMovementDirection();
        int currentFloor = elevator.getCurrentFloorNumber();

        if (currentDirection == Direction.UP) {
            return lookUp(currentFloor);
        } else {
            return lookBelow(currentFloor);
        }
    }

    private int lookUp(int currentFloor) {
        // If any elevator buttons pressed - find first pressed elevator button above current floor
        // Else find the highest floor button pressed
        if (!pressedElevatorButtons.isEmpty()) {
            return pressedElevatorButtons.stream()
                    .filter(button -> button.getFloorNumber() > currentFloor)
                    .min(Button.buttonComparator).orElse(new ElevatorButton(currentFloor)).getFloorNumber();
        } else {
            return destinationFloorNumbers.stream()
                    .filter(floorNumber -> floorNumber > currentFloor)
                    .max(Integer::compareTo).orElse(currentFloor);
        }
    }

    private int lookBelow(int currentFloor) {
        // Find first button pressed below current floor
        return destinationFloorNumbers.stream()
                .filter(floorNumber -> floorNumber < currentFloor)
                .max(Integer::compareTo).orElse(currentFloor);
    }

    private boolean moreDestinationsOnTheWay() {
        int currentFloor = elevator.getCurrentFloorNumber();
        if (elevator.getMovementDirection() == Direction.UP) {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber > currentFloor);
        } else {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber < currentFloor);
        }
    }

    private int findNextDestination() {
        int currentFloor = elevator.getCurrentFloorNumber();
        Set<Integer> extremes = Set.of(1, building.getFloors().size());

        if (!elevator.isMoving() && extremes.contains(currentFloor) || !moreDestinationsOnTheWay()) {
            toggleElevatorMovementDirection();
        }

        return findNextStopInCurrentDirection();
    }

    private void detectPressedButtons() {
        detectPressedElevatorButtons();
        detectPressedFloorButtons();
        // logger.logECS("Detected pressed elevator buttons: " + Button.buttonsToString(pressedElevatorButtons));
        // logger.logECS("Detected pressed floor buttons: " + Button.buttonsToString(pressedFloorButtons));
    }

    private void detectPressedElevatorButtons() {
        synchronized (elevator.getElevatorButtons()) {
            elevator.getElevatorButtons().stream()
                    .filter(ElevatorButton::isPressed)
                    .collect(Collectors.toCollection(() -> pressedElevatorButtons));
            destinationFloorNumbers.addAll(pressedElevatorButtons.stream()
                    .map(ElevatorButton::getFloorNumber)
                    .collect(Collectors.toSet()));
        }
    }

    private void detectPressedFloorButtons() {
        synchronized (building.getFloors()) {
            building.getFloors().stream()
                    .map(Floor::getButton)
                    .filter(FloorButton::isPressed)
                    .collect(Collectors.toCollection(() -> pressedFloorButtons));
            destinationFloorNumbers.addAll(pressedFloorButtons.stream()
                    .map(FloorButton::getFloorNumber)
                    .collect(Collectors.toSet()));
        }
    }

    private void sendDestination(int destination) {
        logger.logECS("Sending destination: " + destination);
        elevator.addDestinations(new TreeSet<>(Collections.singleton(destination)));
        destinationFloorNumbers.clear();
        elevator.wakeUp();
    }

    private void toggleElevatorMovementDirection() {
        Direction currentDirection = elevator.getMovementDirection();
        if (currentDirection == Direction.UP) {
            elevator.setMovementDirection(Direction.DOWN);
        } else {
            elevator.setMovementDirection(Direction.UP);
        }
    }

    private Floor findMatchingFloor(int floorNumber) {
        return building.getFloors().stream().
                filter(floor -> floor.floorNumber == floorNumber)
                .findFirst().orElseThrow();
    }

    private void executePollingDelay() {
        try {
            Thread.sleep(POLLING_DELAY);
        } catch (InterruptedException e) {
            logger.logError(e);
        }
    }

    @Override
    public void run() {
        logger.logECS("Started");
        while (shouldRun) {
            executePollingDelay();
            handleElevatorCalls();
        }
        logger.logECS("Stopped");
    }

    public void stopThread() {
        shouldRun = false;
    }
}
