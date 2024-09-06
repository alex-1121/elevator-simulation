package button;

public class Button {
    private boolean isPressed = false;

    public void press() {
        isPressed = true;
    }

    public void release() {
        isPressed = false;
    }
}
