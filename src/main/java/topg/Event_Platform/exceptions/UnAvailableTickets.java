package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class UnAvailableTickets extends  AppBaseException {
    public UnAvailableTickets(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
