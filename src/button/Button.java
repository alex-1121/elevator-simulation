package button;

public class Button {
    public final int floorNumber;
    private boolean isPressed = false;

    public Button(int floorNumber) {
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
}
