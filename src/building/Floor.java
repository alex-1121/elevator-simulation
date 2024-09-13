package building;

import button.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Floor {
    public final int floorNumber;
    private final FloorButton button;
    private final Collection<Passenger> waitingPassengers = Collections.synchronizedCollection(new ArrayList<>());

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.button = new FloorButton(floorNumber);
    }

    public Collection<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }

    public void addWaitingPassenger(Passenger passenger) {
        this.waitingPassengers.add(passenger);
    }

    public FloorButton getButton() {
        return button;
    }
}
