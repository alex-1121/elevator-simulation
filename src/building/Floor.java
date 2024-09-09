package building;

import button.Button;

import java.util.ArrayList;

public class Floor {
    public final int floorNumber;
    private final ArrayList<Passenger> waitingPassengers = new ArrayList<>();

    public Button button = new Button();

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public ArrayList<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }

    public void addWaitingPassenger(Passenger passenger) {
        this.waitingPassengers.add(passenger);
    }
}
