package topg.Event_Platform.dto;

import topg.Event_Platform.enums.TicketCategory;

public record TicketResponseDto(
        Integer ticketTypeId,
        TicketCategory ticketCategory,
        double price,
        int totalQuantity,
        int quantityAvailable
) {}