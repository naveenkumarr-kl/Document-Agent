package com.example.capstone_project.controller;

import com.example.capstone_project.dto.LoginRequest;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthController}.
 *
 * <p>Uses pure Mockito (no Spring context) to verify controller method logic:
 * <ul>
 *   <li>{@code @Mock}        – creates a lightweight Mockito mock of AuthService.</li>
 *   <li>{@code @InjectMocks} – creates the AuthController and injects the mock.</li>
 * </ul>
 *
 * @see AuthControllerMvcTest for complementary {@code @MockBean} + MockMvc tests.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    /** Mock dependency for AuthController – injected via constructor by @InjectMocks. */
    @Mock
    private AuthService authService;

    /** Controller under test; Mockito injects {@code authService} via constructor. */
    @InjectMocks
    private AuthController authController;

    // ---------------------------------------------------------------
    // userRegister
    // ---------------------------------------------------------------

    @Test
    void userRegister_shouldReturnCreatedStatus_whenNewUser() {
        User newUser = new User(null, "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        when(authService.registerUser(any(User.class))).thenReturn(HttpStatus.CREATED);

        ResponseEntity<HttpStatus> response = authController.userRegister(newUser);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).registerUser(newUser);
    }

    @Test
    void userRegister_shouldReturnAlreadyReported_whenUserAlreadyExists() {
        User existingUser = new User(null, "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        when(authService.registerUser(any(User.class))).thenReturn(HttpStatus.ALREADY_REPORTED);

        ResponseEntity<HttpStatus> response = authController.userRegister(existingUser);

        assertNotNull(response);
        assertEquals(HttpStatus.ALREADY_REPORTED, response.getStatusCode());
        verify(authService).registerUser(existingUser);
    }

    @Test
    void userRegister_shouldDelegateToAuthService() {
        User user = new User(null, "Bob", "bob@test.com", "pass", User.Role.CREATOR);
        when(authService.registerUser(user)).thenReturn(HttpStatus.CREATED);

        authController.userRegister(user);

        verify(authService, times(1)).registerUser(user);
    }

    // ---------------------------------------------------------------
    // userLogin
    // ---------------------------------------------------------------

    @Test
    void userLogin_shouldReturnJwtToken_whenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("pass");
        when(authService.userLogin(any(LoginRequest.class))).thenReturn("mock-jwt-token");

        ResponseEntity<String> response = authController.userLogin(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mock-jwt-token", response.getBody());
        verify(authService).userLogin(loginRequest);
    }

    @Test
    void userLogin_shouldReturnUserNotFound_whenUserDoesNotExist() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unknown@test.com");
        loginRequest.setPassword("pass");
        when(authService.userLogin(any(LoginRequest.class))).thenReturn("USER NOT FOUND !!!");

        ResponseEntity<String> response = authController.userLogin(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("USER NOT FOUND !!!", response.getBody());
        verify(authService).userLogin(loginRequest);
    }

    @Test
    void userLogin_shouldReturnInvalidCredentials_whenPasswordIsWrong() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("wrong");
        when(authService.userLogin(any(LoginRequest.class))).thenReturn("INVALID CREDENTIALS !!");

        ResponseEntity<String> response = authController.userLogin(loginRequest);

        assertEquals("INVALID CREDENTIALS !!", response.getBody());
        verify(authService).userLogin(loginRequest);
    }
}
