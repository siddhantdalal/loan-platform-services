# Loan Platform Services

A full-stack **Loan Application Microservices System** with a React frontend and Spring Boot backend, demonstrating event-driven architecture, distributed caching, API gateway pattern, and containerized deployment.

## Architecture

```
  ┌──────────────┐
  │   React UI   │ (port 3000)
  │   Frontend   │
  └──────┬───────┘
         │
         ▼
  ┌─────────────┐
  │ API Gateway  │ (port 8080) - JWT Validation + Routing
  └──────┬──────┘
         │
    ┌────┼──────────────┐
    ▼    ▼              ▼
┌────────┐ ┌─────────┐ ┌──────────────┐
│  User  │ │  Loan   │ │ Notification │
│Service │ │ Service │ │   Service    │
│ (8081) │ │ (8082)  │ │   (8083)     │
└───┬────┘ └────┬────┘ └──────┬───────┘
    │           │             │
    ▼           ▼             ▼
[PostgreSQL] [PostgreSQL] [PostgreSQL]
[Redis]      [Redis]
    │           │             ▲
    └────► [Apache Kafka] ◄──┘
```

## Tech Stack

| Technology | Purpose |
|---|---|
| **React 19** | Single-page application frontend |
| **Spring Boot 3.2** | Microservice framework |
| **Spring Cloud Gateway** | API Gateway with reactive routing |
| **Spring Security + JWT** | Authentication & authorization |
| **Apache Kafka** | Asynchronous event-driven messaging |
| **Redis** | Distributed caching |
| **PostgreSQL** | Relational database (per-service) |
| **Docker Compose** | Container orchestration |
| **Nginx** | Frontend static file serving & API proxy |
| **Maven** | Multi-module build system |

## Frontend Pages

| Page | Description |
|---|---|
| **Login** | JWT authentication with email/password |
| **Register** | New user registration with form validation |
| **Dashboard** | Loan application overview with stats and status tracking |
| **Apply for Loan** | Loan application form with monthly payment estimator |
| **Admin Panel** | Approve/reject pending loan applications |
| **Notifications** | Real-time view of all system notifications |

## Backend Services

### API Gateway (port 8080)
- Routes requests to downstream services
- Validates JWT tokens via a global filter
- CORS configuration for frontend integration
- Forwards authenticated user context headers

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
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── components/         # Navbar, PrivateRoute
│   │   ├── pages/              # Login, Register, Dashboard, ApplyLoan, AdminPanel, Notifications
│   │   ├── services/           # Axios API client
│   │   └── context/            # Auth context (JWT state management)
│   ├── nginx.conf              # Production Nginx config
│   └── Dockerfile              # Multi-stage Node + Nginx build
├── common/                     # Shared Java library
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
- Node.js 18+ (for frontend development)
- Docker & Docker Compose

### Quick Start with Docker Compose

```bash
# Build and start everything (frontend + backend + infrastructure)
docker compose up --build

# Open the app:
# Frontend:             http://localhost:3000
# API Gateway:          http://localhost:8080
```

### Local Development

```bash
# 1. Start infrastructure only
docker compose up -d postgres-users postgres-loans postgres-notifications redis zookeeper kafka

# 2. Build and run backend services
mvn clean install -DskipTests
cd user-service && mvn spring-boot:run &
cd loan-service && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &

# 3. Run frontend (in a new terminal)
cd frontend
npm install
npm start
# Opens at http://localhost:3000
```

### Running Tests

```bash
# Backend tests
mvn test

# Frontend build check
cd frontend && npm run build
```

## API Endpoints

### Authentication
```bash
POST /api/auth/register    # Register a new user
POST /api/auth/login       # Login and receive JWT token
```

### Users (requires JWT)
```bash
GET /api/users/{id}        # Get user by ID
GET /api/users/me          # Get current user profile
PUT /api/users/{id}        # Update user
```

### Loans (requires JWT)
```bash
POST /api/loans                  # Submit loan application
GET /api/loans/{id}              # Get loan by ID
GET /api/loans/user/{userId}     # Get all loans for a user
PUT /api/loans/{id}/decision     # Approve/reject loan
```

### Notifications (requires JWT)
```bash
GET /api/notifications/user/{userId}   # Get all notifications for a user
```

## Key Design Decisions

- **Full-stack architecture**: React frontend with Nginx reverse proxy communicating with Spring Boot microservices
- **Database-per-service**: Each microservice owns its database, ensuring loose coupling
- **Event-driven communication**: Services communicate asynchronously via Apache Kafka
- **API Gateway pattern**: Single entry point with centralized JWT authentication
- **Distributed caching**: Redis reduces database load for frequently accessed data
- **String-based Kafka serialization**: Manual JSON serialization avoids cross-JVM class-loading issues
- **Separate consumer groups**: Notification and Loan services independently consume the same Kafka topics
- **Multi-stage Docker builds**: Optimized container images for both Java services and React frontend
