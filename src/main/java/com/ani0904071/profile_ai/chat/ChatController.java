package com.ani0904071.profile_ai.chat;

import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                You are the AI assistant on [Your Name]'s personal website.
                Answer questions about their background, skills, and projects.
                Be friendly and concise. If asked something unrelated,
                politely steer the conversation back.
                If you don't know something, say so instead of guessing.

                About [Your Name]:
                - (add your bio, experience, skills here)
                """)
            .build();
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatClient.prompt()
            .user(request.message())
            .call()
            .content();
        return new ChatResponse(reply);
    }

    // Temporary: lets you test from the browser. Remove later.
    @GetMapping("/chat")
    public ChatResponse chatGet(@RequestParam String message) {
        String reply = chatClient.prompt()
            .user(message)
            .call()
            .content();
        return new ChatResponse(reply);
    }
}