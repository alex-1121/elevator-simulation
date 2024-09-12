package elevator;

import customLogger.CustomLogger;
import building.Building;
import direction.Direction;
import building.Floor;
import building.Passenger;
import button.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Elevator implements Runnable {

    private final CustomLogger logger;

    private boolean shouldRun = true;
    private final Object lock = new Object();

    private final Building building;
    private final int capacity;
    private final ArrayList<Passenger> passengers = new ArrayList<>();
    private static final int TIME_TO_MOVE_BETWEEN_FLOORS = 500;

    private final Collection<ElevatorButton> elevatorButtons = Collections.synchronizedCollection(new ArrayList<>());
    private boolean isMoving = false;
    private int currentFloorNumber;
    private int destinationFloorNumber;
    private Direction movementDirection;

    public Elevator(int capacity, Building building, int currentFloorNumber, CustomLogger logger) {
        this.logger = logger;
        this.building = building;
        this.capacity = capacity;
        this.currentFloorNumber = currentFloorNumber;
        this.destinationFloorNumber = currentFloorNumber;
        synchronized (elevatorButtons) {
            building.getFloors().forEach(floor -> elevatorButtons.add(new ElevatorButton(floor.floorNumber)));
        }

        logger.logElevator("Starting at floor " + currentFloorNumber + "/" + building.getFloors().size());
    }

    private void goToDestinationFloor() {
        logger.logElevator("Moving to destination floor: " + this.destinationFloorNumber);
        while (!atDestination()) {
            isMoving = true;
            makeStep();
        }
        isMoving = false;
        releaseButtons();
        loadAndUnloadPassengers();
    }

    private void makeStep() {
        simulateElevatorMovingTime();
        if (currentFloorNumber < destinationFloorNumber) {
            currentFloorNumber++;
            movementDirection = Direction.UP;
        } else {
            currentFloorNumber--;
            movementDirection = Direction.DOWN;
        }
        logger.logElevator("Moving " + movementDirection + ", " + currentFloorNumber + "/" + building.getFloors().size());
    }

    private void releaseButtons() {
        synchronized (elevatorButtons) {
            elevatorButtons.stream()
                    .filter(button -> button.floorNumber == currentFloorNumber)
                    .findFirst()
                    .ifPresent(Button::release);
        }
        synchronized (building.getFloors()) {
            building.getFloors().stream()
                    .filter(floor -> floor.floorNumber == currentFloorNumber && floor.button.isPressed())
                    .findFirst()
                    .ifPresent(floor -> {
                        floor.button.release();
                        logger.logElevator("Released floor button on floor " + floor.floorNumber);
                    });
        }
    }

    private void loadAndUnloadPassengers() {
        Floor currentFloor = this.building.getFloorByNumber(this.currentFloorNumber);
        unloadPassengers(currentFloor);
        loadPassengers(currentFloor);
    }

    private void unloadPassengers(Floor currentFloor) {
        ArrayList<Passenger> passengersToUnload = new ArrayList<>();
        this.passengers.stream()
                .filter(passenger -> passenger.destinationFloorNumber == currentFloor.floorNumber)
                .forEach(passengersToUnload::add);
        if (passengersToUnload.isEmpty()) {
            return;
        }
        logger.logElevator(passengersToUnload.size() + " passengers arrived at their destination floor " + destinationFloorNumber);
        passengers.removeAll(passengersToUnload);
    }

    private void loadPassengers(Floor currentFloor) {
        int newPassengers = 0;
        synchronized (currentFloor.getWaitingPassengers()) {
            Collection<Passenger> waitingPassengers = currentFloor.getWaitingPassengers();
            ArrayList<Passenger> passengersToLoad = new ArrayList<>();
            for (Passenger passenger : waitingPassengers) {
                if (this.passengers.size() >= this.capacity) {
                    break;
                }
                passengersToLoad.add(passenger);
                newPassengers++;
                pressElevatorButton(passenger.destinationFloorNumber);
            }
            waitingPassengers.removeAll(passengersToLoad);
            this.passengers.addAll(passengersToLoad);
        }
        logger.logElevator("New passengers: " + newPassengers + ", total passengers: " + this.passengers.size() + "/" + capacity);
    }

    private void pressElevatorButton(int destinationFloorNumber) {
        elevatorButtons.stream()
                .filter(button -> button.floorNumber == destinationFloorNumber)
                .findFirst().ifPresent(Button::press);
    }

    private void simulateElevatorMovingTime() {
        try {
            Thread.sleep(TIME_TO_MOVE_BETWEEN_FLOORS);
        } catch (InterruptedException e) {
            logger.logError(e);
        }
    }

    public boolean atDestination() {
        return currentFloorNumber == destinationFloorNumber;
    }

    public synchronized Collection<ElevatorButton> getElevatorButtons() {
        return this.elevatorButtons;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public int getCurrentFloorNumber() {
        return currentFloorNumber;
    }

    public int getDestinationFloorNumber() {
        return destinationFloorNumber;
    }

    public void setDestinationFloorNumber(int destinationFloorNumber) {
        this.destinationFloorNumber = destinationFloorNumber;
    }


    private void waitIfNeeded() {
        while (atDestination()) {
            synchronized (lock) {
                try {
                    logger.logElevator("Waiting for calls");
                    lock.wait();
                } catch (InterruptedException e) {
                    logger.logError(e);
                }
            }
        }
    }

    public void wakeUp() {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void run() {
        logger.logElevator("Started");
        while (shouldRun) {
            waitIfNeeded();
            goToDestinationFloor();
        }
        logger.logElevator("Stopped");
    }

    public void stopThread() {
        shouldRun = false;
    }
}
