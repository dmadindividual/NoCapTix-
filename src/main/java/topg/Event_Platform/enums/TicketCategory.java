package topg.Event_Platform.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TicketCategory {

    VIP("Access to VIP lounge, front-row seating, free drinks"),
    REGULAR("General admission, no reserved seating"),
    EARLY_BIRD("Discounted price for early registrants"),
    GROUP("Special pricing for group bookings");

    private final String description;




}
