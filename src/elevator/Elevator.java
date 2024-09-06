package elevator;

import building.Passenger;

import java.util.ArrayList;

public class Elevator {
    private final int capacity;
    private int currentFloor = 0;
    private ArrayList<Passenger> passengers = new ArrayList<>();

    public Elevator(int capacity) {
        this.capacity = capacity;
    }
}