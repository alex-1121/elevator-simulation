package building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Building {
    private final Collection<Floor> floors = Collections.synchronizedList(new ArrayList<>());

    public Building(int numberOfFloors) {
        for (int i = 0; i < numberOfFloors; i++) {
            floors.add(new Floor(i + 1));
        }
    }

    public Floor getFloorByNumber(Integer floorNumber) {
        return floors.stream().filter(f -> f.floorNumber.equals(floorNumber)).findFirst().orElse(null);
    }

    public Collection<Floor> getFloors() {
        return floors;
    }

    public int getFloorCount() {
        return floors.size();
    }

    public int getBottomFloorNumber() {
        return floors.stream().mapToInt(f -> f.floorNumber).min().orElseThrow();
    }
}
