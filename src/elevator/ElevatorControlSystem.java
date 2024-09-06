package elevator;

import building.FloorButton;

import java.util.ArrayList;

public class ElevatorControlSystem {
    private ArrayList<ElevatorButton> elevatorButtons;
    private ArrayList<FloorButton> floorButtons;
    private Elevator elevator;

    public ElevatorControlSystem(Elevator elevator, ArrayList<FloorButton> floorButtons, ArrayList<ElevatorButton> elevatorButtons) {
        this.elevator = elevator;
        this.floorButtons = floorButtons;
        this.elevatorButtons = elevatorButtons;
    }
}
