package topg.Event_Platform.models;

import jakarta.persistence.*;
import lombok.*;
import topg.Event_Platform.enums.TicketCategory;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ticketTypeId;

    @Enumerated(EnumType.STRING)
    private TicketCategory ticketCategory; // e.g., Regular, Early Bird
    private double price;
    private int totalQuantity;
    private int quantityAvailable;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Events event;
    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>(); // Initialize with an empty list

}
