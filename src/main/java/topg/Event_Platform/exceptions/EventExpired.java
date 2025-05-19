package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class EventExpired extends  AppBaseException {
    public EventExpired(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}