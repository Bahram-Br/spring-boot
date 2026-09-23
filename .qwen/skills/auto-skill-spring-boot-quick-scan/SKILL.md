---
name: spring-boot-quick-scan
description: Read-only improvement scan of a Spring Boot CRUD project. Use when asked to "quick scan" or "list what to improve" without modifying anything — produces a severity-ordered checklist of bugs, security, design, testing, and hygiene findings.
source: auto-skill
extracted_at: '2026-09-23T00:38:09.858Z'
---

# Quick read-only improvement scan of a Spring Boot project

## When to use
User asks to scan a Spring Boot project and list improvements, explicitly "do not modify anything". Deliver a list only — no edits.

## Procedure
1. **Map the structure**: `pom.xml`, `src/main/resources/application.properties`, then glob all `src/**/*.java` and `src/test/**/*.java`.
2. **Read every file**: controller(s), service, repository, entity, DTOs, exception handler(s), main app class, tests, and config. Even a bare `contextLoads()` test is a finding (no real coverage).
3. **Scan against the checklist below and report grouped by severity**: Critical/bugs → Security → API design → Code quality → Testing → Repo hygiene.

## Bug patterns to hunt (high-value, easy to miss)
- **DTO validation no-op**: `@Valid @RequestBody DTO` where the DTO has zero constraint annotations → validation silently does nothing (constraints on the entity are never triggered).
- **Update endpoint takes the entity, not a DTO**: invalid payloads pass through; path `id` is ignored (`save(student)` with a body whose id is null = INSERT, not UPDATE); no existence check → silent create.
- **`findByName`/`findByEmail` returning a single entity** → `IncorrectResultSizeDataAccessException` (500) if data duplicates. Should be `Optional` or `List`.
- **Inconsistent missing-resource handling**: some endpoints 404, others return `null` with 200 OK / empty body.
- **Endpoint name vs. semantics mismatch** (e.g. `/students/age/{age}` calling a "greater-than" query).
- **Exception handler that won't compile**: missing imports (`RestControllerAdvice`, `ExceptionHandler`, `ResponseEntity`, `HttpStatus`) and/or a missing closing brace. Flag as Critical, verify by reading.

## Security checklist
- Hardcoded DB credentials in `application.properties` → must move to env vars; suggest rotating.
- `ddl-auto=update` → prefer `validate` + Flyway/Liquibase migrations.
- `show-sql` / `format_sql` left on in shared config → profile-scoped.

## API design / code quality
- Controller injecting a repository it never uses (dead dependency).
- Generic class names (`Controller`) and inconsistent method casing (`addstudent` vs `addStudent`).
- No base path / versioning (`/api/v1/...`).
- Lombok declared in pom but hand-written accessors → pick one.
- Magic numbers / business rules buried in service methods (e.g. `if (age < 15) return null`).
- POM boilerplate placeholders (`<url/>`, `<licenses/>`, `<developers/>`, `<scm/>` empty).
- Entity auto-generating fields (e.g. email from name) with no unique constraint → silent duplicates; surprise side effects inside setters.

## Testing / repo hygiene
- Only default `contextLoads()` test despite test starters/deps present → no coverage.
- No H2/Testcontainers → tests require a live database.
- No README; IDE dirs (`.idea/`, `.vscode/`) untracked but not gitignored.

## Delivery format
Grouped bullet list, most severe first, each item naming the file(s) involved. End by offering to fix the build-breaking items first. Stay read-only unless the user asks for changes.
