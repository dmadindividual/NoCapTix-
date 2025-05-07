package topg.Event_Platform.exceptions;

public class InvalidJwtToken extends RuntimeException {
    public InvalidJwtToken(String message) {
        super(message);
    }
}
