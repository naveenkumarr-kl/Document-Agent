package com.example.capstone_project.controller;

import com.example.capstone_project.dto.UserRequest;
import com.example.capstone_project.filter.JwtAuthFilter;
import com.example.capstone_project.service.AgentService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (HTTP) tests for {@link AgentController}.
 *
 * <p>{@code @MockBean} replaces the real {@link AgentService} (which would otherwise
 * start a live AI-agent session) with a Mockito mock inside the Spring application
 * context, keeping tests fast and side-effect-free.
 *
 * @see AgentControllerTest for complementary {@code @InjectMocks} + {@code @Mock} unit tests.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
        controllers = AgentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class AgentControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Spring-managed mock – prevents the real AgentService (which calls an external
     * AI runtime) from being created, and wires a controllable double into the
     * Spring-managed AgentController bean.
     */
    @MockBean
    private AgentService agentService;

    /** Mocks JwtAuthFilter to allow the Spring security context to start cleanly. */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    // ---------------------------------------------------------------
    // POST /agent/ask
    // ---------------------------------------------------------------

    @Test
    void askQuestion_shouldReturn200WithAgentResponse() throws Exception {
        UserRequest request = new UserRequest("What time is it in New York?");
        when(agentService.askAgent(anyString()))
                .thenReturn("The current time in New York is 3:00 PM EST");

        mockMvc.perform(post("/agent/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response")
                        .value("The current time in New York is 3:00 PM EST"));

        verify(agentService).askAgent("What time is it in New York?");
    }

    @Test
    void askQuestion_shouldReturn200WithEmptyResponse_whenServiceReturnsEmpty() throws Exception {
        UserRequest request = new UserRequest("Unknown query");
        when(agentService.askAgent(anyString())).thenReturn("");

        mockMvc.perform(post("/agent/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(""));

        verify(agentService).askAgent("Unknown query");
    }

    @Test
    void askQuestion_shouldDelegateExactQuestionToService() throws Exception {
        String question = "List all uploaded documents.";
        UserRequest request = new UserRequest(question);
        when(agentService.askAgent(question)).thenReturn("There are 3 uploaded documents.");

        mockMvc.perform(post("/agent/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(agentService, times(1)).askAgent(question);
    }
}
