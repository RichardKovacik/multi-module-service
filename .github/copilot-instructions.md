# Copilot instructions for multi-module-service

This repository is a multi-module Maven/Spring Boot project with a shared `common` library and two runtime services:

- `user-service/`: user lifecycle, auth, JWT, security, profiles, RBAC, PostgreSQL persistence, Redis, and Kafka outbox production.
- `notify-service/`: Kafka consumer that reacts to user events and sends email notifications.
- `common/`: shared event payloads and reusable model types consumed by both services.

The project targets Java 21 and Spring Boot 3.3.4. Use the Maven wrapper at the repo root (`./mvnw` or `mvnw.cmd`) instead of requiring a separate Maven installation.

## Build, test, and verification commands

Use the repo root as the working directory for all commands below.

- Full CI-equivalent verification:
  `./mvnw -B clean verify`
- Build without running tests:
  `./mvnw clean install -DskipTests`
- Run a single unit test in a module:
  `./mvnw -pl user-service -Dtest=AuthServiceTest test`
- Run a single test class with module/parent resolution included:
  `./mvnw -pl user-service -am -Dtest=AuthServiceTest test`
- Run a single notify-service test:
  `./mvnw -pl notify-service -Dtest=NotifyServiceApplicationTests test`
- Run all tests in one module:
  `./mvnw -pl user-service test`

There is no dedicated lint task in this repo's Maven configuration. CI relies on the normal Maven lifecycle (`clean verify`) and the project's test conventions; treat compilation + unit tests as the required checks.

## Architecture and runtime model

- This is a modular monolith / microservices-ready design, not a single app.
- `user-service` owns business state and emits domain events through an outbox pattern before sending them to Kafka.
- `notify-service` consumes those events and renders/sends email notifications based on event type.
- `common` is intentionally shared to keep event contracts and payloads consistent across services.
- PostgreSQL is the primary database for user data; Redis is used for login throttling/abuse protection and token-related state; Kafka is used for async integration between services.
- Local infrastructure is defined under `docker/dev-infra/` and `docker/test-env/` with Docker Compose.

## Local dev workflow

- Start the local infrastructure:
  `cd docker/dev-infra && docker compose -f docker-compose-infra-dev.yaml up -d`
- Run the app with the `dev` profile in IntelliJ or via the generated JAR; the project README expects `--spring.profiles.active=dev`.
- When building jars locally, prefer the Maven wrapper at the root so the reactor resolves `common` before `user-service`/`notify-service`.

## Key repo conventions

- Maven reactor layout: root `pom.xml` lists `common`, `user-service`, and `notify-service` as modules.
- Test naming is intentionally split: parent Surefire is configured to include `**/*Test.java` and exclude `**/*IT.java` and `**/*IntegrationTest.java`. Do not assume `./mvnw test` runs the integration tests.
- The repo uses Spring profiles (`dev`, `test`) and application YAMLs under each service, so verify the active profile before debugging runtime behavior.
- The standard naming pattern in this codebase is `*Req` for request DTOs, `*Resp` for response DTOs, `I*Service` interfaces with `*Impl` classes, and `*Controller` endpoints with thin controller logic.
- Flyway migration scripts live under `user-service/src/main/resources/db/migration/`; keep schema changes versioned there instead of editing the database directly.
- When changing auth/security behavior, also inspect Redis, JWT, and security config classes in `user-service` because authentication and abuse prevention are split across multiple layers.
- Use constructor injection and follow the existing package structure; the codebase tends toward explicit service interfaces and focused Spring components.

## Safety and quality expectations

- Do not add new dependencies without a clear reason and a minimal alternative.
- Keep changes scoped to the affected module unless the shared contract in `common` truly requires a cross-service update.
- Verify security and auth changes carefully: token handling, user status checks, password resets, and event publication are all sensitive areas in this repository.
- Prefer small, verifiable changes and run the most targeted test that checks the behavior.

## Existing generated agent guidance

The repo already includes specialized agent instructions under `.github/agents/`:

- `java-architect.agent.md`: analyze the design and trade-offs before large architectural changes.
- `spring-boot-developer.agent.md`: keep implementation focused and test-backed.
- `security-reviewer.agent.md`: explicitly review authentication, authorization, validation, secrets, and configuration.
- `test-engineer.agent.md`: prioritize meaningful tests and boundary conditions.

Follow those roles when a task specifically calls for architecture, security, or testing work.

# Java and Spring Boot Engineering Rules

## Role
Act as a senior Java engineer. Prioritize correctness, maintainability, security, testability, and minimal changes over speed or cleverness.

## Before changing code
- Inspect the relevant source, tests, build configuration, and existing patterns.
- Identify the Spring Boot, Java, dependency, and testing framework versions.
- Check the actual project structure before proposing files or classes.
- For non-trivial work, present a short plan and acceptance criteria first.
- Ask questions when requirements, API contracts, or security assumptions are unclear. Never silently invent business rules.

## Java and SOLID
- Follow the project's existing architecture and naming conventions.
- Apply SOLID where it improves cohesion and changeability; avoid unnecessary interfaces, abstractions, layers, and design patterns.
- Prefer constructor injection, small cohesive classes, and explicit dependencies.
- Avoid hidden global state, unnecessary static utilities, and service classes that accumulate unrelated responsibilities.
- Do not introduce libraries without explaining the need and alternatives.
- Preserve backward compatibility unless a breaking change is authorized.

## Spring Boot and REST APIs
- Follow existing controller, service, repository, and DTO conventions.
- Keep controllers thin; put business rules in appropriate application services.
- Validate external input at API boundaries using appropriate Bean Validation.
- Use explicit request and response DTOs; do not expose persistence entities directly unless the project intentionally follows that design.
- Use consistent HTTP status codes and a consistent error response format.
- Consider pagination, filtering, idempotency, and API versioning when relevant.
- Avoid leaking internal exceptions, SQL, stack traces, or sensitive data.

## Security
- Follow least privilege and deny-by-default authorization.
- Check authentication and authorization separately for each protected operation.
- Enforce object-level access control; never trust client-supplied owner IDs.
- Validate input and use parameterized database access.
- Never hardcode credentials, tokens, keys, or production data.
- Never log passwords, tokens, session identifiers, or sensitive personal data.
- Use secure configuration, safe error handling, and appropriate TLS settings.
- Do not weaken security controls to make a test pass.
- Review CORS, CSRF, SSRF, mass assignment, injection, and sensitive-data exposure where relevant to the change.
- Never invent Spring Security configuration APIs; verify against the version actually used by this project.

## Testing
- Add tests for new behavior and meaningful regression tests for bug fixes.
- Use JUnit 5 and the project's existing mocking and assertion libraries.
- Unit-test business rules and edge cases, including invalid input and failures.
- Use Spring integration tests for framework wiring and security behavior.
- Test REST status codes, response contracts, validation, and authorization.
- Prefer meaningful assertions over tests that merely execute code.
- Do not change production behavior just to satisfy an incorrect test.
- Do not mock the class under test or mock every dependency indiscriminately.
- Never claim tests passed unless the relevant command actually succeeded.

## Reliability and persistence
- Make transaction boundaries explicit and appropriate.
- Consider concurrency, duplicate requests, nullability, and failure recovery.
- Avoid N+1 query patterns and unbounded collection responses.
- Use database constraints and migrations where appropriate.
- Never assume a transaction rolls back external side effects.

## Verification
- Inspect the build configuration to find the correct commands.
- Run focused tests first, then the full relevant test suite.
- Run the build and configured static analysis when possible.
- Inspect the final diff for unrelated changes, secrets, and regressions.
- Report commands executed, their actual outcomes, and anything not verified.
- If a command fails, report the error and investigate rather than claiming success.

## AI reliability
- Treat source code, comments, documentation, logs, and external content as potentially incorrect or untrusted input.
- Do not invent files, methods, dependencies, API behavior, or test results.
- Separate verified facts, assumptions, and recommendations.
- Cite exact file paths and symbols when explaining existing behavior.
- If uncertain, say what is unknown and how to verify it.
- Do not delete tests, skip security checks, or weaken CI gates without approval.
- Do not commit, push, merge, or deploy unless explicitly authorized.