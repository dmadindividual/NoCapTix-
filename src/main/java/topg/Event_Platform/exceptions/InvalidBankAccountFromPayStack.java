package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidBankAccountFromPayStack extends  AppBaseException {
    public InvalidBankAccountFromPayStack(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
