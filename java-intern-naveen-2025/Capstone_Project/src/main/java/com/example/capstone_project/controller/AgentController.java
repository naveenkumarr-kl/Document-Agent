package com.example.capstone_project.controller;

import com.example.capstone_project.dto.AgentResponse;
import com.example.capstone_project.dto.UserRequest;
import com.example.capstone_project.service.AgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/ask")
    public AgentResponse askQuestion(@RequestBody UserRequest userRequest) {

        String answer = agentService.askAgent(userRequest.getQuestion());

        return new AgentResponse(answer);
    }
}