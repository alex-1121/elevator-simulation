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
    private final Object lock = new Object();

    private final Building building;
    private final Integer capacity;
    private final ArrayList<Passenger> passengers = new ArrayList<>();
    private static final Integer TIME_TO_MOVE_BETWEEN_FLOORS = 500;

    private final Collection<ElevatorButton> elevatorButtons = Collections.synchronizedCollection(new ArrayList<>());
    private final SortedSet<Integer> destinationFloorNumbers = Collections.synchronizedSortedSet(new TreeSet<>());
    private Integer destinationFloorNumber;
    private boolean isMoving = false;
    private Integer currentFloorNumber;
    private Direction movementDirection;

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
        synchronized (destinationFloorNumbers) {
            if (this.destinationFloorNumbers.isEmpty()) {
                return;
            }
            this.destinationFloorNumber = destinationFloorNumbers.first();
        }

        logger.logPassengers("Passengers: " + passengers);
        logger.logElevator("Moving to destination floor: " + this.destinationFloorNumber);
        while (!atDestination()) {
            isMoving = true;
            makeStep();
        }
        isMoving = false;
        synchronized (destinationFloorNumbers) {
            destinationFloorNumbers.remove(currentFloorNumber);
        }
        releaseButtons();
        loadAndUnloadPassengers();
    }

    private void makeStep() {
        simulateElevatorMovingTime();
        if (currentFloorNumber < this.destinationFloorNumber) {
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

    public boolean atDestination() {
        return currentFloorNumber.equals(destinationFloorNumber);
    }

    public synchronized Collection<ElevatorButton> getElevatorButtons() {
        return this.elevatorButtons;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public Integer getCurrentFloorNumber() {
        return currentFloorNumber;
    }

    public void addDestinations(Set<Integer> newDestinations) {
        this.destinationFloorNumbers.addAll(newDestinations);
    }

    public void setMovementDirection(Direction movementDirection) {
        this.movementDirection = movementDirection;
    }

    public Direction getMovementDirection() {
        return this.movementDirection;
    }

    public void wakeUp() {
        synchronized (lock) {
            lock.notify();
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

    public void stopThread() {
        shouldRun = false;
    }
}
