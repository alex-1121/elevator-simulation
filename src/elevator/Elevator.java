package elevator;

import customLogger.CustomLogger;
import building.Building;
import building.Direction;
import building.Floor;
import building.Passenger;
import button.Button;

import java.util.ArrayList;

public class Elevator implements Runnable {

    private final CustomLogger logger;

    private boolean shouldRun = true;
    private final Object lock = new Object();

    private final Building building;
    private final int capacity;
    private final ArrayList<Passenger> passengers = new ArrayList<>();
    private static final int MOVEMENT_BETWEEN_FLOORS = 500;

    private final ArrayList<ElevatorButton> elevatorButtons = new ArrayList<>();
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
        building.getFloors().forEach(floor -> elevatorButtons.add(new ElevatorButton(floor.floorNumber)));

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
        elevatorButtons.stream()
                .filter(button -> button.floorNumber == currentFloorNumber)
                .findFirst()
                .ifPresent(Button::release);

        building.getFloors().stream()
                .filter(floor -> floor.floorNumber == currentFloorNumber)
                .findFirst()
                .ifPresent(floor -> {
                    floor.button.release();
                    logger.logElevator("Released floor button on floor " + floor.floorNumber);
                });
    }

    private void loadAndUnloadPassengers() {
        Floor currentFloor = this.building.floors.get(this.currentFloorNumber - 1);
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
        ArrayList<Passenger> waitingPassengers = currentFloor.getWaitingPassengers();
        int newPassengers = 0;
        while (!waitingPassengers.isEmpty() && this.passengers.size() < this.capacity) {
            Passenger passenger = waitingPassengers.removeFirst();
            this.passengers.add(passenger);
            newPassengers++;

            pressElevatorButton(passenger.destinationFloorNumber);
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
            Thread.sleep(MOVEMENT_BETWEEN_FLOORS);
        } catch (InterruptedException e) {
            logger.logError(e);
        }
    }

    public boolean atDestination() {
        return currentFloorNumber == destinationFloorNumber;
    }

    public synchronized ArrayList<ElevatorButton> getElevatorButtons() {
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
