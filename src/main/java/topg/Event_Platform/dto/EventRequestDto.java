package topg.Event_Platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequestDto(
        @NotBlank
        @Size(min = 5, max = 50, message = "Event name must be between 5 and 50 characters")
        String name,
        @NotBlank
        @Size(min = 10, max = 200, message = "description must be between 10 and 200 characters")
        String description,
        @NotBlank
        @Size(min = 5, max = 50, message = "Location must be between 5 and 100 characters")
        String location,
        @NotNull
        @Future
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dateTime) {
}
