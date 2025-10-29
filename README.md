# Movie Rating Service

A RESTful API for a movie rating service built with Spring Boot, PostgreSQL, and JWT authentication.

## Features

- **User Management**: Registration and JWT-based authentication
- **Movie Browsing**: List all movies and view details (public access)
- **Movie Ratings**: Rate movies, update ratings, and delete ratings (authenticated users)
- **Top Rated**: Get the highest-rated movie based on user ratings
- **Database Migrations**: Liquibase for version-controlled schema changes
- **API Documentation**: Interactive Swagger UI
- **Containerization**: Docker and Docker Compose support
- **Testing**: Comprehensive unit and integration tests with Testcontainers

## 🛠Technology Stack

- **Java 21**
- **Spring Boot 3.2.0**
    - Spring Web
    - Spring Data JPA
    - Spring Security
- **PostgreSQL 15**
- **Liquibase** for database migrations
- **JWT** for authentication
- **MapStruct** for DTO mapping
- **Testcontainers** for integration testing
- **Swagger/OpenAPI** for API documentation
- **Docker** for containerization

## Prerequisites

- Java 21 or higher
- Maven 3.9+ for local build
- Docker and Docker Compose (for containerized setup)
- PostgreSQL 15 (if running locally without Docker)

## Quick Start

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd movie-rating-service
```

2. **Create environment file <Optional, you can skip this step>** 
```bash
cp .env.example .env
# Edit .env and set your JWT_SECRET
```

3. **Start the application**
```bash
docker-compose up -d
```
OR
```
DOCKER_BUILDKIT=0 docker compose up --build
```

4. **Access the application**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health Check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Grafana: http://localhost:3000 (default credentials: admin/admin)
- Prometheus: http://localhost:9090

### Option 2: Running Locally

1. **Start PostgreSQL**
```bash
docker run -d \
  --name movierating-postgres \
  -e POSTGRES_DB=movierating \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

2. **Set environment variables**
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=movierating
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=YourSuperSecretKeyHere
```

3. **Build and run**
```bash
mvn clean package
java -jar target/movie-rating-service-1.0.0.jar
```

##  API Documentation

### Authentication

#### Register
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!@"
}
```

#### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!@"
}
```

### Movies (Public)

#### List all movies
```bash
GET /api/v1/movies?page=0&size=20
```

#### Get movie by ID
```bash
GET /api/v1/movies/{movieId}
```

#### Get top-rated movie
```bash
GET /api/v1/movies/top-rated
```

### Ratings (Protected)

#### Create/Update rating
```bash
POST /api/v1/ratings
Authorization: Bearer {token}
Content-Type: application/json

{
  "movieId": "123e4567-e89b-12d3-a456-426614174000",
  "ratingValue": 9,
  "review": "Amazing movie!"
}
```

#### Get my ratings
```bash
GET /api/v1/ratings/my
Authorization: Bearer {token}
```

#### Delete rating
```bash
DELETE /api/v1/ratings/{ratingId}
Authorization: Bearer {token}
```

## 🧪 Testing

### Run all tests
```bash
mvn test
```

## 🗃️ Database Migrations

Liquibase manages database schema changes. Migrations are located in:
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── changes/
│   └── v1.0/
│       ├── 001-create-users-table.yaml
│       ├── 002-create-movies-table.yaml
│       ├── 003-create-ratings-table.yaml
│       ├── 004-add-indexes.yaml
│       └── 005-add-constraints.yaml
└── data/
    ├── seed-movies.yaml
    └── seed-test-users.yaml
```

## 🔒 Security

- Passwords are hashed using BCrypt (strength: 12)
- JWT tokens expire after 24 hours
- CORS is configured for specific origins
- SQL injection prevention via JPA/Hibernate
- Input validation on all endpoints

## 📊 Performance Optimizations

- Database indexes on frequently queried columns
- Connection pooling with HikariCP
- JPA batch operations enabled
- Query optimization for top-rated movie calculation
- Efficient N+1 query prevention

## 🐳 Docker Commands
```bash
# Build and start
docker-compose up -d # use docker compose

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild image
docker-compose build --no-cache app

# Clean up everything
docker-compose down -v
```

## 🔧 Configuration

Key configuration properties in `application.yml`:
```yaml
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000  # 24 hours

spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 📝 Test Credentials

Development/Test environment includes seeded users:
```
You can create your own credentials via the rest /api/v1/auth/register endpoint. you will get a JWT token to access protected endpoints.
```