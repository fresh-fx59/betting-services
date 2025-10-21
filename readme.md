# Betting Services Application

A Spring Boot application for betting services with Kafka messaging and PostgreSQL database.

## Architecture

- **Spring Boot 3.4** - Application framework
- **Apache Kafka (KRaft mode)** - Message broker for event streaming
- **PostgreSQL 16** - Relational database
- **Java 21** - Programming language

## Prerequisites

- Docker and Docker Compose installed

## Quick Start

### Launch the Application

Start all services with one command:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Kafka in KRaft mode
- Kafka topic initialization (creates `jackpot-bets` topic)
- Kafka UI for monitoring
- Spring Boot application

### Verify Services

Check if all containers are running:

```bash
docker-compose ps
```

View logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f betting-app
```

### Access the Application

- **Application API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **Kafka UI**: http://localhost:8080

## API Usage

### Place a Bet

**Endpoint:** `POST /api/v1/bets`

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "jackpotId": 456,
    "betAmount": 50.00
  }'
```

**Response:**
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

### How Outbox Pattern Works

1. **API receives bet request** → Saves to `bets` table
2. **Same transaction** → Creates record in `outbox_events` table
3. **Scheduled job** (every 5 seconds) → Reads pending events from outbox
4. **Publishes to Kafka** → Sends message to `jackpot-bets` topic
5. **Marks as published** → Updates outbox status

This ensures **transactional consistency** - if database fails, message won't be sent to Kafka.

### Monitor Messages in Kafka

**Option 1: Using Kafka UI**
- Open http://localhost:8080
- Navigate to Topics → `jackpot-bets`
- View messages

**Option 2: Using Command Line**
```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic jackpot-bets \
  --from-beginning
```

## Common Commands

### Managing Services

```bash
# Stop all services
docker-compose down

# Stop and remove data
docker-compose down -v

# Restart application after code changes
docker-compose up -d --build betting-app

# View application logs
docker-compose logs -f betting-app
```

### Kafka Operations

```bash
# List topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9093 --list

# Create a topic
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9093 --create --topic betting-events --partitions 3 --replication-factor 1

# Console consumer (view messages)
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9093 --topic betting-events --from-beginning
```

### Database Access

```bash
# Access PostgreSQL shell
docker-compose exec postgres psql -U betting_user -d betting_db
```

**Connection Details:**
- Host: localhost
- Port: 5432
- Database: betting_db
- Username: betting_user
- Password: betting_password

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Application | 8081 | Spring Boot REST API |
| PostgreSQL | 5432 | Database |
| Kafka | 9092 | External connections |
| Kafka UI | 8080 | Web interface for Kafka |

## Troubleshooting

### Application won't start

Check logs:
```bash
docker-compose logs betting-app
```

### Clean restart

Remove all containers and data:
```bash
docker-compose down -v
docker-compose up -d
```

### Port conflicts

If ports are in use, modify them in `docker-compose.yml`

## Development

To run locally without Docker:

1. Start only infrastructure:
```bash
docker-compose up -d postgres kafka
```

2. Run application with Maven:
```bash
mvn spring-boot:run
```