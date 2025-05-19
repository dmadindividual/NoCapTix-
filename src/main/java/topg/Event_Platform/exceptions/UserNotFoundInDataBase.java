package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundInDataBase extends  AppBaseException {
    public UserNotFoundInDataBase(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
