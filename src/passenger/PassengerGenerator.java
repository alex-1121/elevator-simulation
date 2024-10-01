package passenger;

import building.Building;
import building.Floor;
import customLogger.CustomLogger;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class PassengerGenerator implements Runnable {

    private boolean shouldRun = true;
    private static final long GENERATION_INTERVAL = 200;

    private final Building building;
    private final CustomLogger logger;
    private final Random random = new Random();

    public PassengerGenerator(Building building, CustomLogger logger) {
        this.building = building;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.logPassengers("Started");
        int counter = 0;
        while (shouldRun) {
            generateResidentialPassenger();
            executeGenerationInterval();
            counter++;
            if (counter > 10) {
                counter = 0;
                long waitingPassengers = building.getFloors().stream()
                        .map(floor -> floor.getWaitingPassengers().size())
                        .collect(Collectors.summarizingInt(Integer::intValue)).getSum();
                logger.logPassengers("Waiting passengers: " + waitingPassengers);
            }
        }
        logger.logPassengers("Stopped");
    }

    private int getRandomFloorNumberExcept(Integer floorNumberToExclude) {
        ArrayList<Integer> options = building.getFloors().stream()
                .filter(floor -> !floorNumberToExclude.equals(floor.floorNumber))
                .map(floor -> floor.floorNumber)
                .collect(Collectors.toCollection(ArrayList::new));
        return options.get(random.nextInt(options.size()));
    }

    // Residential passengers only move between 1st floor and the floor they reside.
    // There's 50% chance they start at 1st floor.
    private void generateResidentialPassenger() {
        int startingFloorNumber;
        int destinationFloorNumber;

        boolean coinFlip = random.nextBoolean();
        if (coinFlip) {
            startingFloorNumber = 1;
            destinationFloorNumber = getRandomFloorNumberExcept(startingFloorNumber);
        } else {
            startingFloorNumber = getRandomFloorNumberExcept(1);
            destinationFloorNumber = 1;
        }
        createPassenger(startingFloorNumber, destinationFloorNumber);
    }

    // Office workers can go between random floors. There's 2 in 3 chance they start or finish at 1st floor, and 1 in 3
    // chance they start and finish on random floors.
    private void generateOfficeWorkerPassenger() {
        // TODO: Implement this method
    }

    public void createPassenger(int startingFloorNumber, int destinationFloorNumber) {
        Floor floor = building.getFloorByNumber(startingFloorNumber);
        Passenger passenger = new Passenger(destinationFloorNumber);
        synchronized (building.getFloors()) {
            floor.addWaitingPassenger(passenger);
            floor.getButton().press();
        }
        logger.logPassengers("Generated passenger at floor " + startingFloorNumber + ", dest: " + destinationFloorNumber);
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