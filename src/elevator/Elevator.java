package elevator;

import customLogger.CustomLogger;
import building.Building;
import direction.Direction;
import building.Floor;
import passenger.Passenger;
import button.Button;

import java.util.*;

public class Elevator implements Runnable {

    private final CustomLogger logger;

    private boolean shouldRun = true;
    private final Object sleepLock = new Object();
    private boolean isSleeping = false;

    private final Building building;
    private final Integer capacity;
    private final ArrayList<Passenger> passengers = new ArrayList<>();
    private static final Integer TIME_TO_MOVE_BETWEEN_FLOORS = 500;

    private final Collection<ElevatorButton> elevatorButtons = Collections.synchronizedCollection(new ArrayList<>());
    private Integer destinationFloorNumber;
    private boolean isStopped = true;
    private Integer currentFloorNumber;
    private final ElevatorDirection movementDirection = new ElevatorDirection(Direction.UP);

    public Elevator(Integer capacity, Building building, Integer currentFloorNumber, CustomLogger logger) {
        this.logger = logger;
        this.building = building;
        this.capacity = capacity;
        this.currentFloorNumber = currentFloorNumber;
        synchronized (elevatorButtons) {
            building.getFloors().forEach(floor -> elevatorButtons.add(new ElevatorButton(floor.floorNumber)));
        }
    }

    private void goToDestinationFloor() {
        logger.logPassengers("Passengers: " + passengers);

        if (atDestination()) {
            logger.logElevator("Already at destination floor: " + this.destinationFloorNumber);
        } else {
            logger.logElevator("Moving to destination floor: " + this.destinationFloorNumber);
            synchronized (movementDirection) {
                while (!atDestination()) {
                    isStopped = false;
                    makeStep();
                }
                isStopped = true;
            }
        }
        releaseButtons();
        loadAndUnloadPassengers();
    }

    private void makeStep() {
        simulateElevatorMovingTime();
        if (currentFloorNumber < this.destinationFloorNumber) {
            currentFloorNumber++;
            movementDirection.setDirection(Direction.UP);
        } else {
            currentFloorNumber--;
            movementDirection.setDirection(Direction.DOWN);
        }
        logger.logElevator("Moving " + movementDirection + ", " + currentFloorNumber + "/" + building.getFloorCount());
    }

    private void releaseButtons() {
        synchronized (elevatorButtons) {
            elevatorButtons.stream()
                    .filter(button -> button.getFloorNumber().equals(currentFloorNumber))
                    .findFirst()
                    .ifPresent(Button::release);
        }
        synchronized (building.getFloors()) {
            building.getFloors().stream()
                    .filter(floor -> floor.floorNumber.equals(currentFloorNumber))
                    .findFirst()
                    .ifPresent(floor -> floor.getButton().release());
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
                .filter(passenger -> passenger.destinationFloorNumber.equals(currentFloor.floorNumber))
                .forEach(passengersToUnload::add);
        if (passengersToUnload.isEmpty()) {
            return;
        }
        logger.logElevator(passengersToUnload.size() + " passengers arrived at their destination floor " + destinationFloorNumber);
        passengers.removeAll(passengersToUnload);
    }

    private void loadPassengers(Floor currentFloor) {
        int newPassengers = 0;
        Collection<Passenger> waitingPassengers = currentFloor.getWaitingPassengers();
        synchronized (waitingPassengers) {
            ArrayList<Passenger> passengersToLoad = new ArrayList<>();
            for (Passenger passenger : waitingPassengers) {
                if (this.passengers.size() + passengersToLoad.size() >= this.capacity) {
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

    private void pressElevatorButton(Integer destinationFloorNumber) {
        synchronized (elevatorButtons) {
            elevatorButtons.stream()
                    .filter(button -> button.getFloorNumber().equals(destinationFloorNumber))
                    .findFirst().ifPresent(Button::press);
        }
    }

    private void simulateElevatorMovingTime() {
        try {
            Thread.sleep(TIME_TO_MOVE_BETWEEN_FLOORS);
        } catch (InterruptedException e) {
            logger.logError(e);
        }
    }

    public Integer getDestinationFloorNumber() {
        return destinationFloorNumber;
    }

    public void setDestinationFloorNumber(Integer destinationFloorNumber) {
        this.destinationFloorNumber = destinationFloorNumber;
    }

    public boolean atDestination() {
        return currentFloorNumber.equals(destinationFloorNumber);
    }

    public synchronized Collection<ElevatorButton> getElevatorButtons() {
        return this.elevatorButtons;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public Integer getCurrentFloorNumber() {
        return currentFloorNumber;
    }

    public void setMovementDirection(Direction movementDirection) {
        synchronized (this.movementDirection) {
            this.movementDirection.setDirection(movementDirection);
        }
    }

    public Direction getMovementDirection() {
        synchronized (this.movementDirection) {
            return this.movementDirection.getDirection();
        }
    }

    public void wakeUp() {
        synchronized (sleepLock) {
            sleepLock.notify();
            isSleeping = false;
        }
    }

    @Override
    public void run() {
        logger.logElevator("Started at floor " + currentFloorNumber);
        while (shouldRun) {
            waitIfNeeded();
            goToDestinationFloor();
        }
        logger.logElevator("Stopped");
    }

    private void waitIfNeeded() {
        if (atDestination() || destinationFloorNumber == null) {
            synchronized (sleepLock) {
                try {
                    logger.logElevator("Waiting for calls");
                    isSleeping = true;
                    sleepLock.wait();
                } catch (InterruptedException e) {
                    logger.logError(e);
                }
            }
        }
    }

    public void stopThread() {
        shouldRun = false;
    }

    public boolean isNotSleeping() {
        return !isSleeping;
    }
}
