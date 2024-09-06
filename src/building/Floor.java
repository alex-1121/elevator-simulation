package building;

import java.util.ArrayList;

public class Floor {
    private final int floorNumber;
    private ArrayList<Passenger> waitingPassengers;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.waitingPassengers = new ArrayList<Passenger>();
    }

    public ArrayList<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }

    public void addPassenger(Passenger newPassenger) {
        this.waitingPassengers.add(newPassenger);
    }

    public Passenger removePassenger(Passenger passenger) {
        return this.waitingPassengers.removeLast();
    }
}
