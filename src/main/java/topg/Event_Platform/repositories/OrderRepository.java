package topg.Event_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.models.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByReference(String reference);

    List<Order> findByEvent_EventIdAndStatus(String eventId, String paid);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt <= :cutoff")
    List<Order> findPendingOrdersOlderThanMinutes(@Param("cutoff") LocalDateTime cutoff);
}
