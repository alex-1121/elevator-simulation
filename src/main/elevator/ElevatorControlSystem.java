package main.elevator;

import main.Stoppable;
import main.building.FloorButton;
import main.button.Button;
import main.customLogger.CustomLogger;
import main.building.Building;
import main.Direction;

import java.util.*;

public class ElevatorControlSystem implements Stoppable {

    private static final long POLLING_DELAY = 100;

    private final CustomLogger logger;

    private volatile boolean shouldRun = true;

    private final Elevator elevator;
    private final Building building;

    private final ButtonReader buttonReader = new ButtonReader();
    private final DestinationFinder destinationFinder = new DestinationFinder();

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
        getDestinations();
        if (destinationFloorNumbers.isEmpty()) {
            return;
        }

        if (!destinationFinder.isMoreDestinationsOnTheWay(elevator, destinationFloorNumbers)) {
            toggleElevatorMovementDirection();
        }

        Optional<Integer> nextDestination;
        if (elevator.getMovementDirection() == Direction.UP) {
            nextDestination = destinationFinder.lookUp(elevator, pressedElevatorButtons, destinationFloorNumbers);
        } else {
            nextDestination = destinationFinder.lookBelow(elevator, destinationFloorNumbers);
        }

        nextDestination.ifPresent(this::sendDestination);
    }

    private void toggleElevatorMovementDirection() {
        Direction currentDirection = elevator.getMovementDirection();
        if (currentDirection == Direction.UP) {
            elevator.setMovementDirection(Direction.DOWN);
        } else {
            elevator.setMovementDirection(Direction.UP);
        }
    }

    private void detectPressedButtons() {
        pressedElevatorButtons.clear();
        pressedElevatorButtons.addAll(buttonReader.detectPressedElevatorButtons(elevator));

        pressedFloorButtons.clear();
        pressedFloorButtons.addAll(buttonReader.detectPressedFloorButtons(building));
    }

    private void getDestinations() {
        destinationFloorNumbers.clear();
        destinationFloorNumbers.addAll(destinationFinder.getDestinationFloorNumbers(pressedElevatorButtons, pressedFloorButtons));
    }

    private void sendDestination(Integer destination) {
        if (destination.equals(elevator.getDestinationFloorNumber()) && elevator.isNotSleeping()) {
            return;
        }

        logger.logECS("Sending destination: " + destination);
        elevator.setDestinationFloorNumber(destination);
        elevator.wakeUp();
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

    @Override
    public void stop() {
        shouldRun = false;
    }
}
