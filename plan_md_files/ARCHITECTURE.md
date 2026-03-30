# Doto — Architecture Document
**Version:** 1.0  
**Stack:** Scala Play Framework (REST API) + SwiftUI (iOS)  
**Scope:** MVP — Auth, Family, Schedule, Tasks, Shopping, Rewards

---

## 1. System Overview

Doto is a family coordination app. The system consists of two independent codebases:

1. **`doto-api`** — Scala Play Framework REST API. Owns all business logic, database access, and authentication. Exposes JSON over HTTP.
2. **`doto-ios`** — SwiftUI iOS application. Consumes the REST API. Stores the JWT in Keychain. No business logic lives on the client.

```
┌─────────────────────────────────────────────────────┐
│                   iOS App (SwiftUI)                  │
│  TabBar → Dashboard / Schedule / Tasks /             │
│           Shopping / Rewards                         │
└───────────────────────┬─────────────────────────────┘
                        │ HTTPS JSON REST
                        │ Authorization: Bearer <jwt>
┌───────────────────────▼─────────────────────────────┐
│              Play Framework REST API                  │
│  AuthController / FamilyController /                  │
│  EventController / TaskController /                   │
│  ShoppingController / RewardController               │
└───────────────────────┬─────────────────────────────┘
                        │ JDBC / Slick
┌───────────────────────▼─────────────────────────────┐
│                   PostgreSQL Database                 │
│  families / profiles / events / tasks /              │
│  shopping_lists / shopping_items / rewards           │
└─────────────────────────────────────────────────────┘
```

---

## 2. Repository Structure

Two separate repositories (or two top-level folders in a monorepo):

```
doto/
├── doto-api/          ← Scala Play project
└── doto-ios/          ← Xcode project
```

---

## 3. Backend — `doto-api`

### 3.1 Tech Stack

| Layer | Technology |
|---|---|
| Language | Scala 2.13 |
| Framework | Play Framework 2.9 |
| Database ORM | Slick 3.4 via play-slick |
| DB Migrations | Play Evolutions |
| Database | PostgreSQL 15 |
| Auth | JWT via `java-jwt` (Auth0) |
| Password Hashing | bcrypt via `jBCrypt` |
| JSON | Play JSON (built-in) |
| HTTP Client | Play WS (built-in) |
| Dependency Injection | Guice (built-in) |

### 3.2 Folder Structure

```
doto-api/
├── app/
│   ├── controllers/
│   │   ├── AuthController.scala
│   │   ├── FamilyController.scala
│   │   ├── MemberController.scala
│   │   ├── EventController.scala
│   │   ├── TaskController.scala
│   │   ├── ShoppingController.scala
│   │   └── RewardController.scala
│   ├── models/
│   │   ├── domain/
│   │   │   ├── Family.scala
│   │   │   ├── Profile.scala
│   │   │   ├── Event.scala
│   │   │   ├── Task.scala
│   │   │   ├── ShoppingList.scala
│   │   │   ├── ShoppingItem.scala
│   │   │   └── Reward.scala
│   │   └── requests/
│   │       ├── AuthRequests.scala
│   │       ├── EventRequests.scala
│   │       ├── TaskRequests.scala
│   │       ├── ShoppingRequests.scala
│   │       └── RewardRequests.scala
│   ├── repositories/
│   │   ├── ProfileRepository.scala
│   │   ├── FamilyRepository.scala
│   │   ├── EventRepository.scala
│   │   ├── TaskRepository.scala
│   │   ├── ShoppingRepository.scala
│   │   └── RewardRepository.scala
│   ├── services/
│   │   ├── AuthService.scala
│   │   └── InviteService.scala
│   ├── actions/
│   │   └── AuthenticatedAction.scala
│   └── utils/
│       ├── JwtUtils.scala
│       └── PasswordUtils.scala
├── conf/
│   ├── application.conf
│   ├── routes
│   └── evolutions/
│       └── default/
│           ├── 1.sql   ← families + profiles
│           ├── 2.sql   ← events
│           ├── 3.sql   ← tasks
│           ├── 4.sql   ← shopping_lists + shopping_items
│           └── 5.sql   ← rewards
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── test/
│   └── controllers/
└── build.sbt
```

### 3.3 build.sbt

```scala
name := "doto-api"
organization := "com.doto"
version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  "org.playframework"      %% "play-slick"             % "6.1.0",
  "org.playframework"      %% "play-slick-evolutions"  % "6.1.0",
  "org.postgresql"          %  "postgresql"             % "42.7.1",
  "com.auth0"               %  "java-jwt"               % "4.4.0",
  "org.mindrot"             %  "jbcrypt"                % "0.4",
  "com.typesafe.play"      %% "play-json"              % "2.10.5",
  "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.0"  % Test
)

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
```

### 3.4 application.conf

```hocon
# Database
slick.dbs.default.profile = "slick.jdbc.PostgresProfile$"
slick.dbs.default.db.driver = "org.postgresql.Driver"
slick.dbs.default.db.url = ${?DATABASE_URL}
slick.dbs.default.db.user = ${?DATABASE_USER}
slick.dbs.default.db.password = ${?DATABASE_PASSWORD}
slick.dbs.default.db.connectionPool = "HikariCP"
slick.dbs.default.db.numThreads = 10

# Evolutions
play.evolutions.db.default.autoApply = true

# JWT
jwt.secret = ${?JWT_SECRET}
jwt.expiry.days = 30

# CORS
play.filters.cors.allowedOrigins = ["*"]
play.filters.cors.allowedHttpMethods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
play.filters.cors.allowedHttpHeaders = ["Accept", "Content-Type", "Authorization"]

# Filters
play.filters.enabled += "play.filters.cors.CORSFilter"
play.filters.disabled += "play.filters.csrf.CSRFFilter"

# HTTP
play.http.secret.key = ${?APPLICATION_SECRET}
```

### 3.5 Environment Variables

```bash
# .env (never commit this file)
DATABASE_URL=jdbc:postgresql://localhost:5432/doto
DATABASE_USER=postgres
DATABASE_PASSWORD=yourpassword
JWT_SECRET=a-long-random-secret-minimum-32-chars
APPLICATION_SECRET=another-long-random-secret
```

### 3.6 Authentication Strategy

- Passwords hashed with **bcrypt** (cost factor 12) using `jBCrypt`
- On login, server issues a signed **JWT** containing `userId` (UUID) and `familyId` (UUID, nullable until family is joined)
- JWT expiry: **30 days**
- iOS stores the JWT in **Keychain** (never UserDefaults)
- Every protected endpoint requires `Authorization: Bearer <token>` header
- The `AuthenticatedAction` wrapper validates the JWT and injects `userId` and `familyId` into the request context

### 3.7 Family Invite Code Strategy

- When a parent creates a family, a **6-character alphanumeric code** is generated and stored on the `families` table (e.g. `DOTO4X`)
- The code is permanent for MVP (no expiry, no rotation)
- A second user registers normally, then calls `POST /api/families/join` with the code
- They are added to the family as a `parent` role
- Child profiles are **created by parents inside the app** — children are not independent auth accounts, they are profile records belonging to the family

---

## 4. Frontend — `doto-ios`

### 4.1 Tech Stack

| Layer | Technology |
|---|---|
| Language | Swift 5.9 |
| UI Framework | SwiftUI |
| Min iOS Target | iOS 16.0 |
| Architecture | MVVM |
| Networking | URLSession + async/await |
| Auth Storage | Keychain via Security framework |
| State Management | @StateObject, @EnvironmentObject, @Published |

### 4.2 Folder Structure

```
doto-ios/
├── DotoApp.swift                  ← App entry point, injects AuthViewModel as environment
├── Config/
│   └── APIConfig.swift            ← Base URL constant, switches DEBUG vs RELEASE
├── Networking/
│   ├── APIClient.swift            ← URLSession wrapper, injects auth header automatically
│   └── APIError.swift             ← Typed error enum
├── Auth/
│   ├── KeychainHelper.swift       ← save / load / delete JWT token
│   ├── AuthViewModel.swift        ← login, register, logout, session state
│   └── Views/
│       ├── LoginView.swift
│       └── RegisterView.swift
├── Models/                        ← Codable structs, 1:1 with API response shapes
│   ├── Family.swift
│   ├── Profile.swift
│   ├── DotoEvent.swift            ← Named DotoEvent to avoid collision with SwiftUI Event
│   ├── DotoTask.swift             ← Named DotoTask to avoid collision with Swift concurrency Task
│   ├── ShoppingList.swift
│   ├── ShoppingItem.swift
│   └── Reward.swift
├── Dashboard/
│   ├── DashboardViewModel.swift
│   └── DashboardView.swift
├── Schedule/
│   ├── ScheduleViewModel.swift
│   ├── ScheduleView.swift
│   └── AddEditEventView.swift
├── Tasks/
│   ├── TasksViewModel.swift
│   ├── TasksView.swift
│   └── AddEditTaskView.swift
├── Shopping/
│   ├── ShoppingViewModel.swift
│   ├── ShoppingListsView.swift
│   ├── ShoppingItemsView.swift
│   └── AddItemView.swift
├── Rewards/
│   ├── RewardsViewModel.swift
│   └── RewardsView.swift
├── Family/
│   ├── FamilyViewModel.swift
│   ├── FamilySetupView.swift      ← Create family or enter invite code
│   └── FamilyManageView.swift
└── Shared/
    ├── Components/
    │   ├── AvatarView.swift
    │   ├── LoadingView.swift
    │   └── EmptyStateView.swift
    └── Extensions/
        ├── Color+Doto.swift       ← Brand colours as Color constants
        └── Date+Formatting.swift  ← Shared date display helpers
```

### 4.3 App Navigation States

The root `DotoApp.swift` switches between three top-level states based on `AuthViewModel`:

```
State 1: No JWT in Keychain
  → Show LoginView / RegisterView

State 2: JWT valid, profile.familyId == nil
  → Show FamilySetupView (create a family or join with code)

State 3: JWT valid, profile.familyId set
  → Show MainTabView with 5 tabs:
    [Dashboard] [Schedule] [Tasks] [Shopping] [Rewards]
```

### 4.4 APIConfig.swift

```swift
enum APIConfig {
    #if DEBUG
    static let baseURL = "http://localhost:9000/api"
    #else
    static let baseURL = "https://api.getdoto.com/api"
    #endif
}
```

---

## 5. Local Development Setup

### Backend
```bash
# Prerequisites: Java 17+, sbt, PostgreSQL 15
brew install sbt postgresql@15

# Create database
psql -U postgres -c "CREATE DATABASE doto;"

# Export environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/doto
export DATABASE_USER=postgres
export DATABASE_PASSWORD=yourpassword
export JWT_SECRET=dev-secret-minimum-32-characters-here
export APPLICATION_SECRET=dev-app-secret-also-long

# Run — Play Evolutions auto-creates schema on first boot
cd doto-api && sbt run
# API available at http://localhost:9000
```

### iOS
```bash
# Prerequisites: Xcode 15+
cd doto-ios
open Doto.xcodeproj
# Select any iPhone 16 simulator (iOS 16+)
# Cmd+R to build and run
# App will point to http://localhost:9000 in DEBUG mode
```

---

## 6. API Conventions

| Convention | Rule |
|---|---|
| Base path | `/api` |
| Content-Type | `application/json` on all requests and responses |
| Auth header | `Authorization: Bearer <jwt>` on all protected routes |
| Timestamps | ISO 8601 string format: `"2026-03-27T10:30:00Z"` |
| IDs | UUIDs as strings: `"a1b2c3d4-..."` |
| Unauthenticated | 401 with `{"code":"unauthorized","message":"..."}` |
| Not found | 404 with `{"code":"not_found","message":"..."}` |
| Validation error | 400 with `{"code":"validation_error","message":"..."}` |
| Forbidden | 403 with `{"code":"forbidden","message":"..."}` |

---

## 7. Security Rules

1. Never log JWT tokens, passwords, or password hashes
2. All database queries go through Slick parameterised queries — no raw SQL string interpolation
3. Every repository method filters by `familyId` from the JWT — a user cannot access another family's data
4. Passwords require minimum 8 characters, enforced at the API layer
5. JWT secret must be minimum 32 characters in all environments
6. The `AuthenticatedAction` must be applied to every route except `POST /api/auth/register` and `POST /api/auth/login`
