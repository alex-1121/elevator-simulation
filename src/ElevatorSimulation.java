import main.Stoppable;
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

    private static final int NUMBER_OF_FLOORS = 5;
    private static final int ELEVATOR_CAPACITY = 6;
    private static final CustomLogger logger = new CustomLogger();

    public static void main(String[] args) throws InterruptedException {

        Building building = new Building(NUMBER_OF_FLOORS);
        Elevator elevator = new Elevator(ELEVATOR_CAPACITY, building, 3, logger);

        List<Stoppable> runnables = Arrays.asList(
                elevator,
                new ElevatorControlSystem(elevator, building, logger),
                new PassengerGenerator(building, logger)
        );

        List<Thread> threads = runnables.stream()
                .map(runnable -> {
                    Thread t = new Thread(runnable);
                    logger.logMain(String.format("%s: %s", t.getName(), runnable.getClass().getSimpleName()));
                    return t;
                }).toList();

        threads.forEach(Thread::start);

        logger.logMain("All threads started");

        //noinspection resource
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            boolean isEveryThreadAlive = threads.stream().allMatch(Thread::isAlive);
            if (!isEveryThreadAlive) {
                logger.logMain("Stopping all threads");
                runnables.forEach(Stoppable::stop);
                scheduler.close();
            }
        }, 0, 1, TimeUnit.SECONDS);

        for (Thread thread : threads) {
            thread.join();
        }
        logger.logMain("All threads stopped");
    }
}
