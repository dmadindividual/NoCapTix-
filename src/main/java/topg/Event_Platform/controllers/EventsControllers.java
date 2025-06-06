package topg.Event_Platform.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import topg.Event_Platform.dto.EventRequestDto;
import topg.Event_Platform.dto.EventResponseDto;
import topg.Event_Platform.dto.PageResponse;
import topg.Event_Platform.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventsControllers {

    private final EventService eventService;


    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto eventRequestDto, Authentication connectedUser){
        EventResponseDto data = eventService.createEvent(eventRequestDto, connectedUser);
        return ResponseEntity.ok(data);
    }


    @GetMapping
    public ResponseEntity<PageResponse<EventResponseDto>> getAllEvents( @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size){
        PageResponse<EventResponseDto> data = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getAllEventsById( @PathVariable("eventId") String id){
        EventResponseDto data = eventService.getEventById(id);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/delete/{eventId}")
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<String> deleteEventById( @PathVariable("eventId") String id, Authentication connectedUser){
        String data = eventService.deleteEventById(id, connectedUser);
        return ResponseEntity.ok(data);
    }




}
