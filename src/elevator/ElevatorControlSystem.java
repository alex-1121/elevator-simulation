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
        detectPressedButtons();

        if (destinationFloors.isEmpty()) {
            return;
        }

        // TODO refactor
        LinkedList<Integer> destinationFloorNumbers = destinationFloors.stream()
                .map(floor -> floor.floorNumber)
                .collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

        destinationFloors.clear();
        sendNewDestinations(destinationFloorNumbers);
    }

    private void detectPressedButtons() {
        detectPressedElevatorButtons();
        detectPressedFloorButtons();
    }

    private void detectPressedElevatorButtons() {
        synchronized (elevator.getElevatorButtons()) {
            elevator.getElevatorButtons().stream().filter(ElevatorButton::isPressed).forEach(pressedButton -> {
                Floor matchingFloor = findMatchingFloor(pressedButton.floorNumber);
                if (!destinationFloors.contains(matchingFloor)) {
                    destinationFloors.push(matchingFloor);
                }
            });
        }
    }

    private void detectPressedFloorButtons() {
        synchronized (building.getFloors()) {
            building.getFloors().stream().filter(floor -> floor.getButton().isPressed()).forEach(floor -> {
                if (!destinationFloors.contains(floor)) {
                    destinationFloors.push(floor);
                }
            });
        }
    }

    private void sendNewDestinations(LinkedList<Integer> newDestinations) {
        logger.logECS("Sending new destinations: " + newDestinations.toString());
        elevator.addDestinations(newDestinations);
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
