package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidLoginDetailsException extends AppBaseException {
    public InvalidLoginDetailsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
