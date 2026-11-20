package com.ani0904071.profile_ai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_IP = 5; 
    private static final long WINDOW_MS = 60_000;
    // <ip:203.0.113.5, Window(startTime, 2)>
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private static class Window {
        final long start;
        int count;
        Window(long start, int count) { this.start = start; this.count = count; }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/chat")
                || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        
        // keep memory bounded
        if (windows.size() > 10_000) {   
            windows.entrySet().removeIf(e -> now - e.getValue().start >= WINDOW_MS);
        }

        Window w = windows.compute(request.getRemoteAddr(), (ip, current) -> {
            if (current == null || now - current.start >= WINDOW_MS) {
                return new Window(now, 1);
            }
            current.count++;
            return current;
        });

        if (w.count > MAX_REQUESTS_PER_IP) {
            long retryAfter = Math.max(1, (WINDOW_MS - (now - w.start)) / 1000);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Too many requests. Please wait a moment and try again.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}