package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTicketCategory extends  AppBaseException {
    public InvalidTicketCategory(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
