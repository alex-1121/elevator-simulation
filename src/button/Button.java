package button;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public class Button {
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

    public static String buttonsToString(Set<? extends Button> buttonList) {
        return buttonList.stream()
                .map(Button::getFloorNumber)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }
}
