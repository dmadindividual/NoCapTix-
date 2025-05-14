package topg.Event_Platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import topg.Event_Platform.enums.TicketCategory;

public record TicketRequestDto(
        @NotNull String ticketCategory,
        @Positive double price,
        @Min(1) int totalQuantity,
        @Min(0) int quantityAvailable,
        @NotNull String eventId
) {}