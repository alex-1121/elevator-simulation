package elevator;

import customLogger.CustomLogger;
import building.Building;
import building.Floor;

import java.util.LinkedList;

public class ElevatorControlSystem implements Runnable {

    private final CustomLogger logger;

    private static final long POLLING_DELAY = 100;
    private boolean shouldRun = true;

    private final Elevator elevator;
    private final Building building;
    private final LinkedList<Floor> destinationFloors = new LinkedList<>();

    public ElevatorControlSystem(Elevator elevator, Building building, CustomLogger logger) {
        this.logger = logger;
        this.elevator = elevator;
        this.building = building;
    }

    private void handleElevatorCalls() {
        if (elevator.atDestination()) {
            destinationFloors.stream()
                    .filter(floor -> floor.floorNumber == elevator.getCurrentFloorNumber())
                    .findFirst().ifPresent(destinationFloors::remove);
        }
        detectPressedButtons();

        if (destinationFloors.isEmpty()) {
            return;
        }

        // TODO make it possible to interrupt moving elevator
        if (elevator.isMoving()) {
            return;
        }

        setNewDestination(destinationFloors);
    }

    private void detectPressedButtons() {
        detectPressedElevatorButtons();
        detectPressedFloorButtons();
    }

    private void detectPressedElevatorButtons() {
        elevator.getElevatorButtons().stream()
                .filter(ElevatorButton::isPressed)
                .forEach(pressedElevatorButton -> {
                    Floor matchingFloor = findMatchingFloor(pressedElevatorButton.floorNumber);
                    if (!destinationFloors.contains(matchingFloor)) {
                        destinationFloors.push(matchingFloor);
                        logger.logECS("Pressed elevator button detected " + pressedElevatorButton.floorNumber);
                    }
                });
    }

    private void detectPressedFloorButtons() {
        building.getFloors().stream().filter(floor -> floor.button.isPressed()).forEach(floor -> {
            if (!destinationFloors.contains(floor)) {
                destinationFloors.push(floor);
                logger.logECS("Pressed floor button detected on floor " + floor.floorNumber);
            }
        });
    }

    private void setNewDestination(LinkedList<Floor> destinationFloors) {
        elevator.setDestinationFloorNumber(destinationFloors.getFirst().floorNumber);
        elevator.wakeUp();
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
