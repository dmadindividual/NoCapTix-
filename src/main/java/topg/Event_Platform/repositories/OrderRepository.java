package topg.Event_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.models.Order;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
