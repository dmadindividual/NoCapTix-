package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class EventNotFoundInDb extends  AppBaseException {
    public EventNotFoundInDb(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
