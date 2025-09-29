# Muscledia User Service

A comprehensive user management microservice for the Muscledia fitness platform, providing user authentication, profile management, badge system, champion battles, avatar management, and notifications.

## Technology Stack

- **Framework**: Spring Boot 3.4.5
- **Security**: JWT Authentication with Spring Security
- **Database**: MySQL with JPA/Hibernate
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven

## API Documentation

### Base URL: `http://localhost:8081`

---

## 🔐 Authentication Controller

### POST `/api/users/login`

**Description**: Authenticate user and get JWT token  
**Security**: Public  
**Request Body**: `AuthenticationRequest`

```json
{
  "username": "string",
  "password": "string"
}
```

**Response**: `AuthenticationResponse`

- **200**: Successfully authenticated
- **401**: Invalid credentials
- **400**: Invalid request

---

## 👤 User Controller

### GET `/api/users/{id}`

**Description**: Get user by ID  
**Security**: USER/ADMIN role required  
**Response**: `User`

- **200**: User found
- **404**: User not found
- **403**: Forbidden

### POST `/api/users/register`

**Description**: Register new user  
**Security**: Public  
**Request Body**: `RegistrationRequest`

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

**Response**: `User`

- **201**: User successfully created
- **400**: Invalid input
- **409**: Username or email already exists

### PUT `/api/users/me`

**Description**: Update own user details  
**Security**: USER/ADMIN role required  
**Request Body**: `RegistrationRequest`  
**Response**: `User`

- **200**: User details successfully updated
- **400**: Invalid input
- **403**: Forbidden
- **404**: User not found
- **409**: Username or email already exists

### PUT `/api/users/{id}`

**Description**: Update user (Admin only)  
**Security**: ADMIN role required  
**Request Body**: `RegistrationRequest`  
**Response**: `User`

- **200**: User successfully updated
- **404**: User not found
- **409**: Username or email already exists
- **403**: Forbidden

### DELETE `/api/users/{id}`

**Description**: Delete user  
**Security**: ADMIN role required  
**Response**: `Void`

- **204**: User successfully deleted
- **404**: User not found
- **403**: Forbidden

### POST `/api/users/{id}/promote`

**Description**: Promote user to admin  
**Security**: ADMIN role required  
**Response**: `User`

- **200**: User successfully promoted to admin
- **404**: User not found
- **403**: Forbidden

### POST `/api/users/{id}/demote`

**Description**: Demote user from admin  
**Security**: ADMIN role required  
**Response**: `User`

- **200**: User successfully demoted from admin
- **404**: User not found
- **403**: Forbidden

---

## 🏆 User Badge Controller

### GET `/api/users/{userId}/badges`

**Description**: Get all badges for a user  
**Security**: USER/ADMIN role required  
**Response**: `List<UserBadge>`

- **200**: Badges retrieved successfully
- **404**: User not found
- **403**: Forbidden

### GET `/api/users/{userId}/badges/{badgeId}`

**Description**: Get specific badge for a user  
**Security**: USER/ADMIN role required  
**Response**: `UserBadge`

- **200**: Badge found
- **404**: Badge not found
- **403**: Forbidden

### POST `/api/users/{userId}/badges/{badgeId}`

**Description**: Award badge to user  
**Security**: ADMIN role required  
**Response**: `Void`

- **201**: Badge awarded successfully
- **404**: User not found
- **409**: Badge already awarded
- **403**: Forbidden

### PATCH `/api/users/{userId}/badges/{badgeId}/progress`

**Description**: Update badge progress  
**Security**: ADMIN role required  
**Query Param**: `progress` (int)  
**Response**: `Void`

- **204**: Progress updated successfully
- **404**: Badge not found
- **403**: Forbidden

### POST `/api/users/{userId}/badges`

**Description**: Create or update user badge  
**Security**: ADMIN role required  
**Request Body**: `UserBadge`  
**Response**: `UserBadge`

- **201**: Badge saved successfully
- **403**: Forbidden

---

## ⚔️ User Champion Controller

### GET `/api/user-champions/users/{userId}`

**Description**: Get all champions for a user  
**Security**: Authenticated  
**Response**: `List<UserChampion>`

- **200**: Champions retrieved successfully

### GET `/api/user-champions/users/{userId}/{championId}`

**Description**: Get specific champion for a user  
**Security**: Authenticated  
**Response**: `UserChampion`

- **200**: Champion found
- **404**: Champion not found for user

### POST `/api/user-champions/start`

**Description**: Start champion battle  
**Security**: Authenticated  
**Request Body**: `StartBattleRequest`

```json
{
  "userId": 1,
  "championId": 1
}
```

**Response**: `UserChampion`

- **201**: Battle started successfully
- **400**: Invalid request

### PATCH `/api/user-champions/progress`

**Description**: Update exercise count for champion battle  
**Security**: Authenticated  
**Request Body**: `UpdateExerciseCountRequest`

```json
{
  "userId": 1,
  "championId": 1,
  "count": 10
}
```

**Response**: `Void`

- **204**: Exercise count updated successfully
- **400**: Invalid request

### PATCH `/api/user-champions/defeat`

**Description**: Mark champion as defeated  
**Security**: Authenticated  
**Request Body**: `DefeatChampionRequest`

```json
{
  "userId": 1,
  "championId": 1
}
```

**Response**: `Void`

- **204**: Champion marked as defeated successfully
- **400**: Invalid request

---

## 🎭 Avatar Controller

### POST `/api/users/{userId}/avatars`

**Description**: Create avatar for user  
**Security**: Authenticated  
**Query Param**: `avatarType` (AvatarType enum)  
**Available Types**: `OGRE`, `DWARF`, `MINOTAUR`, `WEREWOLF`, `ELF`, `VAMPIRE`  
**Response**: `Avatar`

- **201**: Avatar created successfully
- **400**: Invalid avatar type
- **404**: User not found

### GET `/api/users/{userId}/avatar`

**Description**: Get user's primary avatar  
**Security**: Authenticated  
**Response**: `Avatar`

- **200**: Avatar found
- **404**: No avatar found for user

### GET `/api/users/{userId}/avatars`

**Description**: Get all avatars for user  
**Security**: Authenticated  
**Response**: `List<Avatar>`

- **200**: Avatars retrieved successfully

### GET `/api/avatars/{avatarId}`

**Description**: Get avatar by ID  
**Security**: Authenticated  
**Response**: `Avatar`

- **200**: Avatar found
- **404**: Avatar not found

### PATCH `/api/avatars/{avatarId}/level`

**Description**: Update avatar level  
**Security**: Authenticated  
**Request Body**: `UpdateAvatarLevelRequest`

```json
{
  "newLevel": 5
}
```

**Response**: `Avatar`

- **200**: Avatar level updated successfully

### PATCH `/api/avatars/{avatarId}/exp`

**Description**: Update avatar experience  
**Security**: Authenticated  
**Request Body**: `UpdateAvatarExpRequest`

```json
{
  "newExp": 1500
}
```

**Response**: `Avatar`

- **200**: Avatar experience updated successfully

### PATCH `/api/avatars/{avatarId}/ability`

**Description**: Unlock avatar ability  
**Security**: Authenticated  
**Request Body**: `UnlockAbilityRequest`

```json
{
  "abilityKey": "strength_boost",
  "abilityValue": true
}
```

**Response**: `Avatar`

- **200**: Ability unlocked successfully

### PATCH `/api/avatars/{avatarId}/flame`

**Description**: Set flame animation  
**Security**: Authenticated  
**Request Body**: `SetFlameAnimationRequest`

```json
{
  "enabled": true
}
```

**Response**: `Avatar`

- **200**: Flame animation updated successfully

### DELETE `/api/avatars/{avatarId}`

**Description**: Delete avatar  
**Security**: Authenticated  
**Response**: `Void`

- **204**: Avatar deleted successfully

---

## 🔔 Notification Controller

### GET `/api/users/{userId}/notifications`

**Description**: Get all notifications for user  
**Security**: Authenticated  
**Response**: `List<Notification>`

- **200**: Notifications retrieved successfully

### GET `/api/notifications/{notificationId}`

**Description**: Get notification by ID  
**Security**: Authenticated  
**Response**: `Notification`

- **200**: Notification found
- **404**: Notification not found

### GET `/api/users/{userId}/notifications/unread`

**Description**: Get unread notifications for user  
**Security**: Authenticated  
**Response**: `List<Notification>`

- **200**: Unread notifications retrieved successfully

### GET `/api/users/{userId}/notifications/unread/count`

**Description**: Get count of unread notifications  
**Security**: Authenticated  
**Response**: `Long`

- **200**: Count retrieved successfully

### PATCH `/api/notifications/{notificationId}/read`

**Description**: Mark notification as read  
**Security**: Authenticated  
**Response**: `Notification`

- **200**: Notification marked as read
- **404**: Notification not found

### PATCH `/api/users/{userId}/notifications/read-all`

**Description**: Mark all notifications as read  
**Security**: Authenticated  
**Response**: `Void`

- **204**: All notifications marked as read

### POST `/api/users/{userId}/notifications`

**Description**: Create notification  
**Security**: Authenticated  
**Query Params**:

- `type`: NotificationType (`BADGE`, `QUEST`, `CHAMPION`, `FRIEND`)
- `message`: String  
  **Response**: `Notification`
- **201**: Notification created successfully
- **400**: Invalid input

### DELETE `/api/notifications/{notificationId}`

**Description**: Delete notification  
**Security**: Authenticated  
**Response**: `Void`

- **204**: Notification deleted successfully
- **404**: Notification not found

---

## 🔧 Available Enums

### GoalType

- `LOSE_WEIGHT`
- `BUILD_STRENGTH`
- `GAIN_MUSCLE`

### AvatarType

- `OGRE`
- `DWARF`
- `MINOTAUR`
- `WEREWOLF`
- `ELF`
- `VAMPIRE`

### NotificationType

- `BADGE`
- `QUEST`
- `CHAMPION`
- `FRIEND`

---

## 🚀 Getting Started

1. **Prerequisites**

   - Java 21
   - Maven 3.6+
   - MySQL 8.0+

2. **Installation**

   ```bash
   git clone <repository-url>
   cd muscledia-user-service
   mvn clean install
   ```

3. **Configuration**

   - Update `application.yaml` with your database credentials
   - Configure JWT secret key

4. **Run Application**

   ```bash
   mvn spring-boot:run
   ```

5. **Access Swagger UI**
   - URL: `http://localhost:8080/swagger-ui.html`

---

## 🔐 Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

To get a token, use the `/api/users/login` endpoint with valid credentials.

---

## 📝 Response Format

### Success Response

Standard HTTP status codes with JSON response body.

### Error Response

```json
{
  "timestamp": "2024-03-21T10:15:30.123",
  "message": "Error description",
  "status": 404,
  "error": "Not Found"
}
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## 📄 License

This project is licensed under the MIT License.
