package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topg.Event_Platform.dto.TicketResponseDto;
import topg.Event_Platform.service.TicketTypeService;

@RestController
@RequestMapping("/api/v1/ticketCategory")
@RequiredArgsConstructor
public class TicketController {
    private final TicketTypeService ticketTypeService;

    @PostMapping
    public TicketResponseDto


}
