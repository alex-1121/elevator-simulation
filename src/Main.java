import building.Building;
import building.Floor;
import building.Passenger;
import elevator.Elevator;
import elevator.ElevatorControlSystem;

public class Main {
    public static void main(String[] args) {

        int numberOfFloors = 5;
        int elevatorCapacity = 6;

        Building building = new Building(numberOfFloors);
        Elevator elevator = new Elevator(elevatorCapacity, building, 3);
        ElevatorControlSystem ecs = new ElevatorControlSystem(elevator, building);

        Passenger passenger1 = new Passenger(1);
        Passenger passenger2 = new Passenger(1);

        Floor secondFloor = building.getFloors().get(1);
        Floor fifthFloor = building.getFloors().get(4);
        secondFloor.addWaitingPassenger(passenger1);
        fifthFloor.addWaitingPassenger(passenger2);
        secondFloor.button.press();
        fifthFloor.button.press();

        for (int i = 0; i < 3; i++) {
            ecs.handleElevatorCalls();
        }
    }
}
