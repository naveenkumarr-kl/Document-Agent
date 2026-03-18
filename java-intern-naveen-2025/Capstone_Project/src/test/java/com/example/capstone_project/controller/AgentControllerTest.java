package com.example.capstone_project.controller;

import com.example.capstone_project.dto.AgentResponse;
import com.example.capstone_project.dto.UserRequest;
import com.example.capstone_project.service.AgentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AgentController}.
 *
 * <p>The real {@link AgentService} establishes a live AI-agent session on
 * construction. Using {@code @Mock} prevents that expensive initialization and
 * keeps the tests fast and side-effect-free.
 *
 * <ul>
 *   <li>{@code @Mock}        – creates a lightweight Mockito mock of AgentService.</li>
 *   <li>{@code @InjectMocks} – creates AgentController and injects the mock.</li>
 * </ul>
 *
 * @see AgentControllerMvcTest for complementary {@code @MockBean} + MockMvc tests.
 */
@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    /** Mock dependency – prevents real AgentService from starting an AI session. */
    @Mock
    private AgentService agentService;

    /** Controller under test; Mockito injects {@code agentService} via constructor. */
    @InjectMocks
    private AgentController agentController;

    // ---------------------------------------------------------------
    // askQuestion
    // ---------------------------------------------------------------

    @Test
    void askQuestion_shouldReturnAgentResponse() {
        UserRequest request = new UserRequest("What time is it in New York?");
        when(agentService.askAgent("What time is it in New York?"))
                .thenReturn("The current time in New York is 3:00 PM EST");

        AgentResponse response = agentController.askQuestion(request);

        assertNotNull(response);
        assertEquals("The current time in New York is 3:00 PM EST", response.getResponse());
        verify(agentService).askAgent("What time is it in New York?");
    }

    @Test
    void askQuestion_shouldDelegateQuestionToService() {
        String question = "What time is it in Tokyo?";
        UserRequest request = new UserRequest(question);
        when(agentService.askAgent(question)).thenReturn("Some response");

        agentController.askQuestion(request);

        verify(agentService, times(1)).askAgent(question);
    }

    @Test
    void askQuestion_shouldReturnEmptyResponse_whenServiceReturnsEmpty() {
        UserRequest request = new UserRequest("Unknown query");
        when(agentService.askAgent("Unknown query")).thenReturn("");

        AgentResponse response = agentController.askQuestion(request);

        assertNotNull(response);
        assertEquals("", response.getResponse());
    }

    @Test
    void askQuestion_shouldPassExactQuestionTextToService() {
        String question = "How many documents are pending approval?";
        UserRequest request = new UserRequest(question);
        when(agentService.askAgent(question)).thenReturn("5 documents are pending approval.");

        AgentResponse response = agentController.askQuestion(request);

        assertEquals("5 documents are pending approval.", response.getResponse());
        verify(agentService).askAgent(question);
    }
}
