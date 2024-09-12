package building;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Building {
    private final Collection<Floor> floors = Collections.synchronizedCollection(new LinkedList<>());

    public Building(int numberOfFloors) {

        for (int i = 0; i < numberOfFloors; i++) {
            floors.add(new Floor(i + 1));
        }
    }

    public Floor getFloorByNumber(int floorNumber) {
        return floors.stream().filter(f -> f.floorNumber == floorNumber).findFirst().orElse(null);
    }

    public Collection<Floor> getFloors() {
        return floors;
    }
}
