package topg.Event_Platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import topg.Event_Platform.dto.TicketPurchaseRequest;
import topg.Event_Platform.exceptions.EventNotFoundInDb;
import topg.Event_Platform.exceptions.InvalidTIcketCategory;
import topg.Event_Platform.exceptions.UserNotFoundInDataBase;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.Order;
import topg.Event_Platform.models.TicketType;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.EventRepository;
import topg.Event_Platform.repositories.OrderRepository;
import topg.Event_Platform.repositories.TicketTypeRepository;
import topg.Event_Platform.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TicketTypeRepository ticketTypeRepo;
    private final EventRepository eventsRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final RestTemplate restTemplate;

    @Value("${paystack.secret-key}")
    private String paystackSecretKey;

    @Value("${paystack.base-url}")
    private String paystackBaseUrl;

    public String initializePayment(TicketPurchaseRequest ticketRequestDto, String userEmail, String userId) {
        Events events = eventsRepo.findById(ticketRequestDto.eventId())
                .orElseThrow(() -> new EventNotFoundInDb("Event not found"));

        TicketType ticketType = ticketTypeRepo.findById(ticketRequestDto.ticketTypeId())
                .orElseThrow(() -> new InvalidTIcketCategory("Ticket type not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundInDataBase("User not found in database"));

        if (!ticketType.getEvent().getEventId().equals(events.getEventId())) {
            throw new InvalidTIcketCategory("Ticket type does not belong to the event");
        }

        if (ticketType.getQuantityAvailable() < ticketRequestDto.quantity()) {
            throw new InvalidTIcketCategory("Not enough tickets available");
        }

        double totalAmount = ticketType.getPrice() * ticketRequestDto.quantity();
        String reference = UUID.randomUUID().toString();

        Order order = Order.builder()
                .reference(reference)
                .status("PENDING")
                .quantity(ticketRequestDto.quantity())
                .totalAmount(totalAmount)
                .ticketType(ticketType)
                .event(events)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepo.save(order);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "email", userEmail,
                "amount", (int)(totalAmount * 100),
                "reference", reference,
                "callback_url", "https://yourdomain.com/payment-success"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                paystackBaseUrl + "/transaction/initialize",
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return (String) data.get("authorization_url");
        } else {
            throw new RuntimeException("Failed to initialize payment with Paystack");
        }
    }
}
