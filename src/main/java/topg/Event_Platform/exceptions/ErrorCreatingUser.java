package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class ErrorCreatingUser extends AppBaseException {
    public ErrorCreatingUser(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
