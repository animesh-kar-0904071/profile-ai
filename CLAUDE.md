# Profile AI

Personal profile site with an AI assistant. Monorepo: Spring Boot backend
(`profile-ai/`) and Angular frontend (`profile-app-ui/`), run together with
Docker Compose. Architecture and ports: docs/architecture.md

## Commands
- Backend: `cd profile-ai && export GEMINI_API_KEY=... && ./mvnw clean spring-boot:run` (port 8080)
- Frontend: `cd profile-app-ui && npm install && npm start` (port 4200, proxies /api to 8080)
- Docker: `docker compose up --build -d`, then `docker compose ps`; open http://localhost:8081
- Backend tests: `cd profile-ai && ./mvnw test` (the Docker build skips tests)

## Conventions
- Java 21, Spring Boot 4, Spring AI. Records for request/response objects.
- Packages: `chat/` (controller, DTOs), `config/` (CORS, rate limiting).
- Prompt files live in `profile-ai/src/main/resources/prompts/`; `system.st` has exactly one placeholder, `{profile}`.
- One branch per change, PRs use `.github/pull_request_template.md`.

## Rules
- Never commit API keys or `.env`. Never put secrets in prompt files: the model can be tricked into repeating them.
- Do not publish the backend port in Compose; all traffic goes through nginx.
- Ask before adding a dependency.

## Gotchas learned
- Spring AI 2.x property keys: `spring.ai.google.genai.chat.model` (no `options` level).
- Gemini model names change and 503 means overloaded; check names in AI Studio.
- `GEMINI_API_KEY` must be exported in the same terminal that starts the app; Compose reads `.env` next to `docker-compose.yml`.
- After moving or renaming Java files, run `./mvnw clean`.
- nginx needs `proxy_buffering off` or streamed replies arrive all at once.

## Working style
I am learning while building. Explain why for each change, keep steps small, and mention trade-offs.X
