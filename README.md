# Muscledia - User Service

Core authentication and user management microservice for the Muscledia platform. Handles user registration, JWT-based authentication, profile management, avatar progression, badge tracking, champion battles, and notifications.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Security | Spring Security with JWT |
| Database | MySQL 8.0 with JPA / Hibernate |
| Build | Maven |
| Docs | OpenAPI / Swagger UI |

---

## Architecture

This service is the authentication authority for the Muscledia ecosystem. It issues JWT tokens that all other services validate locally — no token round-trips at runtime.

**Responsibilities:**
- User registration and authentication — issues JWT tokens on login
- Profile management — user details, goals, weight tracking
- Avatar system — RPG-style avatars with level, experience, and unlockable abilities
- Badge tracking — award and progress tracking for achievement badges
- Champion battles — track user progress against muscle champion bosses
- Notifications — in-app notification delivery and read state management

**Kafka Events Published:**
- `UserRegisteredEvent` — consumed by the Gamification Service to initialise a user's XP profile on account creation

---

## Authentication

Login returns a JWT Bearer token. Include it in all protected requests:

```
Authorization: Bearer <your-jwt-token>
```

**Access levels:**
- Public — no token required
- Authenticated — valid JWT required
- Admin — valid JWT with `ADMIN` role required

---

## API Reference

**Base URL:** `http://localhost:8081`
**Swagger UI:** `http://localhost:8081/swagger-ui.html`
**Health check:** `http://localhost:8081/actuator/health`

---

### Authentication — `/api/users`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/login` | Public | Authenticate and receive JWT token |
| POST | `/register` | Public | Register a new user account |

**Login request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Registration request:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "birthDate": "YYYY-MM-DD",
  "gender": "string",
  "height": 180.5,
  "initialWeight": 75.0,
  "goalType": "BUILD_STRENGTH",
  "initialAvatarType": "WEREWOLF"
}
```

---

### User Management — `/api/users`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/{id}` | Authenticated | Get user by ID |
| PUT | `/me` | Authenticated | Update own profile |
| PUT | `/{id}` | Admin | Update any user |
| DELETE | `/{id}` | Admin | Delete user |
| POST | `/{id}/promote` | Admin | Promote user to admin |
| POST | `/{id}/demote` | Admin | Demote user from admin |

---

### Badges — `/api/users/{userId}/badges`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/` | Authenticated | Get all badges for user |
| GET | `/{badgeId}` | Authenticated | Get specific badge |
| POST | `/{badgeId}` | Admin | Award badge to user |
| PATCH | `/{badgeId}/progress` | Admin | Update badge progress |
| POST | `/` | Admin | Create or update user badge |

---

### Champion Battles — `/api/user-champions`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/users/{userId}` | Authenticated | Get all champions for user |
| GET | `/users/{userId}/{championId}` | Authenticated | Get specific champion progress |
| POST | `/start` | Authenticated | Start a champion battle |
| PATCH | `/progress` | Authenticated | Update exercise count toward battle |
| PATCH | `/defeat` | Authenticated | Mark champion as defeated |

**Start battle request:**
```json
{
  "userId": 1,
  "championId": 1
}
```

---

### Avatars — `/api/users/{userId}/avatars` and `/api/avatars`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/users/{userId}/avatars` | Authenticated | Create avatar for user |
| GET | `/api/users/{userId}/avatar` | Authenticated | Get user's primary avatar |
| GET | `/api/users/{userId}/avatars` | Authenticated | Get all avatars for user |
| GET | `/api/avatars/{avatarId}` | Authenticated | Get avatar by ID |
| PATCH | `/api/avatars/{avatarId}/level` | Authenticated | Update avatar level |
| PATCH | `/api/avatars/{avatarId}/exp` | Authenticated | Update avatar experience |
| PATCH | `/api/avatars/{avatarId}/ability` | Authenticated | Unlock avatar ability |
| PATCH | `/api/avatars/{avatarId}/flame` | Authenticated | Set flame animation |
| DELETE | `/api/avatars/{avatarId}` | Authenticated | Delete avatar |

**Available avatar types:** `OGRE` · `DWARF` · `MINOTAUR` · `WEREWOLF` · `ELF` · `VAMPIRE`

---

### Notifications — `/api/users/{userId}/notifications` and `/api/notifications`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/users/{userId}/notifications` | Authenticated | Get all notifications |
| GET | `/api/users/{userId}/notifications/unread` | Authenticated | Get unread notifications |
| GET | `/api/users/{userId}/notifications/unread/count` | Authenticated | Get unread count |
| POST | `/api/users/{userId}/notifications` | Authenticated | Create notification |
| PATCH | `/api/users/{userId}/notifications/read-all` | Authenticated | Mark all as read |
| GET | `/api/notifications/{notificationId}` | Authenticated | Get notification by ID |
| PATCH | `/api/notifications/{notificationId}/read` | Authenticated | Mark notification as read |
| DELETE | `/api/notifications/{notificationId}` | Authenticated | Delete notification |

**Notification types:** `BADGE` · `QUEST` · `CHAMPION` · `FRIEND`

---

## Domain Enums

| Enum | Values |
|---|---|
| GoalType | `LOSE_WEIGHT` · `BUILD_STRENGTH` · `GAIN_MUSCLE` |
| AvatarType | `OGRE` · `DWARF` · `MINOTAUR` · `WEREWOLF` · `ELF` · `VAMPIRE` |
| NotificationType | `BADGE` · `QUEST` · `CHAMPION` · `FRIEND` |

---

## Error Response Format

```json
{
  "timestamp": "2024-03-21T10:15:30.123",
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```

---

## Running Locally

**Prerequisites:** Java 21, Maven 3.6+, MySQL 8.0+

```bash
# Create the database
mysql -u root -p -e "CREATE DATABASE muscledia;"
```

**application.yml configuration:**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/muscledia
    username: <your-username>
    password: <your-password>
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: <your-secret-key>
  issuer: muscledia-user-service
  expiration: 86400000
```

```bash
# Build and run
mvn clean install
mvn spring-boot:run
```

**Endpoints after startup:**
- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Health check: `http://localhost:8081/actuator/health`

---

## Known Limitations

- Password reset flow not yet implemented
- Notification delivery is in-app only — email and push notification channels are planned
- Avatar ability system currently stores key-value pairs — a typed ability schema is planned for a future iteration
