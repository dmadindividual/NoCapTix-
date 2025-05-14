package topg.Event_Platform.service;


import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topg.Event_Platform.config.JwtUtils;
import topg.Event_Platform.config.UserDetailsServiceImpl;
import topg.Event_Platform.dto.*;
import topg.Event_Platform.enums.Role;
import topg.Event_Platform.exceptions.ErrorCreatingUser;
import topg.Event_Platform.exceptions.InvalidRole;
import topg.Event_Platform.exceptions.InvalidUserInputException;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PaymentService paymentService;



    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto){
        try {
            validateUserInput(userRequestDto);
            Role role = Role.valueOf(userRequestDto.role().toUpperCase());
            User user = User.builder()
                    .id(userIdGenerator(role))
                    .name(userRequestDto.name())
                    .password(passwordEncoder.encode(userRequestDto.password()))
                    .email(userRequestDto.email())
                    .role(role)
                    .ninNumber(userRequestDto.ninNumber())
                    .build();
            userRepository.save(user);

            if (role == Role.ORGANIZER) {
                if (StringUtils.isBlank(userRequestDto.accountNumber()) || StringUtils.isBlank(userRequestDto.bankCode())) {
                    throw new InvalidUserInputException("Organizer must provide bank account number and bank code.");
                }

                user.setAccountNumber(userRequestDto.accountNumber());
                user.setBankCode(userRequestDto.bankCode());

                // Call Paystack and get subaccount code
                String subaccountCode = paymentService.createSubaccount(user);
                user.setSubaccountCode(subaccountCode);
                ResolvedBankAccountDto dto = paymentService.resolveBankAccount(userRequestDto.accountNumber(), userRequestDto.bankCode());
                System.out.println(dto.accountName());  // Will print resolved account name if valid

                userRepository.save(user); // Save updated info
            }

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


    private void validateUserInput(UserRequestDto userRequestDto) {
        if (StringUtils.isBlank(userRequestDto.email()) ||
                StringUtils.isBlank(userRequestDto.password()) ||
                StringUtils.isBlank(userRequestDto.name())) {
            throw new InvalidUserInputException("Email, password, or username cannot be blank.");
        }
        if (userRepository.findByEmail(userRequestDto.email()).isPresent()) {
            throw new InvalidUserInputException("Email is already Taken");
        }

        if ("ORGANIZER".equalsIgnoreCase(userRequestDto.role())) {
            if (StringUtils.isBlank(userRequestDto.accountNumber()) || StringUtils.isBlank(userRequestDto.bankCode())) {
                throw new InvalidUserInputException("Organizers must provide account number and bank code.");
            }
        }

    }



    public UserResponseDto getUser( Authentication connectedUser) {
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDto dto = new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );

        return new UserResponseDto(true, dto, "User retrieved successfully");
    }



    public String deleteUserById( Authentication connectedUser) {
        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
        return "User with id " + user.getId() + " has been successfully deleted.";
    }


    public JwtResponseDto loginUser(LoginRequestDto loginRequestDto) {
        // Authenticate the user
        Authentication authentication = authenticateUser(loginRequestDto);

        // Set authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsServiceImpl userDetails = (UserDetailsServiceImpl) authentication.getPrincipal();
        // Generate JWT token for the authenticated user
        String jwt = jwtUtils.generateToken(userDetails);

        return new JwtResponseDto(true, jwt);
    }

    private Authentication authenticateUser(LoginRequestDto loginRequestDto) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.username(),
                        loginRequestDto.password()
                )
        );
    }


}
