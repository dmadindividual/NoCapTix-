package topg.Event_Platform.exceptions;

public class ErrorCreatingUser extends RuntimeException {
    public ErrorCreatingUser(String message) {
        super(message);
    }
}
