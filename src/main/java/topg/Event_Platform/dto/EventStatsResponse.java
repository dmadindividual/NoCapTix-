package topg.Event_Platform.dto;

import java.math.BigDecimal;

public record EventStatsResponse(
        String eventId,
        String eventTitle,
        int ticketSold,
        BigDecimal totalRevenue

) {

}
