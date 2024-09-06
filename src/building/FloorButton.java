package building;

import button.Button;

public class FloorButton extends Button {
    private final FloorButtonDirection direction;

    public FloorButton(FloorButtonDirection direction) {
        this.direction = direction;
    }
}
