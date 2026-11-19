package com.ani0904071.profile_ai.chat;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(
            ChatClient.Builder builder,
            @Value("classpath:prompts/system.st") Resource systemPrompt,
            @Value("classpath:prompts/profile.md") Resource profile) throws IOException {
    
        String profileText = profile.getContentAsString(StandardCharsets.UTF_8);
    
        this.chatClient = builder
            .defaultSystem(s -> s.text(systemPrompt).param("profile", profileText))
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

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) {
        return chatClient.prompt()
            .user(request.message())
            .stream()
            .content();
    }
}