package topg.Event_Platform.exceptions;

public class UserNotFoundInDataBase extends RuntimeException {
    public UserNotFoundInDataBase(String message) {
        super(message);
    }
}
