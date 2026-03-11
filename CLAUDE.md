# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Juniper is a research study management platform supporting collection of survey, genomic, and medical records data. It has two portals: an admin (study manager) interface and a participant-facing portal.

## Commands

### Frontend (npm workspaces)

```bash
# Install dependencies (required on macOS: brew install pkg-config cairo pango libpng jpeg giflib librsvg pixman)
npm install

# Build the shared ui-core library first (required before running admin/participant UIs)
npm -w ui-core run build

# Run admin UI (localhost:3000)
npm run admin-dev-local           # with unauthed login (no B2C)
npm run admin-dev                 # with HTTPS

# Run participant UI (localhost:3001, accessed as sandbox.demo.localhost:3001)
VITE_UNAUTHED_LOGIN=true HTTPS=true npm -w ui-participant start

# Run all frontend tests
npm test

# Run tests for a single workspace
npm -w ui-admin test
npm -w ui-core test
npm -w ui-participant test

# Lint (zero warnings policy)
npm run lint
```

### Backend (Gradle)

```bash
# Start PostgreSQL (required for tests and running the app)
./scripts/postgres/run_postgres.sh start

# Build all Java modules
./gradlew build

# Run all backend tests
./gradlew test

# Run tests for a single module
./gradlew :core:test
./gradlew :api-admin:test

# Run a specific test class
./gradlew :core:test --tests "bio.terra.pearl.core.dao.SomeTest"

# Populate seed data (after starting ApiAdminApp)
./scripts/populate/populate_portal.sh demo

# Render environment variables for IntelliJ run configs
bash ./scripts/render_environment_vars.sh ApiAdminApp <YOUR_EMAIL>
bash ./scripts/render_environment_vars.sh ApiParticipantApp <YOUR_EMAIL>
```

### Spring Boot profiles
Run both API apps with profiles: `human-readable-logging, development`

## Architecture

### Module Structure

```
core/          - POJOs, DAOs, services, Liquibase migrations (the data layer)
populate/      - Data seeding/population from files and CLI
api-admin/     - Spring Boot REST API for admin UI (port 8080)
api-participant/ - Spring Boot REST API for participant UI (port 8081)
compliance/    - Compliance utilities
pepper-import/ - Data import from DSM/Pepper
ui-core/       - Shared React component library (@juniper/ui-core)
ui-admin/      - Admin SPA (React + Vite)
ui-participant/ - Participant SPA (React + Vite)
e2e-tests/     - Playwright end-to-end tests
```

### Backend Patterns

**Data layer** (`core` module):
- Models extend `BaseEntity` (`core/src/main/java/bio/terra/pearl/core/model/`)
- DAOs extend `BaseJdbiDao` (provides create/find/delete), `BaseMutableJdbiDao`, or `BaseVersionedJdbiDao`
- Services live in `core/src/main/java/bio/terra/pearl/core/service/`
- DB schema managed by Liquibase changesets in `core/src/main/resources/db/changelog/changesets/`

**API layer**:
- Controllers are generated from OpenAPI specs: `api-admin/src/main/resources/api/openapi.yml` and `api-participant/src/main/resources/api/openapi.yml`
- Add a new endpoint by editing the openapi.yml, then implementing the generated interface in `api-admin/src/main/java/bio/terra/pearl/api/admin/controller/`

### Frontend Patterns

- `ui-core` must be built before `ui-admin` or `ui-participant` can run (it's a local package dependency)
- Both UIs use React 18 + React Router 6 + Bootstrap 5 + SurveyJS 1.12.4
- Auth via `react-oidc-context` + Azure B2C; bypass with `VITE_UNAUTHED_LOGIN=true`
- Admin UI proxies API to `http://localhost:8080`

### Adding a New Model (standard workflow)

1. Create POJO extending `BaseEntity` in `core/.../model/`
2. Add Liquibase changeset in `core/.../resources/db/changelog/changesets/`
3. Create DAO extending `BaseJdbiDao` in `core/.../dao/`
4. Add service in `core/.../service/`
5. Add seed data in `populate/src/main/resources/seed/` and a populator in `populate/.../populate/service/`
6. Edit `api-admin/src/main/resources/api/openapi.yml` to add endpoint
7. Implement the generated controller interface in `api-admin/.../controller/`

### Key Tech

- **Backend**: Java 21, Spring Boot 3.x, JDBI3, PostgreSQL, Liquibase, Lombok
- **Frontend**: TypeScript 4.8, React 18, Vite, SurveyJS 1.12.4, TanStack Table
- **Auth**: Azure B2C / Auth0 JWT (OAuth2)
- **Infra**: GCP, Docker (JIB), Terraform, Helm
