import customLogger.CustomLogger;
import building.Building;
import building.Floor;
import building.Passenger;
import elevator.Elevator;
import elevator.ElevatorControlSystem;

public class Main {

    private static final CustomLogger logger = new CustomLogger();

    public static void main(String[] args) {
        int numberOfFloors = 5;
        int elevatorCapacity = 6;

        Building building = new Building(numberOfFloors);
        Elevator elevator = new Elevator(elevatorCapacity, building, 3, logger);
        ElevatorControlSystem ecs = new ElevatorControlSystem(elevator, building, logger);

        Passenger passenger1 = new Passenger(1);
        Passenger passenger2 = new Passenger(1);

        Floor secondFloor = building.getFloorByNumber(2);
        Floor fifthFloor = building.getFloorByNumber(5);

        secondFloor.addWaitingPassenger(passenger1);
        fifthFloor.addWaitingPassenger(passenger2);
        secondFloor.getButton().press();
        fifthFloor.getButton().press();

        Thread ecsRunnable = new Thread(ecs);
        Thread elevatorRunnable = new Thread(elevator);

        ecsRunnable.start();
        elevatorRunnable.start();
    }
}
