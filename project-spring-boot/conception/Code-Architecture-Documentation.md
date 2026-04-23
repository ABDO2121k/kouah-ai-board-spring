# Code Architecture Documentation

## Table of Contents

1. [Overview](#overview)
2. [Package Structure](#package-structure)
3. [Core Components](#core-components)
4. [Utilities and Helpers](#utilities-and-helpers)
5. [Exception Handling](#exception-handling)
6. [Security Components](#security-components)
7. [Design Patterns Used](#design-patterns-used)

---

## Overview

This Spring Boot application implements a project management system with RBAC (Role-Based Access Control), JWT authentication, and real-time notifications via WebSocket. The architecture follows clean code principles with separation of concerns.

---

## Package Structure

```
kouahai.springboot.project/
├── config/                    # Application configuration classes
├── controller/                # REST API controllers
├── dto/                       # Data Transfer Objects
├── exception/                 # Exception handling
│   └── handler/              # Global exception handlers
├── model/                     # JPA entities
├── repository/                # Data access layer
├── security/                  # Security components
│   ├── jwt/                  # JWT utilities
│   └── user/                 # User details implementation
├── service/                   # Business logic interfaces
│   └── impl/                 # Service implementations
├── util/                      # Utility classes
│   ├── constants/            # Application constants
│   ├── validators/           # Validation utilities
│   └── mappers/              # Object mappers
└── ProjectApplication.java   # Main application class
```

---

## Core Components

### 1. Configuration (`config/`)

#### SecurityConfig.java

**Purpose**: Configures Spring Security with JWT authentication

- Defines security filter chain
- Configures CORS settings
- Sets up authentication provider
- Manages password encoding

**Key Features**:

- Stateless session management
- JWT-based authentication
- Public endpoints for authentication and documentation
- CORS configuration for frontend integration

#### WebSocketConfig.java

**Purpose**: Configures WebSocket for real-time notifications

- Enables STOMP messaging
- Sets up message broker
- Configures WebSocket endpoints

#### OpenApiConfig.java

**Purpose**: Configures Swagger/OpenAPI documentation

- Defines API information
- Sets up security schemes
- Configures JWT bearer authentication

---

### 2. Controllers (`controller/`)

#### AuthController.java

**Purpose**: Handles authentication and user registration
**Endpoints**:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/verify` - Email verification
- `POST /api/auth/resend-verification` - Resend verification email

#### ProjectController.java

**Purpose**: Manages project CRUD operations and member management
**Endpoints**:

- `POST /api/projects` - Create project
- `GET /api/projects` - Get user's projects
- `PUT /api/projects/{id}` - Update project
- `POST /api/projects/{id}/members` - Add member to project

#### SprintController.java

**Purpose**: Manages sprint operations
**Endpoints**:

- `POST /api/sprints` - Create sprint
- `GET /api/projects/{projectId}/sprints` - Get project sprints
- `POST /api/sprints/{id}/members` - Add sprint member

#### NotificationController.java

**Purpose**: Handles real-time notifications
**Endpoints**:

- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark as read

---

### 3. Data Transfer Objects (`dto/`)

#### Request DTOs

- **RegisterRequest**: User registration data
- **LoginRequest**: Login credentials
- **CreateProjectRequest**: Project creation data
- **CreateSprintRequest**: Sprint creation data
- **AddMemberRequest**: Add member to project
- **UpdateProjectRequest**: Project update data

#### Response DTOs

- **AuthResponse**: Authentication response with JWT token
- **ProjectResponse**: Project information
- **SprintResponse**: Sprint information
- **NotificationResponse**: Notification data
- **ProjectMemberResponse**: Member information

#### Utility DTOs

- **ValidationResult**: Validation outcome wrapper
- **RolePermissions**: Role-based permissions mapping
- **ErrorResponse**: Standardized error response

---

## Exception Handling

### Custom Exceptions (`exception/`)

#### DuplicateResourceException

**Purpose**: Thrown when attempting to create a resource that already exists
**Usage**: Email already registered, project name exists, etc.

```java
throw new DuplicateResourceException("Email already registered");
```

#### ResourceNotFoundException

**Purpose**: Thrown when a requested resource cannot be found
**Usage**: User not found, project not found, etc.

```java
throw new ResourceNotFoundException("Project not found with id: " + id);
```

#### UnauthorizedException

**Purpose**: Thrown when user lacks permission for an operation
**Usage**: Insufficient permissions, unauthorized access

```java
throw new UnauthorizedException("Only PRODUCT_OWNER can add SCRUM_MASTER");
```

#### VerificationException

**Purpose**: Thrown when email verification fails
**Usage**: Invalid token, expired token

```java
throw new VerificationException("Invalid verification token");
```

### GlobalExceptionHandler.java

**Purpose**: Centralized exception handling for the entire application

**Features**:

- Handles all custom exceptions
- Processes validation errors
- Catches Spring Security exceptions
- Provides consistent error responses
- Logs all exceptions

**Exception Mappings**:

- `DuplicateResourceException` → 400 BAD_REQUEST
- `ResourceNotFoundException` → 404 NOT_FOUND
- `UnauthorizedException` → 403 FORBIDDEN
- `VerificationException` → 400 BAD_REQUEST
- `BadCredentialsException` → 401 UNAUTHORIZED
- `MethodArgumentNotValidException` → 400 BAD_REQUEST (with field errors)
- `Exception` → 500 INTERNAL_SERVER_ERROR

**Error Response Format**:

```json
{
  "timestamp": "2026-01-22T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered",
  "validationErrors": {
    "email": "must be a valid email"
  }
}
```

### ErrorResponse.java

**Purpose**: Standardized error response DTO

**Fields**:

- `timestamp`: When error occurred
- `status`: HTTP status code
- `error`: HTTP status reason phrase
- `message`: User-friendly error message
- `validationErrors`: Field-level validation errors (optional)

**Features**:

- Uses `@JsonInclude(NON_NULL)` to exclude null fields
- Builder pattern for easy construction
- Immutable after construction

---

## Security Components

### JWT Authentication (`security/`)

#### JwtService.java

**Purpose**: Manages JWT token generation and validation

**Key Methods**:

- `generateToken(UserDetails)`: Creates JWT token
- `extractUsername(String token)`: Extracts username from token
- `isTokenValid(String token, UserDetails)`: Validates token
- `extractClaim(String token, Function)`: Extracts custom claims

**Features**:

- Uses HMAC-SHA algorithm for signing
- Configurable expiration time
- Secure key management via application properties

#### JwtAuthenticationFilter.java

**Purpose**: Intercepts HTTP requests to validate JWT tokens

**Workflow**:

1. Extracts JWT from Authorization header
2. Validates token format (Bearer token)
3. Extracts username from token
4. Loads user details from database
5. Validates token against user details
6. Sets authentication in SecurityContext

**Features**:

- Extends `OncePerRequestFilter`
- Non-blocking filter chain execution
- Skips authentication for public endpoints

#### CustomUserDetailsService.java

**Purpose**: Loads user-specific data for authentication

**Key Methods**:

- `loadUserByUsername(String)`: Standard Spring Security method
- `loadUserByUsernameWithProjectRole(String, String)`: Custom method for project-specific roles

**Features**:

- Validates user status (ACTIVE only)
- Integrates with User repository
- Throws appropriate exceptions

#### CustomUserDetails.java

**Purpose**: Implements Spring Security UserDetails interface

**Features**:

- Wraps User entity
- Provides authorities based on user roles
- Factory methods for creating instances

---

## Service Layer

### Business Services (`service/` and `service/impl/`)

#### AuthService & AuthServiceImpl

**Purpose**: Handles authentication and user management

**Key Operations**:

- User registration with email verification
- Login with JWT token generation
- Email verification workflow
- Resend verification email

**Security Features**:

- Password encryption with BCrypt
- Email verification tokens
- Token expiration handling

#### AuthorizationService & AuthorizationServiceImpl

**Purpose**: Implements Role-Based Access Control (RBAC)

**Key Operations**:

- `canMoveTaskToColumn()`: Validates task movement permissions
- `validateTaskMove()`: Comprehensive task move validation
- `getRolePermissions()`: Returns role-specific permissions
- `canAddMemberWithRole()`: Validates member addition permissions

**Role Hierarchy**:

1. **PRODUCT_OWNER**: Creates projects, manages epics, assigns Scrum Master
2. **SCRUM_MASTER**: Manages team members, manages sprints, creates tasks
3. **DEVELOPER**: Works on tasks, moves tasks (except tester columns)
4. **TESTER**: All developer permissions + access to tester-only columns

**Permission Matrix**:
| Permission | PO | SM | DEV | TESTER |
|-----------|----|----|-----|--------|
| Create Project | ✓ | ✗ | ✗ | ✗ |
| Add Scrum Master | ✓ | ✗ | ✗ | ✗ |
| Manage Members | ✗ | ✓ | ✗ | ✗ |
| Manage Sprints | ✗ | ✓ | ✗ | ✗ |
| Move to Tester Columns | ✗ | ✗ | ✗ | ✓ |

#### ProjectService & ProjectServiceImpl

**Purpose**: Manages project lifecycle and membership

**Key Operations**:

- Create, read, update, delete projects
- Add/remove project members
- Manage project roles
- Retrieve user's projects

#### SprintService & SprintServiceImpl

**Purpose**: Manages sprint operations

**Key Operations**:

- Create and manage sprints
- Add sprint members
- Track sprint progress

#### NotificationService & NotificationServiceImpl

**Purpose**: Handles real-time notifications

**Key Operations**:

- Create notifications
- Send via WebSocket
- Mark as read
- Retrieve user notifications

#### EmailService & EmailServiceImpl

**Purpose**: Sends email notifications

**Key Operations**:

- Send verification emails
- Send notification emails
- Template-based email generation

---

## Utilities and Helpers

### Validation (`dto/ValidationResult.java`)

#### ValidationResult

**Purpose**: Wrapper for validation results with success/failure state

**Usage**:

```java
ValidationResult result = ValidationResult.success();
ValidationResult error = ValidationResult.error("Invalid input");

if (!result.isValid()) {
    throw new UnauthorizedException(result.getErrorMessage());
}
```

**Features**:

- Immutable design
- Factory methods for creation
- Clear success/failure indication

---

## Models (Entities)

### Core Entities (`model/`)

#### User

**Purpose**: Represents system users
**Key Fields**: username, email, password, status, role
**Relationships**: One-to-Many with ProjectMember, Notification

#### Project

**Purpose**: Represents projects
**Key Fields**: name, description, startDate, endDate
**Relationships**:

- One-to-Many with ProjectMember
- One-to-Many with Sprint
- One-to-Many with Epic

#### ProjectMember

**Purpose**: Join table for User-Project with role
**Key Fields**: user, project, role, joinedAt
**Role Types**: PRODUCT_OWNER, SCRUM_MASTER, DEVELOPER, TESTER

#### Sprint

**Purpose**: Represents project sprints
**Key Fields**: name, goal, startDate, endDate, project
**Relationships**: One-to-Many with Task

#### Task

**Purpose**: Represents work items
**Key Fields**: title, description, status, priority, assignee
**Relationships**:

- Many-to-One with UserStory
- Many-to-One with KanbanColumn
- One-to-Many with TaskComment

#### Epic

**Purpose**: Large features or initiatives
**Key Fields**: title, description, priority, productOwner
**Relationships**: One-to-Many with UserStory

#### UserStory

**Purpose**: User requirements
**Key Fields**: title, description, acceptanceCriteria, storyPoints
**Relationships**:

- Many-to-One with Epic
- One-to-Many with Task

#### KanbanColumn

**Purpose**: Kanban board columns
**Key Fields**: name, position, testerOnly
**Features**: Column-level permissions for tester-only columns

#### Notification

**Purpose**: User notifications
**Key Fields**: user, message, type, read, createdAt
**Types**: TASK_ASSIGNED, SPRINT_CREATED, PROJECT_MEMBER_ADDED, etc.

---

## Design Patterns Used

### 1. Service Layer Pattern

- Separation of business logic from controllers
- Interface-based design for flexibility
- Implementation classes in `impl/` package

### 2. Repository Pattern

- Spring Data JPA repositories
- Abstraction over data access
- Custom query methods

### 3. DTO Pattern

- Separates internal models from API contracts
- Request and Response DTOs
- Validation at DTO level

### 4. Builder Pattern

- Used in DTOs and Error responses
- Provided by Lombok `@Builder`
- Fluent API for object construction

### 5. Factory Pattern

- `ValidationResult.success()` and `ValidationResult.error()`
- `CustomUserDetails.fromUser()`
- Static factory methods

### 6. Filter Chain Pattern

- `JwtAuthenticationFilter` in security chain
- Spring Security filter chain
- Request processing pipeline

### 7. Strategy Pattern

- Authorization strategies per role
- Different permissions per role type
- Flexible permission checking

### 8. Singleton Pattern

- Spring beans are singletons by default
- Services, repositories, configurations

---

## Best Practices Implemented

### 1. Exception Handling

✅ Centralized exception handling with `@RestControllerAdvice`
✅ Custom exceptions for specific scenarios
✅ Consistent error response format
✅ Logging all exceptions

### 2. Security

✅ JWT-based stateless authentication
✅ Password encryption with BCrypt
✅ Email verification workflow
✅ Role-based access control
✅ CORS configuration

### 3. Code Organization

✅ Clear package structure
✅ Separation of concerns
✅ Interface-based service layer
✅ DTOs for API contracts

### 4. Logging

✅ SLF4J with Lombok `@Slf4j`
✅ Appropriate log levels (error, warn, debug)
✅ Contextual logging information

### 5. Validation

✅ Bean validation annotations
✅ Custom validation logic in services
✅ Validation result wrapper

### 6. API Documentation

✅ OpenAPI/Swagger integration
✅ Security scheme documentation
✅ Endpoint descriptions

---

## Configuration Properties

### JWT Configuration

```properties
jwt.secret=<base64-encoded-secret>
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Database Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/projectdb
spring.datasource.username=<username>
spring.datasource.password=<password>
```

### Email Configuration

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<email>
spring.mail.password=<app-password>
```

---

## Future Enhancements

### Recommended Improvements

1. **Utility Package Enhancement**
   - Date/Time utilities
   - String utilities
   - Collection utilities
   - Validation utilities

2. **Constants Package**
   - API endpoints constants
   - Error messages constants
   - Email templates constants
   - Business rules constants

3. **Mapper Package**
   - Entity to DTO mappers
   - DTO to Entity mappers
   - Consider MapStruct integration

4. **Aspect-Oriented Programming**
   - Logging aspect
   - Performance monitoring
   - Audit trail

5. **Testing Enhancements**
   - Unit tests for all services
   - Integration tests for controllers
   - Security tests

6. **Caching**
   - Redis integration
   - Cache frequently accessed data
   - Cache invalidation strategies

7. **Monitoring**
   - Spring Boot Actuator
   - Metrics collection
   - Health checks

---

## Conclusion

This application follows modern Spring Boot best practices with:

- Clean architecture
- SOLID principles
- Security-first approach
- Comprehensive error handling
- Role-based access control
- Real-time capabilities

The modular structure allows for easy maintenance, testing, and future enhancements.
