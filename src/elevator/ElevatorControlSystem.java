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

        Integer nextDest = findNextDestination();
        if (nextDest.equals(elevator.getCurrentFloorNumber())) {
            return;
        }

        sendDestination(nextDest);
    }

    private Integer findNextDestination() {
        Set<Integer> extremes = Set.of(building.getBottomFloorNumber(), building.getFloorCount());
        boolean elevatorAtExtremes = extremes.contains(elevator.getCurrentFloorNumber());

        if (elevator.isStopped() && elevatorAtExtremes || !moreDestinationsOnTheWay()) {
            toggleElevatorMovementDirection();
        }

        return findNextStopInCurrentDirection();
    }

    private void toggleElevatorMovementDirection() {
        Direction currentDirection = elevator.getMovementDirection();
        if (currentDirection == Direction.UP) {
            elevator.setMovementDirection(Direction.DOWN);
        } else {
            elevator.setMovementDirection(Direction.UP);
        }
    }

    private Integer findNextStopInCurrentDirection() {
        Direction currentDirection = elevator.getMovementDirection();
        Integer currentFloor = elevator.getCurrentFloorNumber();

        if (currentDirection == Direction.UP) {
            return lookUp(currentFloor);
        } else {
            return lookBelow(currentFloor);
        }
    }

    private Integer lookUp(Integer currentFloor) {
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

    private Integer lookBelow(Integer currentFloor) {
        // Find first button pressed below current floor
        return destinationFloorNumbers.stream()
                .filter(floorNumber -> floorNumber < currentFloor)
                .max(Integer::compareTo).orElse(currentFloor);
    }

    private boolean moreDestinationsOnTheWay() {
        Integer currentFloor = elevator.getCurrentFloorNumber();
        if (elevator.getMovementDirection() == Direction.UP) {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber > currentFloor);
        } else {
            return destinationFloorNumbers.stream().anyMatch(floorNumber -> floorNumber < currentFloor);
        }
    }

    private void detectPressedButtons() {
        destinationFloorNumbers.clear();
        detectPressedElevatorButtons();
        detectPressedFloorButtons();
        // logger.logECS("Detected pressed elevator buttons: " + Button.buttonsToString(pressedElevatorButtons));
        // logger.logECS("Detected pressed floor buttons: " + Button.buttonsToString(pressedFloorButtons));
    }

    private void detectPressedElevatorButtons() {
        pressedElevatorButtons.clear();
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
        pressedFloorButtons.clear();
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

    private void sendDestination(Integer destination) {
        logger.logECS("Sending destination: " + destination);
        elevator.setDestinationFloorNumber(destination);
        elevator.wakeUp();
    }

    private Floor findMatchingFloor(Integer floorNumber) {
        return building.getFloors().stream().
                filter(floor -> floor.floorNumber.equals(floorNumber))
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
