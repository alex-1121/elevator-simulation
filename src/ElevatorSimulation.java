import main.customLogger.CustomLogger;
import main.building.Building;
import main.elevator.Elevator;
import main.elevator.ElevatorControlSystem;
import main.passenger.PassengerGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElevatorSimulation {

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
        logger.logMain("All threads started");
    }
}
