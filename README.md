# TODO List Service

A RESTful web service for managing TODO items.

## 📋 Service Description

This TODO List Service provides a complete REST API for managing personal tasks, which are saved in an H2 in-memory
database, offering following capabilities and features:

- **Create, Read, Update** TODO items with descriptions and due dates
- **Mark items as done or not done** with automatic timestamp tracking
- **Automatic status management** - items past their due date are automatically marked as "past due" by a scheduled job
  which runs every 30 min, but also when fetching the item from the database ensuring high data consistency
- **Status-based filtering** - retrieve all items or only "not done" items
- **Immutable past due items** - prevents modification of overdue tasks to maintain data integrity

## 🔌 API Endpoints

The REST API is documented in [`api/openapi.yaml`](api/openapi.yaml).

### Base URL

- `http://localhost:8080/api`

### Following are the main API endpoints:

- `POST /todos` — Create a new todo item
    - Request body: `CreateTodoItem` (`description`, `dueDateTime`)
    - Responses: `201` (TodoItem), `400` (validation error)

- `GET /todos` — List todo items
    - Query params:
        - `includeAll` (boolean, default: `false`) — if `true`, includes DONE and PAST_DUE items
    - Responses: `200` (List of TodoItem)

- `GET /todos/{id}` — Get a single todo item
    - Path params: `id` (int64)
    - Responses: `200` (TodoItem), `404` (not found)

- `PATCH /todos/{id}` — Update a todo item
    - Path params: `id` (int64)
    - Request body fields (optional):
        - `description` (string)
        - `status` (`NOT_DONE` | `DONE`)
    - Responses: `200` (TodoItem), `400` (validation error), `404` (not found), `422` (cannot modify past due item)

## 🛠️ Tech Stack

## Supported application profiles

- `development` - Profile used for local development
- `test` - Profile used for running integration tests
- `production` - Production profile

### Runtime Environment

- **Java 21**
- **Kotlin 2.1.10**

### Frameworks & Libraries

- **Spring Boot 4.0.1** - Application framework
    - Spring Web - REST API
    - Spring Data JPA - Data access layer
    - Spring Validation - Request validation
    - Spring Scheduling - Automatic status updates
- **Hibernate 7.2.0** - ORM/JPA implementation
- **PostgreSQL 42.7.8** - Production database driver
- **H2 Database** - In-memory database for testing
- **Gradle 9.2.1** - Build tool
- **Jackson** - JSON serialization/deserialization
- **ArchUnit** - For testing and enforcing architectural rules in our codebase
- **junit5** - Testing framework
- **Mockk** - Mocking library for Kotlin
- **detekt** - Static code analysis for Kotlin

### Architecture

- **The application following a simple Hexagonal Onion Architecture**
    - Domain layer - Core business logic
    - Application layer - Use cases and scheduling
    - Adapter layer - REST controllers and persistence

## 🚀 How-To Guide

### Prerequisites

- **Docker** and **Docker Compose** installed
- **Java 21** (for local development without Docker)
- **Gradle** (wrapper included, no installation needed)

### 1. Building the service

#### Using Gradle Wrapper

```bash
# Clean and build the project
./gradlew clean build
```

#### Using Docker

```bash
# Build Docker image
docker build -t todoapp:latest .
```

### 2. Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "TodoAppApplicationTests"
```

### 3. Running the service locally

#### Option A: Using Docker Compose

```bash
# Start services 
docker-compose up

# Stop services
docker-compose down
```

#### Option B: Using command line

```bash
1. Run the application using Gradle wrapper: ./gradlew bootRun --args='--spring.profiles.active=development'
```

#### Option C: Using IntelliJ IDEA

```bash
1. Just run the main application class `TodoAppApplication.kt` from IntelliJ IDEA
```

## 🔧 Configuration

### Application Properties

Key configurations in `application.yaml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Use 'update' for production
    show-sql: false          # Set to true for SQL debugging

  scheduling:
    todo-items-status-update:
      enabled: true
      cron: "0 */30 * * * ?"  # Every 30 minutes

server:
  port: 8080
```

### Environment Variables

Required environment variables:

- `SPRING_DATASOURCE_URL` - PostgreSQL JDBC URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

## 🎯 Future Enhancements

1. User authentication and authorization
2. Adding integration, e2e tests and API spec tests
3. Adding a CI pipeline for automated testing and deployment
4. Adding a CI vulnerability pipeline which scans application dependencies for vulnerabilities
5. Adding API pagination for large datasets
6. Add a code static analysis tool like detekt [https://github.com/detekt/detekt]
7. Using ShedLock for distributed scheduling in order to avoid multiple instances running the scheduled job
   simultaneously
8. Adding an automated dependency update tool like Renovate [https://docs.renovatebot.com]
9. Adding observability including alerting and monitoring 
