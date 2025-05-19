package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class AppBaseException extends RuntimeException {
    private final HttpStatus status;

    public AppBaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
