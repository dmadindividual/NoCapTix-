package topg.Event_Platform.dto;

public record UserResponseDto(
        boolean success,
        UserDto data,
        String message
) {
}
