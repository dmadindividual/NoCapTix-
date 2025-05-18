package topg.Event_Platform.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import topg.Event_Platform.config.UserDetailsServiceImpl;
import topg.Event_Platform.dto.EventStatsResponse;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.Order;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.EventRepository;
import topg.Event_Platform.repositories.OrderRepository;
import topg.Event_Platform.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAnalytics {
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;


    public EventStatsResponse getEventStatsByOrganizerId(String eventId, Authentication connectedUser){
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String userId = userDetails.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Events event = eventRepository.findByEventIdAndCreatedBy(eventId, user)
                .orElseThrow(() -> new RuntimeException("Event not found or not authorized"));
        List<Order> paidOrders = orderRepository.findByEvent_EventIdAndStatus(eventId, "PAID");

        int ticketSold = paidOrders.stream()
                .mapToInt(Order::getQuantity)
                .sum();
        BigDecimal totalRevenue = paidOrders.stream()
                .map(order-> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EventStatsResponse(
                event.getEventId(),
                event.getName(),
                ticketSold,
                totalRevenue
        );



    }





}
