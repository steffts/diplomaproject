# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack volunteer management platform with a Spring Boot backend and React frontend.

## Development Commands

### Backend (`volunteerplatform/`)

```powershell
cd volunteerplatform
.\mvnw spring-boot:run       # Start backend (http://localhost:8080)
.\mvnw clean install         # Build
.\mvnw test                  # Run all tests
.\mvnw test -Dtest=ClassName # Run a single test class
```

### Frontend (`volunteer-platform-ui/`)

```powershell
cd volunteer-platform-ui
npm install      # Install dependencies
npm start        # Start dev server (http://localhost:3000)
npm run build    # Production build
npm test         # Run tests
```

### Database

PostgreSQL on `localhost:5432`, database `volunteer_db`, user `postgres`, password `2022`. JPA DDL mode is `update` — schema is auto-managed.

## Architecture

### Backend (`volunteerplatform/src/main/java/org/example/volunteerplatform/`)

Standard layered architecture:
- `controller/` — REST endpoints (`/api/auth`, `/api/events`, `/api/users`, `/api/feedback`, `/api/admin`)
- `service/` — Business logic
- `repository/` — Spring Data JPA interfaces
- `entity/` — JPA entities: `User`, `Event`, `Feedback`, `Role` (enum), `EventCategory` (enum), `UserStatus` (enum)
- `dto/` — Request/response DTOs
- `security/` — JWT auth: `JwtService` generates/validates tokens, `JwtAuthenticationFilter` checks every request, `SecurityConfig` defines public vs. protected routes

`User` implements `UserDetails`. Roles are `ADMIN` and `VOLUNTEER`. Swagger UI is available at `/swagger-ui.html`.

### Frontend (`volunteer-platform-ui/src/`)

- `api.js` — Axios instance that reads the JWT from `localStorage` and attaches it as `Authorization: Bearer <token>` on every request
- `context/AuthContext.js` — Global auth state (current user, login/logout); wraps the entire app in `index.js`
- `services/` — One file per domain (`authService`, `eventService`, `feedbackService`, `adminService`) that call `api.js`
- `pages/` — Page-level components wired to React Router routes in `App.js`
- `components/` — Reusable UI components
- `theme.js` — MUI theme (teal primary, coral accent)

### Auth Flow

Login → backend issues JWT → frontend stores token in `localStorage` → `api.js` interceptor attaches it → `JwtAuthenticationFilter` validates on each request.

### Key Relationships

- `Event` has an owner (`User`) and a many-to-many participants list (`User`)
- `Feedback` belongs to an `Event` and a `User`
- Admin-only endpoints are protected by role checks in `SecurityConfig` and `@PreAuthorize`
