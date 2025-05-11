package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topg.Event_Platform.dto.EventResponseDto;
import topg.Event_Platform.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizer/events")
@RequiredArgsConstructor
public class OrganizersController {
    private final EventService eventService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<List<EventResponseDto>> getAllOrganizerEvents(Authentication connectedUser){
        List<EventResponseDto> data = eventService.getAllOrganizerEvents( connectedUser);
        return ResponseEntity.ok(data);
    }


}
