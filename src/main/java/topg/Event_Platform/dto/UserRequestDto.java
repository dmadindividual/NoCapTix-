package topg.Event_Platform.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Role is required")
        String role,

        @NotBlank(message = "NIN number is required")
        @Size(min = 11, max = 11, message = "NIN number must be 11 characters long")
        String ninNumber
) {}
