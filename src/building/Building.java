package building;

import java.util.LinkedList;

public class Building {
    public final LinkedList<Floor> floors = new LinkedList<>();

    public Building(int numberOfFloors) {

        for (int i = 0; i < numberOfFloors; i++) {
            floors.add(new Floor(i + 1));
        }
    }

    public Floor getFloorByNumber(int floorNumber) {
        return floors.stream().filter(f -> f.floorNumber == floorNumber).findFirst().orElse(null);
    }

    public LinkedList<Floor> getFloors() {
        return floors;
    }
}
