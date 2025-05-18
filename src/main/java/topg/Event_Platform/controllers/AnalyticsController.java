package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import topg.Event_Platform.dto.EventStatsResponse;
import topg.Event_Platform.service.EventAnalytics;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final EventAnalytics eventAnalytics;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventStatsResponse> getEventStats(
            @PathVariable String eventId,
            Authentication authentication) {
        EventStatsResponse response = eventAnalytics.getEventStatsByOrganizerId(eventId, authentication);
        return ResponseEntity.ok(response);
    }
}
