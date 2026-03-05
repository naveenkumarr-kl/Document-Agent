package com.example.capstone_project.service;


import com.example.capstone_project.agent.DocumentAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final InMemoryRunner runner;
    private final Session session;

    public AgentService() {

        RunConfig runConfig = RunConfig.builder().build();
        runner = new InMemoryRunner(DocumentAgent.ROOT_AGENT);

        session = runner.sessionService()
                .createSession(runner.appName(), "user123")
                .blockingGet();
    }

    public String askAgent(String question) {

        RunConfig runConfig = RunConfig.builder().build();

        Content userMsg = Content.fromParts(Part.fromText(question));

        Flowable<Event> events =
                runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

        final StringBuilder response = new StringBuilder();

        events.blockingForEach(event -> {
            if (event.finalResponse()) {
                response.append(event.stringifyContent());
            }
        });

        return response.toString();
    }
}