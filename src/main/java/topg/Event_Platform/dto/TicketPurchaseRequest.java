package topg.Event_Platform.dto;

public record TicketPurchaseRequest(
         Integer eventId,
         Integer ticketTypeId,
         Integer quantity
) {
}
