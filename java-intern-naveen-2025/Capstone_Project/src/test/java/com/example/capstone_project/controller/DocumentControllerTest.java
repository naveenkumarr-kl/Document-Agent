package com.example.capstone_project.controller;

import com.example.capstone_project.entity.Documents;
import com.example.capstone_project.entity.User;
import com.example.capstone_project.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DocumentController}.
 *
 * <p>Uses pure Mockito (no Spring context) to verify controller method logic:
 * <ul>
 *   <li>{@code @Mock}        – creates a lightweight Mockito mock of DocumentService.</li>
 *   <li>{@code @InjectMocks} – creates the DocumentController and injects the mock.</li>
 * </ul>
 *
 * @see DocumentControllerMvcTest for complementary {@code @MockBean} + MockMvc tests.
 */
@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    /** Mock dependency for DocumentController – injected via constructor by @InjectMocks. */
    @Mock
    private DocumentService documentService;

    /** Controller under test; Mockito injects {@code documentService} via constructor. */
    @InjectMocks
    private DocumentController documentController;

    private Documents sampleDocument;

    @BeforeEach
    void setUp() {
        User owner = new User("uid-1", "Alice", "alice@test.com", "pass", User.Role.CREATOR);
        sampleDocument = new Documents(
                "doc-1", "report.txt", "Sample content",
                LocalDateTime.now(), Documents.Status.UPLOADED, owner
        );
    }

    // ---------------------------------------------------------------
    // upload
    // ---------------------------------------------------------------

    @Test
    void upload_shouldReturnSuccessMessage() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file", "report.txt", "text/plain", "Sample content".getBytes()
        );
        when(documentService.upload(any(MultipartFile.class)))
                .thenReturn("FILE UPLOADED SUCCESSFULLY report.txt");

        ResponseEntity<String> response = documentController.upload(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FILE UPLOADED SUCCESSFULLY report.txt", response.getBody());
        verify(documentService).upload(file);
    }

    @Test
    void upload_shouldDelegateToDocumentService() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes()
        );
        when(documentService.upload(any(MultipartFile.class))).thenReturn("Uploaded");

        documentController.upload(file);

        verify(documentService, times(1)).upload(file);
    }

    // ---------------------------------------------------------------
    // viewAll
    // ---------------------------------------------------------------

    @Test
    void viewAll_shouldReturnPageOfDocuments() {
        Page<Documents> page = new PageImpl<>(
                List.of(sampleDocument), PageRequest.of(0, 10), 1
        );
        when(documentService.viewAll(0)).thenReturn(page);

        Page<Documents> result = documentController.viewAll(0);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("report.txt", result.getContent().get(0).getName());
        verify(documentService).viewAll(0);
    }

    @Test
    void viewAll_shouldReturnEmptyPage_whenNoDocuments() {
        Page<Documents> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(documentService.viewAll(0)).thenReturn(emptyPage);

        Page<Documents> result = documentController.viewAll(0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentService).viewAll(0);
    }

    // ---------------------------------------------------------------
    // viewById
    // ---------------------------------------------------------------

    @Test
    void viewById_shouldReturnDocument() {
        when(documentService.viewById("doc-1")).thenReturn(sampleDocument);

        ResponseEntity<Documents> response = documentController.viewById("doc-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("doc-1",      response.getBody().getId());
        assertEquals("report.txt", response.getBody().getName());
        verify(documentService).viewById("doc-1");
    }

    // ---------------------------------------------------------------
    // updateStatus
    // ---------------------------------------------------------------

    @Test
    void updateStatus_shouldReturnSuccessMessage() {
        when(documentService.updateStatus("doc-1", Documents.Status.APPROVED))
                .thenReturn("STATUS UPDATED SUCCESSFULLY !!");

        ResponseEntity<String> response =
                documentController.updateStatus("doc-1", Documents.Status.APPROVED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("STATUS UPDATED SUCCESSFULLY !!", response.getBody());
        verify(documentService).updateStatus("doc-1", Documents.Status.APPROVED);
    }

    @Test
    void updateStatus_shouldPassStatusToService() {
        when(documentService.updateStatus(any(), any())).thenReturn("done");

        documentController.updateStatus("doc-1", Documents.Status.REJECTED);

        verify(documentService, times(1)).updateStatus("doc-1", Documents.Status.REJECTED);
    }

    // ---------------------------------------------------------------
    // removeById
    // ---------------------------------------------------------------

    @Test
    void removeDocument_shouldReturnSuccessMessage() {
        when(documentService.removeById("doc-1")).thenReturn("FILE REMOVED SUCCESSFULLY !!");

        ResponseEntity<String> response = documentController.removeDocument("doc-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FILE REMOVED SUCCESSFULLY !!", response.getBody());
        verify(documentService).removeById("doc-1");
    }

    @Test
    void removeDocument_shouldReturnNotFound_whenFileDoesNotExist() {
        when(documentService.removeById("missing-id")).thenReturn("FILE DOESN'T EXIST");

        ResponseEntity<String> response = documentController.removeDocument("missing-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FILE DOESN'T EXIST", response.getBody());
        verify(documentService).removeById("missing-id");
    }
}
