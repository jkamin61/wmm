package com.org.wmm.users.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        UserEntity user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive");
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getUserRoles().stream()
                        .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.OffsetDateTime.now()))
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}

