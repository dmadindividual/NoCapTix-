package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRole extends  AppBaseException {
    public InvalidRole(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
