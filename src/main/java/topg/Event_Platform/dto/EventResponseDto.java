package topg.Event_Platform.dto;

import topg.Event_Platform.models.TicketType;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDto(
        Integer eventId,
        String name,
        String description,
        String location,
        LocalDateTime dateTime,
        String organizer,
        List<TicketType> ticketTypes
) {
}

