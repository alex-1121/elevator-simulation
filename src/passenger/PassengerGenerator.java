package passenger;

import building.Building;
import building.Floor;
import customLogger.CustomLogger;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class PassengerGenerator implements Runnable {

    private boolean shouldRun = true;
    private static final long GENERATION_INTERVAL = 100;

    private final Building building;
    private final CustomLogger logger;
    private final Random random = new Random();

    public PassengerGenerator(Building building, CustomLogger logger) {
        this.building = building;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.logMain("Started");
        while (shouldRun) {
            generatePassenger();
            try {
                Thread.sleep(GENERATION_INTERVAL);
            } catch (InterruptedException e) {
                logger.logError(e);
            }
        }
        logger.logMain("Stopped");
    }

    private int getRandomDestinationFloorNumber(Integer currentFloorNumber) {
        ArrayList<Integer> options = building.getFloors().stream()
                .filter(floor -> !currentFloorNumber.equals(floor.floorNumber))
                .map(floor -> floor.floorNumber)
                .collect(Collectors.toCollection(ArrayList::new));
        return options.get(random.nextInt(options.size()));
    }

    private void generatePassenger() {
        int floorNumber = random.nextInt(building.getFloors().size()) + 1;
        int destinationFloorNumber = getRandomDestinationFloorNumber(floorNumber);
        Floor floor = building.getFloorByNumber(floorNumber);
        Passenger passenger = new Passenger(destinationFloorNumber);
        synchronized (building.getFloors()) {
            floor.addWaitingPassenger(passenger);
            floor.getButton().press();
        }
        // logger.logMain("Generated passenger at floor " + floorNumber + ", dest: " + destinationFloorNumber);
    }

    private void executeGenerationInterval() {
        try {
            Thread.sleep(GENERATION_INTERVAL);
        } catch (InterruptedException e) {
            logger.logError(e);
        }
    }

    public void stopThread() {
        shouldRun = false;
    }
}