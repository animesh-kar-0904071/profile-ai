package com.ani0904071.profile_ai.chat;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank @Size(max = 500) String message
) {}
