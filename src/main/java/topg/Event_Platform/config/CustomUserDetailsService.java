package topg.Event_Platform.config;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import topg.Event_Platform.exceptions.InvalidRole;
import topg.Event_Platform.models.User;
import topg.Event_Platform.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            String role = user.getRole().name();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (role.equals("USER") || role.equals("ORGANIZER")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            } else {
                throw new InvalidRole("Unauthorized role: " + role);
            }

            System.out.println("User Roles: " + authorities);

            return new UserDetailsServiceImpl(
                    user.getEmail(),
                    user.getPassword(),
                    authorities
            );
        }
        throw new UsernameNotFoundException("User or Organizer not found: " + email);

    }
}
