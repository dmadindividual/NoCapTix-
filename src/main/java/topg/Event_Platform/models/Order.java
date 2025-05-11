package topg.Event_Platform.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "orders") // change this from "order"
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference; // Paystack transaction ref
    private String status; // PENDING, PAID, FAILED
    private Integer quantity;
    private double totalAmount;

    @ManyToOne
    private TicketType ticketType;

    @ManyToOne
    private Events event;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;
}

