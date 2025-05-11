package topg.Event_Platform.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topg.Event_Platform.dto.TicketRequestDto;
import topg.Event_Platform.dto.TicketResponseDto;
import topg.Event_Platform.service.TicketTypeService;

@RestController
@RequestMapping("/api/v1/ticketCategory")
@RequiredArgsConstructor
public class TicketController {
    private final TicketTypeService ticketTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<TicketResponseDto> createTicket(@Valid @RequestBody TicketRequestDto ticketRequestDto, Authentication connectedUser){
        TicketResponseDto data = ticketTypeService.createTicket(ticketRequestDto, connectedUser);
        return  ResponseEntity.ok(data);
    }


}
