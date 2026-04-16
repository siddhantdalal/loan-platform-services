# Loan Platform Services

A production-grade **Loan Application Microservices System** built with Spring Boot, demonstrating event-driven architecture, distributed caching, API gateway pattern, and containerized deployment.

## Architecture

```
                    ┌─────────────┐
   Client ────────► │ API Gateway │ (port 8080)
                    └──────┬──────┘
                           │  JWT Validation + Routing
              ┌────────────┼────────────┐
              ▼            ▼            ▼
        ┌───────────┐ ┌──────────┐ ┌──────────────────┐
        │User Service│ │Loan Svc  │ │Notification Svc  │
        │  (8081)    │ │ (8082)   │ │    (8083)        │
        └─────┬─────┘ └────┬─────┘ └────────┬─────────┘
              │             │                │
              ▼             ▼                ▼
         [PostgreSQL]  [PostgreSQL]    [PostgreSQL]
         [Redis Cache] [Redis Cache]
              │             │                ▲
              └──────► [Apache Kafka] ◄──────┘
```

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 3.2** | Microservice framework |
| **Spring Cloud Gateway** | API Gateway with reactive routing |
| **Spring Security + JWT** | Authentication & authorization |
| **Apache Kafka** | Asynchronous event-driven messaging |
| **Redis** | Distributed caching |
| **PostgreSQL** | Relational database (per-service) |
| **Docker Compose** | Container orchestration |
| **Maven** | Multi-module build system |

## Services

### API Gateway (port 8080)
- Routes requests to downstream services
- Validates JWT tokens via a global filter
- Forwards authenticated user context (`X-User-Email`, `X-User-Role`) headers

### User Service (port 8081)
- User registration with BCrypt password hashing
- JWT token generation on login
- User profile management with Redis caching
- Publishes `user-events` to Kafka on registration/update

### Loan Service (port 8082)
- Loan application submission and retrieval
- Loan approval/rejection workflow
- Automatic interest rate calculation
- Redis caching for loan data
- Publishes `loan-events` to Kafka
- Consumes `user-events` from Kafka

### Notification Service (port 8083)
- Consumes events from both `user-events` and `loan-events` Kafka topics
- Generates and persists email notifications
- Configurable email sending (SMTP or log-only mode)
- Notification audit trail via REST API

## Project Structure

```
loan-platform-services/
├── pom.xml                     # Parent POM (dependency management)
├── docker-compose.yml          # Full stack orchestration
├── common/                     # Shared library module
│   └── src/main/java/
│       └── com.loanplatform.common/
│           ├── dto/            # ApiResponse, UserDto, LoanApplicationDto
│           ├── event/          # UserEvent, LoanEvent (Kafka payloads)
│           ├── exception/      # Global exception handling
│           ├── security/       # JwtUtil
│           └── constants/      # KafkaConstants
├── api-gateway/                # Spring Cloud Gateway
├── user-service/               # User management & auth
├── loan-service/               # Loan processing
└── notification-service/       # Event-driven notifications
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Quick Start with Docker Compose

```bash
# Build and start all services
docker compose up --build

# Services will be available at:
# API Gateway:          http://localhost:8080
# User Service:         http://localhost:8081
# Loan Service:         http://localhost:8082
# Notification Service: http://localhost:8083
```

### Local Development

```bash
# 1. Start infrastructure only
docker compose up -d postgres-users postgres-loans postgres-notifications redis zookeeper kafka

# 2. Build the project
mvn clean install -DskipTests

# 3. Run each service (in separate terminals)
cd user-service && mvn spring-boot:run
cd loan-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

## API Endpoints

### Authentication
```bash
# Register a new user
POST /api/auth/register
{
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}

# Login
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Users (requires JWT)
```bash
GET /api/users/{id}          # Get user by ID
GET /api/users/me            # Get current user profile
PUT /api/users/{id}          # Update user
```

### Loans (requires JWT)
```bash
POST /api/loans              # Submit loan application
{
  "userId": 1,
  "amount": 50000,
  "termMonths": 36,
  "purpose": "Home renovation"
}

GET /api/loans/{id}              # Get loan by ID
GET /api/loans/user/{userId}     # Get all loans for a user
PUT /api/loans/{id}/decision     # Approve/reject loan
{
  "decision": "APPROVED",
  "interestRate": 7.5
}
```

### Notifications (requires JWT)
```bash
GET /api/notifications/user/{userId}   # Get all notifications for a user
```

## End-to-End Test Flow

```bash
# 1. Register
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123","firstName":"John","lastName":"Doe","phone":"1234567890"}' \
  | jq -r '.data.token')

# 2. Submit a loan application
curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"userId":1,"amount":50000,"termMonths":36,"purpose":"Home renovation"}'

# 3. Approve the loan
curl -X PUT http://localhost:8080/api/loans/1/decision \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"decision":"APPROVED","interestRate":7.5}'

# 4. Check notifications
curl http://localhost:8080/api/notifications/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

## Key Design Decisions

- **Database-per-service**: Each microservice owns its database, ensuring loose coupling
- **Event-driven communication**: Services communicate asynchronously via Apache Kafka
- **API Gateway pattern**: Single entry point with centralized JWT authentication
- **Distributed caching**: Redis reduces database load for frequently accessed data
- **String-based Kafka serialization**: Manual JSON serialization avoids cross-JVM class-loading issues
- **Separate consumer groups**: Notification and Loan services independently consume the same Kafka topics
