package topg.Event_Platform.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import topg.Event_Platform.dto.JwtResponseDto;
import topg.Event_Platform.dto.LoginRequestDto;
import topg.Event_Platform.dto.UserRequestDto;
import topg.Event_Platform.dto.UserResponseDto;
import topg.Event_Platform.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllers {
    private final UserService userService;

    @PostMapping("/create-account")
    public ResponseEntity<UserResponseDto> createAccount(@Valid @RequestBody UserRequestDto userRequestDto){
        UserResponseDto data = userService.createUser(userRequestDto);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/login")
    public  ResponseEntity<JwtResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        JwtResponseDto data = userService.loginUser(loginRequestDto);
        return ResponseEntity.ok(data);
    }



}
