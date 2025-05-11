package topg.Event_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.enums.TicketCategory;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {
    boolean existsByEventAndTicketCategory(Events event, TicketCategory ticketCategory);
}
