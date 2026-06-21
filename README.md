# TypeAhead

TypeAhead is a local development project that demonstrates a distributed
search autocomplete system using:

- Java + Spring Boot backend
- Next.js frontend
- PostgreSQL as the source of truth
- Redis as a cache layer
- A one-shot ORCAS dataset loader for realistic search popularity data

## What this project does

1. Accepts prefix search requests from the frontend.
2. Routes each prefix to one of three standalone Redis nodes using
   application-level consistent hashing.
3. Returns cached suggestions when available.
4. Falls back to PostgreSQL on a cache miss, then populates Redis.
5. Buffers search submissions in memory and flushes them in batches to PostgreSQL.
6. Invalidates affected cached prefixes after each batch write.

## Key features

- Cache-aside design with Redis as a non-durable cache only.
- PostgreSQL as the durable source of truth.
- Three independent Redis nodes with custom routing logic.
- Batch aggregation of search submissions every 30 seconds.
- Real query popularity seeded from the Microsoft ORCAS dataset.

## Repository structure

- `backend/` — Spring Boot application, service logic, cache routing, DB access
- `frontend/` — Next.js app with search UI and API proxy
- `docker/` — Docker Compose stack, Postgres schema, ORCAS loader

## Quick start

1. Open PowerShell.
2. Change to the `docker` directory:

```powershell
cd c:\Users\MUKUND BHARADWAJ\Downloads\TypeAhead\docker
```

3. Start the stack:

```powershell
docker compose up --build
```

4. When the stack is ready, open:

- Frontend: `http://localhost:3001`
- Backend API: `http://localhost:8080`

## Local ports

The default port mappings are controlled through `docker/.env`:

- `POSTGRES_HOST_PORT` — PostgreSQL host port
- `BACKEND_HOST_PORT` — Backend host port
- `FRONTEND_HOST_PORT` — Frontend host port

The stack exposes services on `127.0.0.1` only.

## Docker configuration

Use `docker/.env.example` as a template:

```powershell
cp .env.example .env
```

Then update the values as needed.

Current environment variables supported in `docker/.env`:

- `DOCKER_PLATFORM` — platform override for Docker images
- `POSTGRES_DB` — database name
- `POSTGRES_USER` — database user
- `POSTGRES_PASSWORD` — database password
- `ORCAS_TOP_N` — number of top ORCAS queries to import
- `POSTGRES_HOST_PORT` — host port for Postgres
- `BACKEND_HOST_PORT` — host port for backend
- `FRONTEND_HOST_PORT` — host port for frontend

## Dataset loader

The dataset loader downloads the ORCAS query dataset and loads the top N
queries into PostgreSQL.

- It caches downloads on the `orcasdata` volume.
- It avoids reloading data when the table is already populated.
- Use `ORCAS_TOP_N` to change how many queries are imported.

## Running the app

Once the stack is running, use these endpoints:

- Frontend UI: `http://localhost:3001`
- Suggestion API: `http://localhost:8080/api/suggestions?prefix=how`
- Search submit: `http://localhost:8080/api/search`

Example search submission:

```bash
curl -X POST http://localhost:8080/api/search \
  -H 'Content-Type: application/json' \
  -d '{"query":"how to tie a tie"}'
```

## Notes

- Redis is intentionally not published on a host port.
- The backend and frontend are published only to `127.0.0.1`.
- If the backend or frontend are stuck in `Created`, the dataset loader may
  still be loading the ORCAS data.

## Diagnostics

Check running containers with:

```powershell
docker compose ps
```

View logs with:

```powershell
docker compose logs --tail 100
```

## Learning goals

This project is designed to show:

- how a cache-aside autocomplete system works
- how to use consistent hashing for Redis shard assignment
- how to separate fast cache reads from durable database writes
- how to seed a search system with real popularity data
- how to manage a local Docker-based full-stack environment
