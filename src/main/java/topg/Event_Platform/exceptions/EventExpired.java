package topg.Event_Platform.exceptions;

public class EventExpired extends RuntimeException {
    public EventExpired(String message) {
        super(message);
    }
}
