# AI Attendance — Minimal Demo

This repository contains a minimal demo for an attendance/time-tracking app:

- backend/ — Spring Boot (Java 17) + SQLite minimal API
- frontend/ — Vue 3 + Vite minimal UI
- docker-compose.yml — builds and runs backend and frontend containers

Goal: provide a small runnable system (<=10 users) using SQLite. Later we can add auth, CI, tests, and push to GitHub.

Quick start (requires Docker & Docker Compose):

```bash
# from repo root
docker compose build
docker compose up
```

Backend: http://localhost:8080
Frontend: http://localhost:3000

Next steps:
- Add authentication (JWT)
- Add tests and CI
- Improve UI and i18n

