package test.building;

import main.building.Building;
import main.building.Floor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildingTest {

    private static final int NUMBER_OF_FLOORS = 10;

    Building building = new Building(NUMBER_OF_FLOORS);

    @Test
    void getFloorCount() {
        assertEquals(building.getFloorCount(), NUMBER_OF_FLOORS);
    }

    @Test
    void getFloorByNumber() {
        final int floorNumber = 1;

        List<Floor> floors = (List<Floor>) building.getFloors();
        Floor expectedFloor = floors.getFirst();
        Floor receivedFloor = building.getFloorByNumber(floorNumber);

        assertEquals(expectedFloor, receivedFloor);
    }

    @Test
    void getFloors() {
        Collection<Floor> floors = building.getFloors();

        assertEquals(floors.size(), NUMBER_OF_FLOORS);

        Iterator<Floor> iterator = floors.iterator();
        for (int i = 1; iterator.hasNext(); i++){
            assertEquals(iterator.next().floorNumber, i);
        }
    }
}