package com.example.capstone_project.controller;

import com.example.capstone_project.entity.User;
import com.example.capstone_project.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminController}.
 *
 * <p>Uses pure Mockito (no Spring context) to verify controller method logic:
 * <ul>
 *   <li>{@code @Mock}        – creates a lightweight Mockito mock of AdminService.</li>
 *   <li>{@code @InjectMocks} – creates the AdminController and injects the mock.</li>
 * </ul>
 *
 * @see AdminControllerMvcTest for complementary {@code @MockBean} + MockMvc tests.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    /** Mock dependency for AdminController – injected via constructor by @InjectMocks. */
    @Mock
    private AdminService adminService;

    /** Controller under test; Mockito injects {@code adminService} via constructor. */
    @InjectMocks
    private AdminController adminController;

    // ---------------------------------------------------------------
    // viewAll
    // ---------------------------------------------------------------

    @Test
    void viewAll_shouldReturnAllUsers() {
        User alice = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.ADMIN);
        User bob   = new User("uid-2", "Bob",   "bob@test.com",   "pass", User.Role.CREATOR);
        when(adminService.viewAll()).thenReturn(Arrays.asList(alice, bob));

        List<User> result = adminController.viewAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob",   result.get(1).getName());
        verify(adminService).viewAll();
    }

    @Test
    void viewAll_shouldReturnEmptyList_whenNoUsers() {
        when(adminService.viewAll()).thenReturn(List.of());

        List<User> result = adminController.viewAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adminService).viewAll();
    }

    // ---------------------------------------------------------------
    // deleteByEmail
    // ---------------------------------------------------------------

    @Test
    void deleteByEmail_shouldReturnSuccessMessage() {
        when(adminService.deleteByEmail("alice@test.com"))
                .thenReturn("USER DELETED SUCCESSFULLY !! ");

        ResponseEntity<String> response = adminController.deleteByEmail("alice@test.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("USER DELETED SUCCESSFULLY !! ", response.getBody());
        verify(adminService).deleteByEmail("alice@test.com");
    }

    @Test
    void deleteByEmail_shouldPassEmailToService() {
        String email = "test@example.com";
        when(adminService.deleteByEmail(email)).thenReturn("USER DELETED SUCCESSFULLY !! ");

        adminController.deleteByEmail(email);

        verify(adminService, times(1)).deleteByEmail(email);
    }

    // ---------------------------------------------------------------
    // sortByRole
    // ---------------------------------------------------------------

    @Test
    void sortByRole_shouldReturnFilteredUsers() {
        User admin = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.ADMIN);
        when(adminService.sortByRole("ADMIN")).thenReturn(List.of(admin));

        ResponseEntity<List<User>> response = adminController.sortByRole("ADMIN");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(User.Role.ADMIN, response.getBody().get(0).getRole());
        verify(adminService).sortByRole("ADMIN");
    }

    @Test
    void sortByRole_shouldReturnEmptyList_whenNoUsersWithGivenRole() {
        when(adminService.sortByRole("APPROVER")).thenReturn(List.of());

        ResponseEntity<List<User>> response = adminController.sortByRole("APPROVER");

        assertNotNull(response);
        assertTrue(response.getBody().isEmpty());
        verify(adminService).sortByRole("APPROVER");
    }
}
