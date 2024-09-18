package passenger;

public class Passenger {
    public final int destinationFloorNumber;

    public Passenger(int destinationFloorNumber) {
        this.destinationFloorNumber = destinationFloorNumber;
    }

    @Override
    public String toString() {
        return String.valueOf(destinationFloorNumber);
    }
}
