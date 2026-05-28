package com.eventhub.auth;

import java.util.Set;
import java.util.stream.Collectors;

import com.eventhub.auth.dto.AuthResponse;
import com.eventhub.auth.dto.LoginRequest;
import com.eventhub.auth.dto.RegisterRequest;
import com.eventhub.domain.entities.AppUser;
import com.eventhub.domain.entities.Role;
import com.eventhub.domain.enums.RoleName;
import com.eventhub.repositories.AppUserRepository;
import com.eventhub.repositories.RoleRepository;
import com.eventhub.security.JwtService;
import com.eventhub.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        AppUser user = new AppUser();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name().trim());
        user.setSurname(request.surname().trim());
        user.setProfileImageUrl(request.profileImageUrl().trim());
        user.setCity(request.city());
        user.setFavoriteEventType(request.favoriteEventType());
        user.setRoles(Set.of(role));

        AppUser saved = userRepository.save(user);
        return buildResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return buildResponse(user);
    }

    private AuthResponse buildResponse(AppUser user) {
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getProfileImageUrl(),
                roles
        );
    }
}
