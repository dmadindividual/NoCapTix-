package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class QrCodeException extends  AppBaseException {
    public QrCodeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
