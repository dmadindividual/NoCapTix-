package topg.Event_Platform.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.enums.TicketCategory;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.TicketType;

import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketType t WHERE t.ticketTypeId = :id")
    Optional<TicketType> findByIdForUpdate(@Param("id") String id);

    boolean existsByEventAndTicketCategory(Events event, TicketCategory ticketCategory);
}
