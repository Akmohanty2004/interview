package com.interviewprep.controller;

import com.interviewprep.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiChatController {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Prompt is required", null));
        }

        String query = prompt.trim();

        // 1. Fast evaluation for simple arithmetic expressions (e.g. 1+2, 5 * 10)
        String mathAnswer = evaluateSimpleMath(query);
        if (mathAnswer != null) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Success", Map.of("answer", mathAnswer)));
        }

        // 2. Call live AI model via POST with crisp, ChatGPT/Gemini-style instructions
        try {
            String requestBody = "{\"messages\":[" +
                    "{\"role\":\"system\",\"content\":\"You are a smart, concise AI assistant like ChatGPT/Gemini. Give direct, short, accurate, and simple answers. Do not use filler or long robotic templates. Keep answers crisp, clear, and direct.\"}," +
                    "{\"role\":\"user\",\"content\":\"" + escapeJson(query) + "\"}" +
                    "]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://text.pollinations.ai/"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "InterviewPrep-Server/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank() && !response.body().equals("{}")) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Success", Map.of("answer", response.body())));
            }
        } catch (Exception ex) {
            // fallback
        }

        // 3. Fallback direct short answer if remote AI is slow
        String fallbackAnswer = getDirectShortAnswer(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", Map.of("answer", fallbackAnswer)));
    }

    private String evaluateSimpleMath(String q) {
        String cleaned = q.toLowerCase().replace("what is", "").replace("calculate", "").replace("?", "").trim();
        if (cleaned.matches("^\\d+(?:\\.\\d+)?\\s*[\\+\\-\\*/%^]\\s*\\d+(?:\\.\\d+)?(?:\\s*[\\+\\-\\*/%^]\\s*\\d+(?:\\.\\d+)?)*$")) {
            try {
                String[] parts = cleaned.split("(?<=[\\+\\-\\*/])|(?=[\\+\\-\\*/])");
                if (parts.length == 3) {
                    double a = Double.parseDouble(parts[0].trim());
                    String op = parts[1].trim();
                    double b = Double.parseDouble(parts[2].trim());
                    double res = 0;
                    switch (op) {
                        case "+": res = a + b; break;
                        case "-": res = a - b; break;
                        case "*": res = a * b; break;
                        case "/": res = b != 0 ? a / b : 0; break;
                    }
                    if (res == (long) res) {
                        return String.valueOf((long) res);
                    }
                    return String.valueOf(res);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String getDirectShortAnswer(String query) {
        String q = query.toLowerCase();
        if (q.contains("polymorphism")) {
            return "Polymorphism means 'many forms'. In Java, it allows objects to perform single actions in different ways via Method Overloading (Compile-Time) and Method Overriding (Runtime).";
        }
        if (q.contains("inheritance")) {
            return "Inheritance is an OOP mechanism where one class acquires the properties and methods of another class, promoting code reusability using the 'extends' keyword.";
        }
        if (q.contains("encapsulation")) {
            return "Encapsulation is bundling data (variables) and methods operating on that data into a single unit (class), while hiding internal details using private access modifiers and getters/setters.";
        }
        if (q.contains("deadlock")) {
            return "Deadlock occurs in multi-threading when two or more threads are blocked forever, each waiting for a lock held by the other in a circular chain.";
        }
        if (q.contains("what is java") || q.equals("java")) {
            return "Java is a popular, class-based, object-oriented programming language designed to be platform-independent (\"Write Once, Run Anywhere\") using the Java Virtual Machine (JVM).";
        }
        if (q.contains("president of india")) {
            return "The current President of India is Droupadi Murmu, who has been in office since July 25, 2022.";
        }
        if (q.contains("what is react") || q.equals("react")) {
            return "React is an open-source JavaScript library developed by Meta for building fast, component-based user interfaces with a Virtual DOM.";
        }
        if (q.contains("what is spring boot") || q.equals("spring boot")) {
            return "Spring Boot is an open-source Java framework used to easily build stand-alone, production-ready REST APIs and microservices.";
        }
        if (q.contains("what is sql") || q.equals("sql")) {
            return "SQL (Structured Query Language) is the standard programming language used for storing, querying, and managing relational databases.";
        }
        return "I am here to help you! Please ask any question about Java, Spring Boot, React, Data Structures, or system design.";
    }
}
