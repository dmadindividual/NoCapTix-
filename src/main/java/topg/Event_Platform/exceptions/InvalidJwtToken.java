package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidJwtToken extends  AppBaseException {
    public InvalidJwtToken(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
