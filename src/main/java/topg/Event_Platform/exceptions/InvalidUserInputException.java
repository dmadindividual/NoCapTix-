package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidUserInputException extends  AppBaseException {
    public InvalidUserInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
