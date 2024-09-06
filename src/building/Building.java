package building;

import java.util.LinkedList;

public class Building {
    private LinkedList<Floor> floors;

    public Building(int numberOfFloors) {

        floors = new LinkedList<>();
        for (int i = 0; i < numberOfFloors; i++) {
            floors.add(new Floor(i + 1));
        }
    }

    public LinkedList<Floor> getFloors() {
        return floors;
    }
}
