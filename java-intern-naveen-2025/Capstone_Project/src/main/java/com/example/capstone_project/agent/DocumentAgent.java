package com.example.capstone_project.agent;


import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DocumentAgent {

    public static BaseAgent ROOT_AGENT = initAgent();

    private static BaseAgent initAgent() {
        return LlmAgent.builder()
                .name("hello-time-agent")
                .description("Tells the current time in a specified city")
                .instruction("""
                        You are a helpful assistant that tells the current time in a city.
                        Use the 'getCurrentTime' tool for this purpose.
                        """)
                .model("gemini-2.5-flash-lite")
                .tools(FunctionTool.create(DocumentAgent.class, "getCurrentTime"))
                .build();
    }

    @Schema(description = "Get the current time for a given city")
    public static Map<String, String> getCurrentTime(
            @Schema(name = "city", description = "Name of the city") String city) {

        try {
            ZoneId zoneId = ZoneId.of(city);
            LocalDateTime currentTime = LocalDateTime.now(zoneId);
            String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a, MMM dd yyyy"));
            return Map.of(
                    "city", city,
                    "current_time", formattedTime);
        } catch (Exception e) {
            return Map.of(
                    "error",
                    "Invalid city or time zone. Please provide a valid city name or time zone ID.");
        }
    }
}