package elevator;

import building.Building;
import building.Floor;
import building.Passenger;
import button.Button;
import customLogger.CustomLogger;

import java.util.ArrayList;

public class Elevator {
    
    private final CustomLogger logger;
    
    public final int capacity;
    public final ArrayList<ElevatorButton> elevatorButtons = new ArrayList<>();

    public int currentFloorNumber;
    public int destinationFloor;
    public ArrayList<Passenger> passengers = new ArrayList<>();
    private final Building building;

    public Elevator(int capacity, Building building, int currentFloorNumber, CustomLogger logger) {
        this.logger = logger;
        this.capacity = capacity;
        this.currentFloorNumber = currentFloorNumber;
        this.destinationFloor = currentFloorNumber;
        this.building = building;
        initializeElevatorButtons();

        logger.logElevator("starting at floor: " + currentFloorNumber);
    }

    private void initializeElevatorButtons() {
        building.getFloors().forEach(floor -> elevatorButtons.add(new ElevatorButton(floor.floorNumber)));
    }

    public void goToFloor(int floorNumber) {
        destinationFloor = floorNumber;
        logger.logElevator("destination floor: " + destinationFloor);
        while (!atDestination()) {
            if (currentFloorNumber < destinationFloor) {
                currentFloorNumber++;
                logger.logElevator("moving up");
            } else {
                currentFloorNumber--;
                logger.logElevator("moving down");
            }
        }
        releaseButton();
        logger.logElevator("elevator arrived at floor " + currentFloorNumber);
    }

    private void releaseButton() {
        elevatorButtons.stream()
                .filter(button -> button.floorNumber == currentFloorNumber)
                .findFirst()
                .ifPresent(Button::release);
    }

    public boolean atDestination() {
        return currentFloorNumber == destinationFloor;
    }

    public void loadAndUnloadPassengers() {
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
        logger.logElevator(passengersToUnload.size() + " passengers arrived at their destination floor");
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
        logger.logElevator("new passengers: " + newPassengers + ", total passengers: " + this.passengers.size() + "/" + capacity);
    }

    private void pressElevatorButton(int destinationFloorNumber) {
        elevatorButtons.stream()
                .filter(button -> button.floorNumber == destinationFloorNumber)
                .findFirst().ifPresent(Button::press);
    }
}
