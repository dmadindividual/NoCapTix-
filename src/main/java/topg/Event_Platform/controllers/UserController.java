package topg.Event_Platform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import topg.Event_Platform.dto.UserResponseDto;
import topg.Event_Platform.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser( Authentication connectedUser){
        UserResponseDto data = userService.getUser( connectedUser);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/delete/me")
    public ResponseEntity<String> deleteUserById( Authentication connectedUser){
        String data = userService.deleteUserById( connectedUser);
        return ResponseEntity.ok(data);
    }
}
