package topg.Event_Platform.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topg.Event_Platform.dto.UserDto;
import topg.Event_Platform.dto.UserRequestDto;
import topg.Event_Platform.dto.UserResponseDto;
import topg.Event_Platform.enums.Role;
import topg.Event_Platform.exceptions.ErrorCreatingUser;
import topg.Event_Platform.exceptions.InvalidRole;
import topg.Event_Platform.exceptions.UserNotFoundInDataBase;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto){
        try {
            Role role = Role.valueOf(userRequestDto.role().toUpperCase());

            User user = User.builder()
                    .id(userIdGenerator(role))
                    .name(userRequestDto.name())
                    .password(userRequestDto.password())
                    .email(userRequestDto.email())
                    .role(role)
                    .ninNumber(userRequestDto.ninNumber())
                    .build();

            userRepository.save(user);

            UserDto data = new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return new UserResponseDto(true, data, "User created successfully");

        } catch (InvalidRole e) {
            return new UserResponseDto(false, null, "Invalid role: " + userRequestDto.role());
        } catch (ErrorCreatingUser e) {
            return new UserResponseDto(false, null, "An error occurred while creating the user: " + e.getMessage());
        }
    }

    private String userIdGenerator(Role role){
        String prefix = switch (role){
            case USER -> "user";
            case ORGANIZER -> "organizer";
        };
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0,7);
        return prefix + "-" + datePart + "-" + uniquePart;

    }

    public UserResponseDto getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundInDataBase("User not found"));

        UserDto dto = new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );

        return new UserResponseDto(true, dto, "User retrieved successfully");
    }




}
