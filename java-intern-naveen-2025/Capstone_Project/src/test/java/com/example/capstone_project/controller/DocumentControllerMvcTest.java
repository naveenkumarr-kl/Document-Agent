package com.example.capstone_project.controller;

import com.example.capstone_project.entity.Documents;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.filter.JwtAuthFilter;
import com.example.capstone_project.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (HTTP) tests for {@link DocumentController}.
 *
 * <p>{@code @MockBean} replaces the real {@link DocumentService} with a Mockito mock
 * inside the Spring application context, allowing HTTP semantics (status codes,
 * URL mappings, JSON serialization) to be tested without a running database.
 *
 * @see DocumentControllerTest for complementary {@code @InjectMocks} + {@code @Mock} unit tests.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
        controllers = DocumentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class DocumentControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Spring-managed mock – automatically injected into the DocumentController
     * bean that lives in the Spring test application context.
     */
    @MockBean
    private DocumentService documentService;

    /** Mocks JwtAuthFilter to allow the Spring security context to start cleanly. */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private Documents buildSampleDocument() {
        User owner = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        return new Documents("doc-1", "report.txt", "Sample content",
                LocalDateTime.now(), Documents.Status.UPLOADED, owner);
    }

    // ---------------------------------------------------------------
    // POST /api/capstone/v1/user/document/upload
    // ---------------------------------------------------------------

    @Test
    void upload_shouldReturn200WithSuccessMessage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.txt", MediaType.TEXT_PLAIN_VALUE, "Sample content".getBytes()
        );
        when(documentService.upload(any()))
                .thenReturn("FILE UPLOADED SUCCESSFULLY report.txt");

        mockMvc.perform(multipart("/api/capstone/v1/user/document/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("FILE UPLOADED SUCCESSFULLY report.txt"));

        verify(documentService).upload(any());
    }

    // ---------------------------------------------------------------
    // GET /api/capstone/v1/user/document/viewAll
    // ---------------------------------------------------------------

    @Test
    void viewAll_shouldReturn200WithPageOfDocuments() throws Exception {
        Page<Documents> page = new PageImpl<>(
                List.of(buildSampleDocument()), PageRequest.of(0, 10), 1
        );
        when(documentService.viewAll(0)).thenReturn(page);

        mockMvc.perform(get("/api/capstone/v1/user/document/viewAll")
                        .param("PageSize", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("report.txt"));

        verify(documentService).viewAll(0);
    }

    // ---------------------------------------------------------------
    // GET /api/capstone/v1/user/document/viewById
    // ---------------------------------------------------------------

    @Test
    void viewById_shouldReturn200WithDocument() throws Exception {
        Documents doc = buildSampleDocument();
        when(documentService.viewById("doc-1")).thenReturn(doc);

        mockMvc.perform(get("/api/capstone/v1/user/document/viewById")
                        .param("id", "doc-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-1"))
                .andExpect(jsonPath("$.name").value("report.txt"));

        verify(documentService).viewById("doc-1");
    }

    // ---------------------------------------------------------------
    // PATCH /api/capstone/v1/user/document/updateStatus
    // ---------------------------------------------------------------

    @Test
    void updateStatus_shouldReturn200WithSuccessMessage() throws Exception {
        when(documentService.updateStatus("doc-1", Documents.Status.APPROVED))
                .thenReturn("STATUS UPDATED SUCCESSFULLY !!");

        mockMvc.perform(patch("/api/capstone/v1/user/document/updateStatus")
                        .param("id", "doc-1")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(content().string("STATUS UPDATED SUCCESSFULLY !!"));

        verify(documentService).updateStatus("doc-1", Documents.Status.APPROVED);
    }

    // ---------------------------------------------------------------
    // DELETE /api/capstone/v1/user/document/removeById
    // ---------------------------------------------------------------

    @Test
    void removeById_shouldReturn200WithSuccessMessage() throws Exception {
        when(documentService.removeById("doc-1")).thenReturn("FILE REMOVED SUCCESSFULLY !!");

        mockMvc.perform(delete("/api/capstone/v1/user/document/removeById")
                        .param("id", "doc-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("FILE REMOVED SUCCESSFULLY !!"));

        verify(documentService).removeById("doc-1");
    }
}
