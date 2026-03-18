package com.example.capstone_project.controller;

import com.example.capstone_project.entity.User;
import com.example.capstone_project.filter.JwtAuthFilter;
import com.example.capstone_project.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (HTTP) tests for {@link AdminController}.
 *
 * <p>{@code @MockBean} replaces the real {@link AdminService} with a Mockito
 * mock inside the Spring application context, allowing HTTP semantics
 * (status codes, URL mappings, JSON serialization) to be tested without a
 * running database or security layer.
 *
 * @see AdminControllerTest for complementary {@code @InjectMocks} + {@code @Mock} unit tests.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class AdminControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Spring-managed mock – automatically injected into the AdminController
     * bean that lives in the Spring test application context.
     */
    @MockBean
    private AdminService adminService;

    /**
     * Mocks JwtAuthFilter so the Spring context can start without a real
     * security setup (JwtFilter depends on JwtUtil which needs the DB layer).
     */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    // ---------------------------------------------------------------
    // GET /api/capstone/v1/admin/viewAll
    // ---------------------------------------------------------------

    @Test
    void viewAll_shouldReturn200WithListOfUsers() throws Exception {
        User alice = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.ADMIN);
        User bob   = new User("uid-2", "Bob",   "bob@test.com",   "pass", User.Role.CREATOR);
        when(adminService.viewAll()).thenReturn(Arrays.asList(alice, bob));

        mockMvc.perform(get("/api/capstone/v1/admin/viewAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(adminService).viewAll();
    }

    @Test
    void viewAll_shouldReturn200WithEmptyList_whenNoUsers() throws Exception {
        when(adminService.viewAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/capstone/v1/admin/viewAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(adminService).viewAll();
    }

    // ---------------------------------------------------------------
    // DELETE /api/capstone/v1/admin/deleteByEmail
    // ---------------------------------------------------------------

    @Test
    void deleteByEmail_shouldReturn200WithSuccessMessage() throws Exception {
        when(adminService.deleteByEmail("alice@test.com"))
                .thenReturn("USER DELETED SUCCESSFULLY !! ");

        mockMvc.perform(delete("/api/capstone/v1/admin/deleteByEmail")
                        .param("email", "alice@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("USER DELETED SUCCESSFULLY !! "));

        verify(adminService).deleteByEmail("alice@test.com");
    }

    // ---------------------------------------------------------------
    // GET /api/capstone/v1/admin/sortByRole
    // ---------------------------------------------------------------

    @Test
    void sortByRole_shouldReturn200WithFilteredUsers() throws Exception {
        User admin = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.ADMIN);
        when(adminService.sortByRole("ADMIN")).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/capstone/v1/admin/sortByRole")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));

        verify(adminService).sortByRole("ADMIN");
    }
}
