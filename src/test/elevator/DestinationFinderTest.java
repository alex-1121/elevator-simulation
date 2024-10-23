package elevator;

import main.Direction;
import main.building.Building;
import main.button.Button;
import main.customLogger.CustomLogger;
import main.elevator.*;
import main.passenger.PassengerGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DestinationFinderTest {

    CustomLogger logger = new CustomLogger();

    DestinationFinder destinationFinder = new DestinationFinder();
    ButtonReader buttonReader = new ButtonReader();
    PassengerManager passengerManager = new PassengerManager(logger);

    @Test
    void isMoreDestinationsOnTheWay_findsDestinationsAbove() {
        Elevator elevator = createElevator(1);
        Set<Integer> destinations = new TreeSet<>(Arrays.asList(2, 3));

        assertTrue(destinationFinder.isMoreDestinationsOnTheWay(elevator, destinations));
    }

    @Test
    void isMoreDestinationsOnTheWay_findsDestinationsBelow() {
        Elevator elevator = createElevator(5);
        elevator.setMovementDirection(Direction.DOWN);
        Set<Integer> destinations = new TreeSet<>(Arrays.asList(2, 3));

        assertTrue(destinationFinder.isMoreDestinationsOnTheWay(elevator, destinations));
    }

    @Test
    void isMoreDestinationsOnTheWay_returnsFalseIfNoValidDestinations() {
        Elevator elevator = createElevator(3); // Default is direction UP

        assertFalse(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>()));
        assertFalse(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>(Arrays.asList(1, 2))));
    }

    @Test
    void lookUp_findsDestinationOnCurrentFloor() {
        //TODO all cases (lower, higher, same floor) for both parts of lookUp
        Building building = createBuilding();

        Elevator elevator = createElevatorWithPassenger(3, 3, building);

        Set<ElevatorButton> pressedElevatorButtons = new TreeSet<>(Button.buttonComparator);
        pressedElevatorButtons.addAll(buttonReader.detectPressedElevatorButtons(elevator));

        // Passing empty set, to make sure that result is based on ElevatorButtons
        Set<Integer> destinationFloorNumbers = new TreeSet<>();
        assertEquals(3, destinationFinder.lookUp(elevator, pressedElevatorButtons, destinationFloorNumbers).orElseThrow());
    }

    @Test
    void lookUp_findsDestinationOnUpperFloor() {
        Building building = createBuildingWithPassengerOnFloor(4);

        Elevator elevator = createElevator(1, building);
        Set<ElevatorButton> pressedElevatorButtons = buttonReader.detectPressedElevatorButtons(elevator);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                pressedElevatorButtons,
                buttonReader.detectPressedFloorButtons(building)
        );

        assertEquals(4, destinationFinder.lookUp(elevator, pressedElevatorButtons, destinationFloorNumbers).orElseThrow());
    }

    @Test
    void lookUp_ignoresDestinationOnLowerFloor() {
        Building building = createBuildingWithPassengerOnFloor(1);

        Elevator elevator = createElevator(3, building);
        Set<ElevatorButton> pressedElevatorButtons = buttonReader.detectPressedElevatorButtons(elevator);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                pressedElevatorButtons,
                buttonReader.detectPressedFloorButtons(building)
        );

        assertEquals(Optional.empty(), destinationFinder.lookUp(elevator, pressedElevatorButtons, destinationFloorNumbers));
    }


    @Test
    void lookBelow_findsDestinationOnFloorBelow() {
        Building building = createBuildingWithPassengerOnFloor(1);

        Elevator elevator = createElevator(3, building);
        Set<ElevatorButton> pressedElevatorButtons = buttonReader.detectPressedElevatorButtons(elevator);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                pressedElevatorButtons,
                buttonReader.detectPressedFloorButtons(building)
        );

        assertTrue(destinationFinder.lookBelow(elevator, destinationFloorNumbers).isPresent());
        assertEquals(1, destinationFinder.lookBelow(elevator, destinationFloorNumbers).get());
    }

    @Test
    void lookBelow_ignoresDestinationOnFloorAbove() {
        Building building = createBuildingWithPassengerOnFloor(5);

        Elevator elevator = createElevator(3, building);
        Set<ElevatorButton> pressedElevatorButtons = buttonReader.detectPressedElevatorButtons(elevator);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                pressedElevatorButtons,
                buttonReader.detectPressedFloorButtons(building)
        );

        assertFalse(destinationFinder.lookBelow(elevator, destinationFloorNumbers).isPresent());
    }

    @Test
    void lookBelow_findsDestinationOnCurrentFloor() {
        Building building = createBuildingWithPassengerOnFloor(5);

        Elevator elevator = createElevator(5, building);
        Set<ElevatorButton> pressedElevatorButtons = buttonReader.detectPressedElevatorButtons(elevator);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                pressedElevatorButtons,
                buttonReader.detectPressedFloorButtons(building)
        );

        assertTrue(destinationFinder.lookBelow(elevator, destinationFloorNumbers).isPresent());
        assertEquals(5, destinationFinder.lookBelow(elevator, destinationFloorNumbers).get());
    }

    Building createBuildingWithPassengerOnFloor(Integer floorNumber) {
        Building building = createBuilding();
        PassengerGenerator passengerGenerator = new PassengerGenerator(building, logger);
        passengerGenerator.createPassenger(floorNumber);

        return building;
    }

    Building createBuilding() {
        return new Building(5);
    }

    Elevator createElevator(Integer initialFloorNumber) {
        return new Elevator(6, createBuilding(), initialFloorNumber, logger);
    }

    Elevator createElevatorWithPassenger(Integer elevatorFloorNumber, Integer passengerDestination, Building building) {
        Elevator newElevator = new Elevator(6, building, elevatorFloorNumber, logger);
        PassengerGenerator passengerGenerator = new PassengerGenerator(building, logger);
        passengerGenerator.createPassenger(elevatorFloorNumber, passengerDestination);
        passengerManager.loadPassengers(building.getFloorByNumber(elevatorFloorNumber), newElevator);
        return newElevator;
    }

    Elevator createElevator(Integer initialFloorNumber, Building building) {
        return new Elevator(6, building, initialFloorNumber, logger);
    }
}
