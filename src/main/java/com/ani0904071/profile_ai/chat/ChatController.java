package com.ani0904071.profile_ai.chat;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private static final String BUSY_MESSAGE =
    "Sorry, the assistant is busy right now. Please try again in a moment.";

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
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            String reply = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
            return ResponseEntity.ok(new ChatResponse(reply));
        } catch (Exception e) {
            log.error("Chat call failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ChatResponse(BUSY_MESSAGE));
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) {
        return chatClient.prompt()
            .user(request.message())
            .stream()
            .content()
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(e -> {
            log.error("Chat stream failed", e);
            return Flux.just(BUSY_MESSAGE);
        });
}
}