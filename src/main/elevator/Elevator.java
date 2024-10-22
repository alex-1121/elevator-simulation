package main.elevator;

import main.Stoppable;
import main.customLogger.CustomLogger;
import main.building.Building;
import main.Direction;
import main.building.Floor;
import main.passenger.Passenger;
import main.button.Button;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator implements Stoppable {

    private static final Integer TIME_TO_MOVE_BETWEEN_FLOORS = 500;

    private final CustomLogger logger;

    private volatile boolean shouldRun = true;
    private volatile boolean isSleeping = false;

    private final Building building;
    private final Integer capacity;
    private final List<Passenger> passengers = new ArrayList<>();
    private final PassengerManager passengerManager;

    private final Collection<ElevatorButton> elevatorButtons = Collections.synchronizedCollection(new ArrayList<>());
    private final AtomicInteger currentFloorNumber;
    private final AtomicBoolean elevatorIsStopped = new AtomicBoolean(true);
    private final ElevatorDirection movementDirection = new ElevatorDirection(Direction.UP);
    private Integer destinationFloorNumber;

    public Elevator(Integer capacity, Building building, Integer currentFloorNumber, CustomLogger logger) {
        this.logger = logger;
        this.building = building;
        this.capacity = capacity;
        this.currentFloorNumber = new AtomicInteger(currentFloorNumber);
        this.passengerManager = new PassengerManager(logger);
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
            while (!atDestination()) {
                elevatorIsStopped.set(false);
                makeStep();
            }
            elevatorIsStopped.set(true);
        }
        releaseButtons();
        loadAndUnloadPassengers();
    }

    private void makeStep() {
        simulateElevatorMovingTime();
        if (currentFloorNumber.get() < this.destinationFloorNumber) {
            currentFloorNumber.incrementAndGet();
            movementDirection.setDirection(Direction.UP);
        } else {
            currentFloorNumber.decrementAndGet();
            movementDirection.setDirection(Direction.DOWN);
        }
        logger.logElevator(String.format("Moving %s, %s/%s", movementDirection, currentFloorNumber, building.getFloorCount()));
    }

    private void releaseButtons() {
        synchronized (elevatorButtons) {
            elevatorButtons.stream()
                    .filter(button -> button.getFloorNumber().equals(currentFloorNumber.get()))
                    .findFirst()
                    .ifPresent(Button::release);
        }
        synchronized (building.getFloors()) {
            building.getFloors().stream()
                    .filter(floor -> floor.floorNumber.equals(currentFloorNumber.get()))
                    .findFirst()
                    .ifPresent(floor -> floor.getButton().release());
        }
    }

    private void loadAndUnloadPassengers() {
        Floor currentFloor = this.building.getFloorByNumber(currentFloorNumber.get());
        passengerManager.unloadPassengers(currentFloor, passengers);
        passengerManager.loadPassengers(currentFloor, this);
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
        return Objects.equals(currentFloorNumber.get(), destinationFloorNumber);
    }

    public synchronized Collection<ElevatorButton> getElevatorButtons() {
        return this.elevatorButtons;
    }

    public boolean isStopped() {
        return elevatorIsStopped.get();
    }

    public Integer getCurrentFloorNumber() {
        return currentFloorNumber.get();
    }

    public Integer getElevatorCapacity() {
        return capacity;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setMovementDirection(Direction movementDirection) {
        if (isStopped()) {
            this.movementDirection.setDirection(movementDirection);
        }
    }

    public Direction getMovementDirection() {
        return this.movementDirection.getDirection();
    }

    public void wakeUp() {
        synchronized (this) {
            notify();
            isSleeping = false;
        }
    }

    @Override
    public void run() {
        logger.logElevator(String.format("Started at floor %s", currentFloorNumber));
        while (shouldRun) {
            waitIfNeeded();
            goToDestinationFloor();
        }
        logger.logElevator("Stopped");
    }

    private void waitIfNeeded() {
        if (atDestination() || destinationFloorNumber == null) {
            try {
                logger.logElevator("Waiting for calls");
                synchronized (this) {
                    isSleeping = true;
                    wait();
                }
            } catch (InterruptedException e) {
                logger.logError(e);
            }
        }
    }

    @Override
    public void stop() {
        shouldRun = false;
    }

    public boolean isNotSleeping() {
        return !isSleeping;
    }
}
