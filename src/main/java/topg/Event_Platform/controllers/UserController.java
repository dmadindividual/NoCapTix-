package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import topg.Event_Platform.dto.UserResponseDto;
import topg.Event_Platform.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("userId") String userId, Authentication connectedUser){
        UserResponseDto data = userService.getUser(userId, connectedUser);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable("userId") String userId, Authentication connectedUser){
        String data = userService.deleteUserById(userId, connectedUser);
        return ResponseEntity.ok(data);
    }
}
