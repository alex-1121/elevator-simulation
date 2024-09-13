import customLogger.CustomLogger;
import building.Building;
import elevator.Elevator;
import elevator.ElevatorControlSystem;
import passenger.PassengerGenerator;

public class Main {

    private static final CustomLogger logger = new CustomLogger();

    public static void main(String[] args) {
        int numberOfFloors = 5;
        int elevatorCapacity = 6;

        Building building = new Building(numberOfFloors);
        Elevator elevator = new Elevator(elevatorCapacity, building, 3, logger);
        ElevatorControlSystem ecs = new ElevatorControlSystem(elevator, building, logger);
        PassengerGenerator passengerGenerator = new PassengerGenerator(building, logger);

        Thread ecsRunnable = new Thread(ecs);
        Thread elevatorRunnable = new Thread(elevator);
        Thread passengerGeneratorRunnable = new Thread(passengerGenerator);

        ecsRunnable.start();
        elevatorRunnable.start();
        passengerGeneratorRunnable.start();
    }
}
