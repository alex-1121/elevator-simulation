package customLogger;

import java.util.logging.Logger;

public class CustomLogger {

    private static final Logger logger = Logger.getLogger(CustomLogger.class.getSimpleName());
    private static final String LOG_FORMAT = "%5$s%n";

    // ANSI escape sequences explanation: https://stackoverflow.com/a/33206814
    private static final String ANSI_IDEA_WHITE = "\033[38;2;188;190;196m";
    private static final String ANSI_PINK = "\033[38;5;97m";
    private static final String ANSI_GREEN = "\033[38;5;29m";
    private static final String ANSI_BLUE = "\033[38;5;6m";
    private static final String ANSI_RESET = "\033[0m";

    public CustomLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", LOG_FORMAT);
    }

    public void logMain(String message) {
        logWithColor("Main:      ", message, ANSI_IDEA_WHITE);
    }

    public void logElevator(String message) {
        logWithColor("Elevator:  ", message, ANSI_PINK);
    }

    public void logECS(String message) {
        logWithColor("ECS:       ", message, ANSI_GREEN);
    }

    public void logPassengers(String message) {
        logWithColor("Passenger: ", message, ANSI_BLUE);
    }

    public void logError(InterruptedException e) {
        logger.severe(e.getMessage());
    }

    private void logWithColor(String prefix, String message, String messageColor) {
        String coloredPrefix = wrapWithColor(ANSI_IDEA_WHITE, prefix);
        String coloredMessage = wrapWithColor(messageColor, message);
        logger.info(coloredPrefix + coloredMessage);
    }

    private String wrapWithColor(String color, String message) {
        return color + message + ANSI_RESET;
    }
}
