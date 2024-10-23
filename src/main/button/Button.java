package main.button;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Button {
    public static final Comparator<Button> buttonComparator = Comparator.comparingInt(Button::getFloorNumber);

    private final Integer floorNumber;
    private boolean isPressed = false;

    public Button(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void press() {
        isPressed = true;
    }

    public void release() {
        isPressed = false;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", getFloorNumber(), isPressed);
    }
}
