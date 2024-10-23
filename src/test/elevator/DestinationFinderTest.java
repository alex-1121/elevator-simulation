package elevator;

import main.Direction;
import main.building.Building;
import main.button.Button;
import main.customLogger.CustomLogger;
import main.elevator.ButtonReader;
import main.elevator.DestinationFinder;
import main.elevator.Elevator;
import main.elevator.ElevatorButton;
import main.passenger.PassengerGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DestinationFinderTest {

    CustomLogger logger = new CustomLogger();

    DestinationFinder destinationFinder = new DestinationFinder();
    ButtonReader buttonReader = new ButtonReader();

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
        Elevator elevator = createElevator(3);

        assertFalse(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>()));
        assertFalse(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>(Arrays.asList(1, 2))));
    }

    @Test
    void isMoreDestinationsOnTheWay_findsDestinationOnCurrentFloor() {
        Elevator elevator = createElevator(3);

        assertTrue(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>(Arrays.asList(2, 3))));

        elevator.setMovementDirection(Direction.DOWN);
        assertTrue(destinationFinder.isMoreDestinationsOnTheWay(elevator, new TreeSet<>(Arrays.asList(3, 4))));
    }

    @Test
    void lookUp_findsDestinationOnCurrentFloor() {
        Building building = createBuildingWithPassengerOnFloor(3);

        Elevator elevator = createElevator(3, building);
        Set<Integer> destinationFloorNumbers = destinationFinder.getDestinationFloorNumbers(
                buttonReader.detectPressedElevatorButtons(elevator),
                buttonReader.detectPressedFloorButtons(building)
        );
        Set<ElevatorButton> pressedElevatorButtons = new TreeSet<>(Button.buttonComparator);
        pressedElevatorButtons.addAll(buttonReader.detectPressedElevatorButtons(elevator));

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

        // TODO returns current floor, fix this
        assertEquals(Optional.empty(), destinationFinder.lookUp(elevator, pressedElevatorButtons, destinationFloorNumbers));
    }

    //TODO test both parts of lookUp

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

        // TODO returns current floor, fix this
        assertFalse(destinationFinder.lookBelow(elevator, destinationFloorNumbers).isPresent());
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

    Elevator createElevator(Integer initialFloorNumber, Building building) {
        return new Elevator(6, building, initialFloorNumber, logger);
    }
}
