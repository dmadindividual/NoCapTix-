package topg.Event_Platform.exceptions;

import org.springframework.http.HttpStatus;

public class EventExceptions extends  AppBaseException {
  public EventExceptions(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
