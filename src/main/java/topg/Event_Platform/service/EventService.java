package topg.Event_Platform.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topg.Event_Platform.config.UserDetailsServiceImpl;
import topg.Event_Platform.dto.EventRequestDto;
import topg.Event_Platform.dto.EventResponseDto;
import topg.Event_Platform.dto.PageResponse;
import topg.Event_Platform.exceptions.EventNotFoundInDb;
import topg.Event_Platform.models.Events;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.EventRepository;
import topg.Event_Platform.repositories.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponseDto createEvent(EventRequestDto eventRequestDto, Authentication connectedUser) {
        try {
            UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
            String email = userDetails.getUsername();

            // Step 2: Retrieve the actual User entity from the DB
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Events events = Events.builder()
                    .eventId(generateEventId())
                    .name(eventRequestDto.name())
                    .description(eventRequestDto.description())
                    .location(eventRequestDto.location())
                    .dateTime(eventRequestDto.dateTime())
                    .createdBy(user)
                    .build();

            eventRepository.save(events);

            return new EventResponseDto(
                    events.getEventId(),
                    events.getName(),
                    events.getDescription(),
                    events.getLocation(),
                    events.getDateTime(),
                    events.getCreatedBy().getName(),
                    events.getTicketTypes()
            );

        } catch (Exception e) {
            // Log the exception
            System.err.println("Error occurred while creating an event: " + e.getMessage());
            // Optionally throw a custom exception or return a fallback value
            throw new RuntimeException("Failed to create event. Please try again later.");
        }
    }


    public EventResponseDto getEventById(String id){
        Events events = eventRepository.findById(id)
                .orElseThrow(()-> new EventNotFoundInDb("Event with id :" + id + " could not be found"));
        return new EventResponseDto(
                events.getEventId(),
                events.getName(),
                events.getDescription(),
                events.getLocation(),
                events.getDateTime(),
                events.getCreatedBy().getName(),
                events.getTicketTypes()
        );    }



    public PageResponse<EventResponseDto> getAllEvents(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").descending());
        Page<Events> events = eventRepository.findAll(pageable);
        List<EventResponseDto> eventResponseDtos = events.stream()
                .map(event -> new EventResponseDto(
                        event.getEventId(),
                        event.getName(),
                        event.getDescription(),
                        event.getLocation(),
                        event.getDateTime(),
                        event.getCreatedBy().getName(),
                        event.getTicketTypes()

                )).collect(Collectors.toList());


        return new PageResponse<>(
                eventResponseDtos,
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isFirst(),
                events.isLast()
        );




    }


    @Transactional
    public String deleteEventById(String id, Authentication connectedUser) {
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Events event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!event.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this event");
        }

        eventRepository.delete(event);

        return "Event deleted successfully";
    }


    public List<EventResponseDto> getAllOrganizerEvents(Authentication connectedUser) {
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Events> events = eventRepository.findByCreatedBy(user);

        return events.stream().map(event -> new EventResponseDto(
                event.getEventId(),
                event.getName(),
                event.getDescription(),
                event.getLocation(),
                event.getDateTime(),
                user.getName(), // or event.getCreatedBy().getName()
                event.getTicketTypes()
        )).collect(Collectors.toList());
    }

    private  String generateEventId() {
         String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder("evt_");
        for (int i = 0; i < 9; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

}
