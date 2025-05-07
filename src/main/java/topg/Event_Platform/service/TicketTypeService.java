package topg.Event_Platform.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import topg.Event_Platform.config.UserDetailsServiceImpl;
import topg.Event_Platform.dto.TicketRequestDto;
import topg.Event_Platform.dto.TicketResponseDto;
import topg.Event_Platform.enums.TicketCategory;
import topg.Event_Platform.exceptions.EventExpired;
import topg.Event_Platform.exceptions.EventNotFoundInDb;
import topg.Event_Platform.exceptions.InvalidTIcketCategory;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.TicketType;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.EventRepository;
import topg.Event_Platform.repositories.TicketTypeRepository;
import topg.Event_Platform.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@RequiredArgsConstructor
@Service
public class TicketTypeService {
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;



    @Transactional
    public TicketResponseDto createTicket(TicketRequestDto ticketRequestDto, Authentication connectedUser) {
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        // Step 2: Retrieve the actual User entity from the DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Events event = eventRepository.findById(ticketRequestDto.eventId())
                .orElseThrow(() -> new EventNotFoundInDb("Event not found"));

        if (LocalDateTime.now().isAfter(event.getDateTime())) {
            throw new EventExpired("Event has already occurred. Ticket sales are closed.");
        }

        TicketCategory ticketCategory;
        try {
            ticketCategory = TicketCategory.valueOf(ticketRequestDto.ticketCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTIcketCategory("Invalid ticket category. Valid options are: " + Arrays.toString(getValidCategories()));
        }

        double finalPrice = calculateDynamicPrice(
                ticketCategory,
                ticketRequestDto.price(),
                event.getDateTime(),
                ticketRequestDto.totalQuantity()
        );


        TicketType ticketType = TicketType.builder()
                .ticketCategory(ticketCategory)
                .price(finalPrice)
                .totalQuantity(ticketRequestDto.totalQuantity())
                .quantityAvailable(ticketRequestDto.quantityAvailable())
                .createdBy(user)
                .event(event)
                .build();

        ticketTypeRepository.save(ticketType);

        return new TicketResponseDto(
                ticketType.getTicketTypeId(),
                ticketType.getTicketCategory(),
                ticketType.getPrice(),
                ticketType.getTotalQuantity(),
                ticketType.getQuantityAvailable()
        );
    }

    private String[] getValidCategories() {
        return Arrays.stream(TicketCategory.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    private double calculateDynamicPrice(
            TicketCategory category,
            double basePrice,
            LocalDateTime eventDate,
            int quantity
    ) {
        LocalDateTime now = LocalDateTime.now();
        long daysUntilEvent = ChronoUnit.DAYS.between(now, eventDate);

        return switch (category) {
            case EARLY_BIRD -> {
                if (daysUntilEvent >= 7) yield basePrice * 0.90; // 10% discount
                else if (daysUntilEvent <= 2 && daysUntilEvent >= 0) yield basePrice * 1.20; // 20% increase
                else yield basePrice;
            }
            case REGULAR -> {
                if (daysUntilEvent <= 1 && daysUntilEvent >= 0) yield basePrice * 1.10; // 10% increase
                else yield basePrice;
            }
            case VIP -> basePrice * 0.95;
            case GROUP -> (quantity >= 5) ? basePrice * 0.85 : basePrice;
            default -> basePrice;
        };
    }



}