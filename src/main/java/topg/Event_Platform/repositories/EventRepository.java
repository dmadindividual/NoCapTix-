package topg.Event_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.models.Events;
@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {
}
