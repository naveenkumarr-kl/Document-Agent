package com.example.capstone_project.controller;

import com.example.capstone_project.dto.LoginRequest;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.filter.JwtAuthFilter;
import com.example.capstone_project.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (HTTP) tests for {@link AuthController}.
 *
 * <p>{@code @MockBean} replaces the real {@link AuthService} with a Mockito mock
 * inside the Spring application context, allowing HTTP semantics (status codes,
 * URL mappings, JSON serialization) to be tested without a running database.
 *
 * @see AuthControllerTest for complementary {@code @InjectMocks} + {@code @Mock} unit tests.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class AuthControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Spring-managed mock – automatically injected into the AuthController
     * bean that lives in the Spring test application context.
     */
    @MockBean
    private AuthService authService;

    /** Mocks JwtAuthFilter to allow the Spring security context to start cleanly. */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    // ---------------------------------------------------------------
    // POST /api/capstone/v1/auth/register
    // ---------------------------------------------------------------

    @Test
    void register_shouldReturn201_whenNewUser() throws Exception {
        User newUser = new User(null, "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        when(authService.registerUser(any(User.class))).thenReturn(HttpStatus.CREATED);

        mockMvc.perform(post("/api/capstone/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated());

        verify(authService).registerUser(any(User.class));
    }

    @Test
    void register_shouldReturn208_whenUserAlreadyExists() throws Exception {
        User existingUser = new User(null, "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        when(authService.registerUser(any(User.class))).thenReturn(HttpStatus.ALREADY_REPORTED);

        mockMvc.perform(post("/api/capstone/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isAlreadyReported());

        verify(authService).registerUser(any(User.class));
    }

    // ---------------------------------------------------------------
    // POST /api/capstone/v1/auth/login
    // ---------------------------------------------------------------

    @Test
    void login_shouldReturn200WithToken_whenCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@test.com");
        loginRequest.setPassword("pass");
        when(authService.userLogin(any(LoginRequest.class))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/capstone/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-jwt-token"));

        verify(authService).userLogin(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturn200WithUserNotFound_whenEmailDoesNotExist() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unknown@test.com");
        loginRequest.setPassword("pass");
        when(authService.userLogin(any(LoginRequest.class))).thenReturn("USER NOT FOUND !!!");

        mockMvc.perform(post("/api/capstone/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("USER NOT FOUND !!!"));

        verify(authService).userLogin(any(LoginRequest.class));
    }
}
