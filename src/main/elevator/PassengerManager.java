package main.elevator;

import main.building.Floor;
import main.button.Button;
import main.customLogger.CustomLogger;
import main.passenger.Passenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PassengerManager {

    private final CustomLogger logger;

    public PassengerManager(CustomLogger logger) {
        this.logger = logger;
    }

    public void unloadPassengers(Floor currentFloor, List<Passenger> passengers) {
        ArrayList<Passenger> passengersToUnload = new ArrayList<>();
        passengers.stream()
                .filter(passenger -> passenger.destinationFloorNumber.equals(currentFloor.floorNumber))
                .forEach(passengersToUnload::add);
        if (passengersToUnload.isEmpty()) {
            return;
        }
        logger.logElevator(passengersToUnload.size() + " passengers arrived at their destination floor " + currentFloor);
        passengers.removeAll(passengersToUnload);
    }

    public void loadPassengers(Floor currentFloor, Elevator elevator) {
        int newPassengers = 0;
        Collection<Passenger> waitingPassengers = currentFloor.getWaitingPassengers();
        synchronized (waitingPassengers) {
            ArrayList<Passenger> passengersToLoad = new ArrayList<>();
            for (Passenger passenger : waitingPassengers) {
                if (elevator.getPassengers().size() + passengersToLoad.size() >= elevator.getElevatorCapacity()) {
                    break;
                }
                passengersToLoad.add(passenger);
                newPassengers++;
                pressElevatorButton(passenger.destinationFloorNumber, elevator);
            }
            waitingPassengers.removeAll(passengersToLoad);
            elevator.getPassengers().addAll(passengersToLoad);
        }
        logger.logElevator(String.format("New passengers: %s, total passengers: %s/%s", newPassengers, elevator.getPassengers().size(), elevator.getElevatorCapacity()));
    }

    private void pressElevatorButton(Integer destinationFloorNumber, Elevator elevator) {
        synchronized (elevator.getElevatorButtons()) {
            elevator.getElevatorButtons().stream()
                    .filter(button -> button.getFloorNumber().equals(destinationFloorNumber))
                    .findFirst().ifPresent(Button::press);
        }
    }

}
