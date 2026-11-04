# AI Test Generator for REST APIs

Spring Boot application that takes an OpenAPI spec (and later a small Spring repo), uses an LLM to generate JUnit 5 + REST Assured tests, runs them in an isolated container, and reports results and coverage. Failed tests can be sent back to the model to fix the test or flag a likely real API bug. A simple dashboard will show runs and live progress.

This repository is built **step by step** as a learning portfolio: each phase is small, tested, and understood before the next one starts.

## Current status

| Area | Status |
| --- | --- |
| Spring Boot 4 app skeleton + health endpoint | Done |
| Project domain + Postgres persistence (JPA, Flyway, Testcontainers) | Done |
| OpenAPI upload / parsing | Planned |
| Spring AI test generation | Planned |
| Isolated test execution + JaCoCo | Planned |
| LLM feedback loop | Planned |
| Async jobs + SSE dashboard | Planned |
| Observability / security / deployment | Planned |

**What works today**

- `GET /api/health` → `{"status":"UP"}`
- `Project` persisted to Postgres (`projects` table)
- Flyway migration `V1__create_projects.sql`
- Unit tests + `@WebMvcTest` + `@DataJpaTest` against real Postgres via Testcontainers

**What does not exist yet**

- HTTP APIs to create/list projects
- Spec upload, LLM calls, test runner, dashboard

## Architecture (target)

```text
Client / dashboard
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│  This Spring Boot app (ai-test-generator)                 │
│                                                           │
│  REST API  → projects, specs, test runs                   │
│  OpenAPI parser                                           │
│  Spring AI ChatClient → JUnit + REST Assured source       │
│  Job runner → container executes tests vs sample API      │
│  Feedback loop → fix flaky generated tests / flag bugs    │
│  SSE progress + simple dashboard                          │
└───────────────┬─────────────────────────────┬─────────────┘
                │                             │
                ▼                             ▼
         PostgreSQL                    Docker (test run
      projects / specs /               + JaCoCo coverage)
      runs / results
```

The LLM and the test container are workers. **Postgres is the system of record.**

## Stack

| Layer | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot **4.1.2** (Spring Framework 7) |
| Web | `spring-boot-starter-webmvc` |
| Persistence | Spring Data JPA + Hibernate, PostgreSQL |
| Migrations | Flyway (`spring-boot-starter-flyway`) |
| Tests | JUnit + MockMvc + `@DataJpaTest` + Testcontainers 2.x + `@ServiceConnection` |
| Build | Maven (wrapper), jar packaging |

**Planned additions**

- Spring AI 2.0.x (Anthropic Claude; provider swappable via config)
- JaCoCo for generated-test coverage
- Actuator + Micrometer
- SSE for live run progress

### Boot 4 notes (intentional differences from Boot 3)

- Web starter is `spring-boot-starter-webmvc` (not deprecated `spring-boot-starter-web`)
- Each feature has a matching `*-test` starter (e.g. `spring-boot-starter-data-jpa-test`)
- `@WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure`
- `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure`
- Jackson 3 (`tools.jackson`) is preferred; JSON records work without custom config
- Flyway needs `spring-boot-starter-flyway` (not only `flyway-core`) plus `flyway-database-postgresql`
- Testcontainers 2: `org.testcontainers.postgresql.PostgreSQLContainer`

## Project layout

```text
com.example.aitestgen/
  AiTestGeneratorApplication.java
  health/          # liveness HTTP API
  project/         # Project entity + Spring Data repository
  testrun/         # TestRunStatus enum (domain vocabulary so far)
  support/         # (tests) shared Testcontainers config
```

Packages are created **when the first real class lands**, not as empty folders.

## Domain model (so far)

```text
Project  1──*  ApiSpec  1──*  TestRun
   ▲                ▲            │
 done            planned      planned
                                 └─ TestRunStatus: PENDING → GENERATING → RUNNING → COMPLETED | FAILED
```

| Type | Role |
| --- | --- |
| `Project` | Named API under test; JPA entity mapped to `projects` |
| `ApiSpec` | Uploaded OpenAPI document for a project |
| `TestRun` | One generate + execute attempt |
| `TestRunStatus` | Lifecycle of a run |

## Persistence design

- **Flyway** owns schema changes (`src/main/resources/db/migration/`)
- **Hibernate** uses `spring.jpa.hibernate.ddl-auto=validate` (check mapping; do not create tables)
- **Local run:** Docker Compose Postgres on `localhost:5432`
- **Tests:** Testcontainers Postgres + `@ServiceConnection` (no hardcoded test ports)

## Prerequisites

- Java 21+
- Docker Desktop (or another Docker daemon) running — required for tests and for local Postgres

## Run locally

```bash
# start Postgres
docker compose up -d

# start the app
./mvnw spring-boot:run
```

Check health:

```bash
curl -i http://localhost:8080/api/health
```

Stop Postgres:

```bash
docker compose down
```

Datasource defaults (see `application.properties`):

| Property | Value |
| --- | --- |
| URL | `jdbc:postgresql://localhost:5432/aitestgen` |
| User / password | `aitestgen` / `aitestgen` |

You do **not** need a cloud Postgres account for development.

## Test

```bash
./mvnw test
```

| Test | What it proves |
| --- | --- |
| `HealthControllerTest` | MVC slice: `GET /api/health` JSON |
| `ProjectTest` | Pure unit: domain rules, no Spring |
| `ProjectRepositoryTest` | JPA slice + real Postgres: save/find |
| `AiTestGeneratorApplicationTests` | Full context starts with Testcontainers DB |

## Roadmap / future implementation plan

Phases are sequential. Within a phase, work stays commit-sized.

### Phase 1 — Project skeleton ✅

- [x] Spring Boot 4 Initializr baseline
- [x] Rename to `ai-test-generator` / `com.example.aitestgen`
- [x] `GET /api/health` + `@WebMvcTest`
- [x] First unit / slice tests

### Phase 2 — Domain + persistence ✅ (core) / remaining entities

- [x] `Project` + `TestRunStatus`
- [x] JPA + PostgreSQL + Flyway `V1`
- [x] Testcontainers `@ServiceConnection`
- [ ] `ApiSpec` entity + migration
- [ ] `TestRun` entity + migration
- [ ] REST API to create/list projects (thin CRUD before OpenAPI)

### Phase 3 — OpenAPI spec upload + parsing

- [ ] Upload endpoint (multipart) bound to a project
- [ ] Parse OpenAPI 3.x into an internal model (operations, paths, schemas)
- [ ] Persist raw spec + parsed summary
- [ ] Validation errors returned as problem details
- [ ] Tests with fixture YAML/JSON specs

### Phase 4 — Spring AI integration

- [ ] Add Spring AI 2.0.x + Anthropic starter
- [ ] `ChatClient` bean; provider swappable via config
- [ ] Prompt templates for test generation
- [ ] Structured output into Java records
- [ ] Unit tests with a fake/stub chat model (no network in CI by default)

### Phase 5 — Test generation

- [ ] Generate JUnit 5 + REST Assured source from parsed spec + LLM output
- [ ] Store generated sources against a `TestRun`
- [ ] Guardrails: package names, timeouts, no arbitrary shell
- [ ] Snapshot/golden tests for prompt → structure mapping

### Phase 6 — Isolated test execution

- [ ] Run generated tests in a disposable container against a sample target API
- [ ] Collect pass/fail, logs, timings
- [ ] JaCoCo coverage for the **target** API under test
- [ ] Persist results on `TestRun`

### Phase 7 — Feedback loop

- [ ] On failure, send reports back to the LLM (tool calling / structured fix request)
- [ ] Distinguish “bad generated test” vs “likely real API bug”
- [ ] Cap retries; record decisions for the dashboard

### Phase 8 — Async jobs + resilience

- [ ] Async job execution (virtual threads where appropriate)
- [ ] Retries / rate limiting for LLM calls
- [ ] Idempotent run creation; status transitions on `TestRunStatus`

### Phase 9 — Live progress + dashboard

- [ ] SSE (or similar) stream of run events
- [ ] Simple dashboard: projects, runs, live status, failure summaries
- [ ] Keep first UI intentionally small (one job per view)

### Phase 10 — Observability, security, deployment

- [ ] Actuator + Micrometer (latency, run counts, token usage if available)
- [ ] Basic security for upload/run APIs
- [ ] Deployment notes (container image, env-based secrets)
- [ ] Architecture diagram refresh in this README

## Suggested next commit-sized steps

1. Add `ApiSpec` + `TestRun` tables (Flyway `V2` / `V3`) and repositories  
2. `POST /api/projects` + `GET /api/projects` with slice/integration tests  
3. Spec upload endpoint + parser spike with one fixture file  

## Learning goals

This project prioritizes understanding over speed:

- Boot 4 modular starters and test slices
- Domain model before framework annotations where it helps
- Flyway-owned schema + Hibernate validate
- Real Postgres in tests via Testcontainers, not H2-only shortcuts
- Later: Spring AI structured output, isolated execution, and honest failure handling

## License

No license chosen yet — add one before making the repository public if needed.
