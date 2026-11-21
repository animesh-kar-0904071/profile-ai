# Profile AI

A personal profile website with an AI assistant that answers questions about me.

This is a monorepo with two parts:
- `profile-ai/`: Spring Boot backend that talks to Google Gemini
- `profile-app-ui/`: Angular frontend (the profile page and chat UI)

## Tech stack
- **Backend:** Java 21, Spring Boot 4, Spring AI, Google Gemini, Maven
- **Frontend:** Angular 21, TypeScript 5.9.2
- **Planned:** MySQL, Docker

## Features
- [x] `/api/chat` endpoint backed by Gemini
- [x] Streaming replies via Server-Sent Events (`/api/chat/stream`)
- [x] System prompt and profile loaded from files (`src/main/resources/prompts/`)
- [x] CORS, per-IP rate limiting and friendly error handling
- [ ] Prompt guardrails, 
- [ ] Angular chat UI with streaming
- [ ] Profile data and chat history in MySQL
- [ ] Tools (project lookup, send me a message)
- [ ] Docker setup and deployment

## Project structure
```
.
├── .github/                 # pull request template
├── profile-ai/              # Spring Boot backend
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ani0904071/profile_ai/
│       │   ├── ProfileAiApplication.java
│       │   ├── chat/        # controller and request/response records
│       │   └── config/      # CORS and rate limiting
│       └── resources/
│           ├── application.yml
│           └── prompts/     # system prompt and profile content
└── profile-app-ui/          # Angular frontend
```

## Run locally

Requirements: Java 21, Node.js (current LTS), and a free Gemini API key from [Google AI Studio](https://aistudio.google.com).

### Backend
```bash
cd profile-ai
export GEMINI_API_KEY=your-key-here
./mvnw spring-boot:run
```
The API runs at `http://localhost:8080`.

### Frontend
```bash
cd profile-app-ui
npm ci
npx ng serve
```
The app runs at `http://localhost:4200`.

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/chat` | Full reply as JSON |
| POST | `/api/chat/stream` | Reply streamed as Server-Sent Events |

Request body: `{"message": "..."}` (max 500 characters).

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What does this person do?"}'

curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me about yourself in 3 sentences"}'
```

## Configuration
- The API key is read from the `GEMINI_API_KEY` environment variable. Never commit keys to the repo.
- The model and token limit are set in `profile-ai/src/main/resources/application.yml`.
- Allowed frontend origins are set with `app.cors.allowed-origins` (defaults to `http://localhost:4200`).
- The assistant's instructions and profile content live in `profile-ai/src/main/resources/prompts/` (`system.st` and `profile.md`).
- Rate limit: 10 requests per minute per IP on `/api/chat` endpoints.