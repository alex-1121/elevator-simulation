package elevator;

import building.Building;
import building.Floor;
import building.Passenger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class ElevatorControlSystemTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpLogging() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void checkPressedButtons() {
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

        // ecs.handleElevatorCalls();

        for (int i = 0; i < 3; i++) {
            ecs.handleElevatorCalls();
        }

        String expectedResult = """
                starting at floor: 3
                pressed floor button detected on floor 2
                pressed floor button detected on floor 5
                destination floor: 5
                moving up
                moving up
                elevator arrived at floor 5
                new passengers: 1, total passengers: 1/6
                destination floor: 2
                moving down
                moving down
                moving down
                elevator arrived at floor 2
                new passengers: 1, total passengers: 2/6
                pressed elevator button detected 1
                destination floor: 1
                moving down
                elevator arrived at floor 1
                2 passengers arrived at their destination floor
                new passengers: 0, total passengers: 0/6
                Elevator idling
                """;

        Assertions.assertEquals(expectedResult, outputStreamCaptor.toString());
    }
}
