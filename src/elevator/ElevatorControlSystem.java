package elevator;

import building.FloorButton;
import customLogger.CustomLogger;
import building.Building;
import building.Floor;

import java.util.Set;
import java.util.TreeSet;

public class ElevatorControlSystem implements Runnable {

    private final CustomLogger logger;

    private static final long POLLING_DELAY = 100;
    private boolean shouldRun = true;

    private final Elevator elevator;
    private final Building building;
    private final Set<Integer> destinationFloorNumbers = new TreeSet<>();

    public ElevatorControlSystem(Elevator elevator, Building building, CustomLogger logger) {
        this.logger = logger;
        this.elevator = elevator;
        this.building = building;
    }

    private void handleElevatorCalls() {
        detectPressedButtons();

        if (destinationFloorNumbers.isEmpty()) {
            return;
        }

        sendDestinations();
    }

    private void detectPressedButtons() {
        detectPressedElevatorButtons();
        detectPressedFloorButtons();
    }

    private void detectPressedElevatorButtons() {
        synchronized (elevator.getElevatorButtons()) {
            elevator.getElevatorButtons().stream()
                    .filter(ElevatorButton::isPressed)
                    .forEach(pressedButton -> destinationFloorNumbers.add(pressedButton.floorNumber));
        }
    }

    private void detectPressedFloorButtons() {
        synchronized (building.getFloors()) {
            building.getFloors().stream()
                    .map(Floor::getButton)
                    .filter(FloorButton::isPressed)
                    .forEach(pressedButton -> destinationFloorNumbers.add(pressedButton.floorNumber));
        }
    }

    private void sendDestinations() {
        logger.logECS("Sending destinations: " + destinationFloorNumbers);
        elevator.addDestinations(destinationFloorNumbers);
        destinationFloorNumbers.clear();
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
