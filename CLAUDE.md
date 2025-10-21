# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.4 microservice for handling betting operations with event-driven architecture. The service accepts bets via REST API and uses the Transactional Outbox pattern to reliably publish events to Kafka.

**Key Technologies:**
- Java 21
- Spring Boot 3.4.11-SNAPSHOT
- Spring Data JPA with PostgreSQL
- Spring Kafka
- Maven
- Docker & Docker Compose

## Architecture

### Transactional Outbox Pattern

This service implements the Transactional Outbox pattern for reliable event publishing:

1. **BetService** (`BetServiceImpl.java:27-77`) handles bet placement in a single transaction:
   - Saves the `Bet` entity to the `bets` table
   - Saves an `OutboxEvent` entity to the `outbox_events` table with status `PENDING`
   - Both operations commit atomically

2. **OutboxEvent** (`OutboxEvent.java`) tracks events to be published to Kafka:
   - Status lifecycle: `PENDING` → `PUBLISHED` or `FAILED`
   - Includes retry tracking (max 3 retries) and error messages
   - Query method (`findPendingEvents`) retrieves PENDING events and FAILED events below retry limit

3. **OutboxPublisher** (`OutboxPublisher.java:27-81`) is a scheduled service that:
   - Runs every 5 seconds (`@Scheduled(fixedDelay = 5000)`)
   - Polls for pending/retryable events via `OutboxEventRepository.findPendingEvents()`
   - Publishes each event to Kafka topic `jackpot-bets` using `KafkaTemplate`
   - Updates status to `PUBLISHED` on success with timestamp
   - Increments `retryCount` on failure, marks as `FAILED` after 3 retries
   - Uses asynchronous Kafka sends with `whenComplete()` callback

### Package Structure

- `controller/` - REST API endpoints (`BetController` at `/api/v1/bets`)
- `service/` - Business logic (bet placement with outbox)
- `repository/` - JPA repositories for database access
- `entity/` - JPA entities (`Bet`, `OutboxEvent`)
- `model/` - DTOs for API contracts (`BetRequest`, `BetResponse`, `BetMessage`)
- `config/` - Spring configuration (`AppConfig` with scheduling enabled)

## Development Commands

### Build and Test

```bash
# Build the project
./mvnw clean package

# Build without running tests
./mvnw clean package -DskipTests

# Run tests only
./mvnw test

# Run a single test class
./mvnw test -Dtest=BettingServicesApplicationTests
```

### Running Locally

**Option 1: Full Docker environment (recommended for quick start)**
```bash
# Start all services (PostgreSQL, Kafka, Kafka UI, and the app)
docker-compose up -d

# View logs
docker-compose logs -f betting-app
```

**Option 2: Local development with Docker infrastructure**
```bash
# Start only infrastructure (PostgreSQL + Kafka)
docker-compose up -d postgres kafka kafka-init

# Run the Spring Boot application with Maven
./mvnw spring-boot:run
```

### Docker Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes data)
docker-compose down -v

# Rebuild and restart app after code changes
docker-compose up -d --build betting-app

# Check service status
docker-compose ps

# View logs for all services
docker-compose logs -f
```

### Useful URLs

- **Application API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **Kafka UI**: http://localhost:8080

### Kafka Operations

```bash
# List topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9093 --list

# Create a new topic
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9093 \
  --create --topic new-topic --partitions 3 --replication-factor 1

# Consume messages from jackpot-bets topic
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic jackpot-bets \
  --from-beginning
```

### Database Operations

```bash
# Access PostgreSQL shell
docker-compose exec postgres psql -U betting_user -d betting_db

# Connection details for external tools
# Host: localhost, Port: 5432
# Database: betting_db
# Username: betting_user
# Password: betting_password
```

## API Usage

### Place a Bet

**Endpoint:** `POST /api/v1/bets`

**Example Request:**
```bash
curl -X POST http://localhost:8081/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "jackpotId": 456,
    "betAmount": 50.00
  }'
```

**Example Response:**
```json
{
  "betId": 1,
  "userId": 123,
  "jackpotId": 456,
  "betAmount": 50.00,
  "createdAt": "2025-10-21T20:30:00",
  "message": "Bet placed successfully"
}
```

**Data Flow:**
1. API receives bet request → Saves to `bets` table
2. Same transaction → Creates record in `outbox_events` table with status `PENDING`
3. Scheduled job (every 5s) → Reads pending events from outbox
4. Publishes to Kafka → Sends message to `jackpot-bets` topic
5. Marks as published → Updates outbox status to `PUBLISHED`

## Database Configuration

The service uses PostgreSQL with the following defaults (configurable via environment variables):

- Database: `betting_db`
- User: `betting_user`
- Password: `betting_password`
- Port: 5432

JPA is configured with `spring.jpa.hibernate.ddl-auto=update`, so schema changes are applied automatically on startup.

## Kafka Configuration

- Topic: `jackpot-bets` (3 partitions, created automatically by `kafka-init` service)
- Bootstrap servers: `localhost:9092` (external) or `kafka:9093` (internal)
- Producer acks: `all` (ensures durability)
- Retries: 3
- Kafka runs in KRaft mode (without Zookeeper)

## Testing

The test structure follows standard Spring Boot conventions:
- Test sources in `src/test/java/`
- Use `@SpringBootTest` for integration tests
- `spring-kafka-test` is available for Kafka testing

## Important Notes

- Package name: `com.betting.betting_services` (note: underscore, not hyphen)
- Server port: 8081 (not the default 8080, which is used by Kafka UI)
- Lombok is used extensively for reducing boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- All timestamps use `LocalDateTime` with `@PrePersist` hooks
- ObjectMapper is configured with JavaTimeModule for proper date/time serialization
