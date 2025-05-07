package topg.Event_Platform.exceptions;

public class EventNotFoundInDb extends RuntimeException {
    public EventNotFoundInDb(String message) {
        super(message);
    }
}
