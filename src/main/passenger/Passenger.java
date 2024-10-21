package main.passenger;

public class Passenger {
    public final Integer destinationFloorNumber;

    public Passenger(int destinationFloorNumber) {
        this.destinationFloorNumber = destinationFloorNumber;
    }

    @Override
    public String toString() {
        return String.valueOf(destinationFloorNumber);
    }
}
