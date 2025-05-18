package topg.Event_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Events, String> {
    List<Events> findByCreatedBy(User user);

    Optional<Events> findByEventIdAndCreatedBy(String eventId, User createdBy);
}
