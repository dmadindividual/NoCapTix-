package topg.Event_Platform.dto;

public record TicketPurchaseRequest(
         String eventId,
         String ticketTypeId,
         Integer quantity
) {
}
