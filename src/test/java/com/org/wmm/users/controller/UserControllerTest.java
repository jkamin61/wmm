package com.org.wmm.users.controller;

import com.org.wmm.auth.dto.UserInfo;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.users.entity.RoleEntity;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.entity.UserRoleEntity;
import com.org.wmm.users.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private UserController userController;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        RoleEntity adminRole = RoleEntity.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        UserRoleEntity userRole = UserRoleEntity.builder()
                .userId(1L)
                .roleId(1L)
                .role(adminRole)
                .build();

        testUser = UserEntity.builder()
                .id(1L)
                .email("admin@test.com")
                .passwordHash("$2a$10$hash")
                .passwordAlgo("bcrypt")
                .displayName("Admin")
                .isActive(true)
                .failedLoginAttempts(0)
                .userRoles(Set.of(userRole))
                .build();
    }

    @Test
    @DisplayName("GET /users/me — should return current user info")
    void shouldReturnCurrentUser() {
        // Set up SecurityContext
        var auth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(testUser);

        var response = userController.getCurrentUser();

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.isSuccess()).isTrue();

        UserInfo data = body.getData();
        assertThat(data.getId()).isEqualTo(1L);
        assertThat(data.getEmail()).isEqualTo("admin@test.com");
        assertThat(data.getDisplayName()).isEqualTo("Admin");
        assertThat(data.getRoles()).containsExactly("ROLE_ADMIN");

        verify(userDetailsService).getUserByEmail("admin@test.com");

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /users/me — should propagate exception when user not found")
    void shouldPropagateExceptionWhenUserNotFound() {
        var auth = new UsernamePasswordAuthenticationToken("ghost@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userDetailsService.getUserByEmail("ghost@test.com"))
                .thenThrow(new ResourceNotFoundException("User", "email", "ghost@test.com"));

        assertThatThrownBy(() -> userController.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@test.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /users/me — should map multiple roles correctly")
    void shouldMapMultipleRoles() {
        RoleEntity editorRole = RoleEntity.builder().id(2L).name("ROLE_EDITOR").build();
        UserRoleEntity editorUserRole = UserRoleEntity.builder()
                .userId(1L).roleId(2L).role(editorRole).build();

        // Add second role
        testUser.setUserRoles(Set.of(
                testUser.getUserRoles().iterator().next(),
                editorUserRole
        ));

        var auth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(testUser);

        var response = userController.getCurrentUser();
        UserInfo data = response.getBody().getData();

        assertThat(data.getRoles())
                .hasSize(2)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_EDITOR");

        SecurityContextHolder.clearContext();
    }
}

