# Profile AI

A personal profile website with an AI assistant that answers questions about me.
This repository is the **backend** (Spring Boot). The Angular frontend will live in a separate repo.

## Tech stack
- Java 21, Spring Boot 4
- Spring AI with Google Gemini
- Maven
- Planned: PostgreSQL, Angular frontend

## Features
- [x] `/api/chat` endpoint backed by Gemini
- [x] Streaming replies (Server-Sent Events)
- [ ] Rate limiting and error handling
- [ ] Profile data and chat history in MySQL
- [ ] Tools (project lookup, send me a message)

## Run locally

Requirements: Java 21 and a free Gemini API key from [Google AI Studio](https://aistudio.google.com).

```bash
export GEMINI_API_KEY=your-key-here
./mvnw spring-boot:run
```

Test it:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What does this person do?"}'
```

## Configuration
The API key is read from the `GEMINI_API_KEY` environment variable. Never commit keys to the repo.
The model is set in `src/main/resources/application.yml`.

## Project structure
```
src/main/java/com/ani0904071/profile_ai/
├── ProfileAiApplication.java
├── chat/     # controller and request/response records
└── config/   # CORS and other configuration
```
