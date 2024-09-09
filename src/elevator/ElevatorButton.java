package elevator;

import button.Button;

public class ElevatorButton extends Button {
    public int floorNumber;

    public ElevatorButton(int floorNumber) {
        this.floorNumber = floorNumber;
    }
}
