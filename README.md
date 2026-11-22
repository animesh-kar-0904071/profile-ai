# Profile AI

A personal profile website with an AI assistant that answers questions about me.

This is a monorepo with two parts:
- `profile-ai/`: Spring Boot backend that talks to Google Gemini
- `profile-app-ui/`: Angular frontend (profile page and streaming chat UI)

## Tech stack
- **Backend:** Java 21, Spring Boot 4, Spring AI, Google Gemini, Maven
- **Frontend:** Angular 21, TypeScript 5.9.2
- **Containers:** Docker, Docker Compose, nginx
- **Planned:** MySQL

## Features
- [x] `/api/chat` endpoint backed by Gemini
- [x] Streaming replies via Server-Sent Events (`/api/chat/stream`)
- [x] System prompt and profile loaded from files (`src/main/resources/prompts/`)
- [x] CORS, per-IP rate limiting, timeouts and friendly error messages
- [ ] Prompt guardrails
- [x] Angular chat UI with streaming, suggestion chips and error states
- [x] Docker setup (multi-stage builds, nginx reverse proxy)
- [ ] Markdown rendering in chat replies
- [ ] Profile data and chat history in MySQL
- [ ] Tools (project lookup, send me a message)
- [ ] Healthchecks, pinned image versions, CI
- [ ] Cloud deployment

## Project structure
```
.
├── .github/                        # pull request template
├── docker-compose.yml              # runs backend + frontend together
├── .env                            # GEMINI_API_KEY for Docker Compose (not committed)
├── profile-ai/                     # Spring Boot backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ani0904071/profile_ai/
│       │   ├── ProfileAiApplication.java
│       │   ├── chat/               # controller and request/response records
│       │   └── config/             # CORS and rate limiting
│       └── resources/
│           ├── application.yml
│           └── prompts/            # system.st (rules) and profile.md (content)
└── profile-app-ui/                 # Angular frontend
    ├── Dockerfile
    ├── nginx.conf                  # serves the app and proxies /api to the backend
    ├── proxy.conf.json             # dev proxy for `npm start`
    └── src/app/chat/               # chat component and streaming service
```

## Requirements
- Java 21
- Node.js (current LTS) and npm
- Docker with Docker Compose (only for the Docker option)
- A free Gemini API key from [Google AI Studio](https://aistudio.google.com)

## Option 1: Run locally (development)

### Backend
```bash
cd profile-ai
export GEMINI_API_KEY=your-key-here && ./mvnw spring-boot:run
```
The API runs at `http://localhost:8080`. Use `./mvnw clean spring-boot:run` after moving or renaming files.

### Frontend
In a second terminal:
```bash
cd profile-app-ui
npm ci
npm start
```
The app runs at `http://localhost:4200`. `npm start` uses `proxy.conf.json`, which forwards `/api` calls to `localhost:8080`.

## Option 2: Run with Docker

1. Create a `.env` file in the repo root, next to `docker-compose.yml`:
   ```
   GEMINI_API_KEY=your-key-here
   ```
   No quotes and no spaces around `=`. The file is git-ignored.

2. Build and start both containers:
   ```bash
   docker compose up --build
   ```

3. Open `http://localhost:8081`.

Useful commands:
```bash
docker compose up --build -d      # rebuild and run in the background
docker compose logs -f backend    # follow the backend log
docker compose ps                 # container status
docker compose config             # check the resolved configuration
docker compose down               # stop and remove the containers
```

The backend is only reachable inside the Docker network. nginx serves the Angular app on port 8081 and forwards `/api/` requests to the backend, with buffering turned off so streamed replies arrive word by word. After editing the prompt files or any code, run `docker compose up --build` again, because they are packaged into the image at build time.

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/chat` | Full reply as JSON |
| POST | `/api/chat/stream` | Reply streamed as Server-Sent Events |

Request body: `{"message": "..."}` (required, max 500 characters).

```bash
# Local backend (port 8080). Through Docker, use port 8081.
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What does this person do?"}'

curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me about yourself in 3 sentences"}'
```

Status codes: `200` success, `400` invalid message, `429` rate limit hit, `503` the model is busy or failed (a friendly message is returned).

Rate limit test (the first 10 print `200`, then `429`):
```bash
for i in $(seq 1 12); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/chat \
    -H "Content-Type: application/json" -d '{"message":"hi"}'
done
```

## Configuration

| Setting | Where | Notes |
|---|---|---|
| `GEMINI_API_KEY` | Environment variable, or `.env` for Docker | Never commit it |
| Model and token limit | `profile-ai/src/main/resources/application.yml` | Gemini model names change, so check AI Studio if you get a 404 |
| Allowed frontend origins | `app.cors.allowed-origins` (or env var `APP_CORS_ALLOWED_ORIGINS`) | Defaults to `http://localhost:4200`; comma-separated list |
| Rate limit | `config/RateLimitFilter.java` | 10 requests per minute per IP on `/api/chat*` |
| Assistant rules | `prompts/system.st` | Behavior and security guardrails; one placeholder, `{profile}` |
| Profile content | `prompts/profile.md` | Experience, skills, interests, contact |

Anything in the prompt files is visible to the model and can be extracted by a determined visitor, so keep secrets out of them.

## Troubleshooting
- **App fails with "Incomplete Google GenAI configuration":** `GEMINI_API_KEY` isn't set in the terminal that starts the app. Check with `echo "${#GEMINI_API_KEY}"` (a real key prints 39).
- **`API key not valid`:** the app got an old or dummy value. Stop every running instance and start again from a terminal that has the right key.
- **Model 404 or 503:** the model name is retired or Google is overloaded. Try another model from AI Studio and update `application.yml`.
- **`./mvnw: Permission denied`:** run `chmod +x mvnw`.
- **`bad interpreter` from `mvnw`:** Windows line endings. Run `sed -i 's/\r$//' mvnw`.
- **Docker says `GEMINI_API_KEY` is missing:** put it in the `.env` file next to `docker-compose.yml`.
- **Port already in use:** stop the other process, or change the port mapping.
- **Replies arrive all at once through nginx:** check that `proxy_buffering off;` is in `profile-app-ui/nginx.conf`, then rebuild.

## Deployment notes
- The frontend is static and can be hosted on Cloudflare Pages, Netlify or similar (base directory `profile-app-ui`, build command `npm run build`).
- The backend can be built from `profile-ai/Dockerfile` on platforms such as Railway, Render or Google Cloud Run (root directory `profile-ai`). Set `GEMINI_API_KEY` in the platform's settings.
- With the frontend and backend on different hosts, point the Angular service at the backend's full URL and set `APP_CORS_ALLOWED_ORIGINS` to the frontend's URL.
- Set a quota or spending cap on your Google account before sharing the site publicly.