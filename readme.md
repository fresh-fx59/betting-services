# Betting Services Application

A Spring Boot microservice for managing betting operations with jackpot contributions and rewards. The service uses the Transactional Outbox pattern for reliable event publishing to Kafka.

## Architecture

- **Spring Boot 3.4** - Application framework
- **Apache Kafka (KRaft mode)** - Message broker for event streaming
- **PostgreSQL 16** - Relational database
- **Java 21** - Programming language
- **Maven** - Build tool
- **Docker Compose** - Container orchestration

## Prerequisites

- Docker and Docker Compose installed
- (Optional) Java 21 and Maven for local development

## Quick Start

### 1. Launch the Application

Start all services with one command:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL database** - Stores bets, jackpots, contributions, rewards, and outbox events
- **Kafka in KRaft mode** - Message broker without Zookeeper dependency
- **Kafka topic initialization** - Automatically creates `jackpot-bets` topic
- **Kafka UI** - Web interface for monitoring Kafka topics and messages
- **Spring Boot application** - REST API service

### 2. Verify Services

Check if all containers are running:

```bash
docker-compose ps
```

All services should show "Up" status.

### 3. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f betting-app
```

### 4. Access the Application

- **Application API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **Kafka UI**: http://localhost:8080

---

## How It Works

### Complete Betting Flow

When a bet is placed, the following steps occur:

1. **Bet Placement** → Bet saved to database
2. **Outbox Event** → Event created in outbox table (same transaction)
3. **Jackpot Contribution** → Bet amount contributes to jackpot pool
4. **Reward Evaluation** → System checks if bet wins the jackpot
5. **Event Publishing** → Outbox publisher sends event to Kafka (every 5 seconds)

### Transactional Outbox Pattern

Ensures **transactional consistency** between database and Kafka:

1. **API receives bet request** → Saves to `bets` table
2. **Same transaction** → Creates record in `outbox_events` table with status `PENDING`
3. **Scheduled job** (every 5 seconds) → Reads pending events from outbox
4. **Publishes to Kafka** → Sends message to `jackpot-bets` topic
5. **Marks as published** → Updates outbox status to `PUBLISHED`

If the database transaction fails, no message is sent to Kafka, maintaining data consistency.

### Jackpot Contribution

Each bet contributes to the jackpot pool based on the jackpot's contribution type:

**1. FIXED Contribution** (10% of bet amount)
- Simple fixed percentage of the bet amount
- Example: $100 bet → $10 contribution

**2. PERCENTAGE Contribution** (Variable, starts at 20%, decays over time)
- Starts high, decreases as jackpot grows
- Formula: `initialPercentage - (jackpotGrowth * decayRate)`
- Never goes below fixed percentage (10%)
- Example: Early jackpot → 20% contribution, Full jackpot → 10% contribution

### Jackpot Reward

System evaluates if a bet wins the jackpot based on reward type:

**1. FIXED Reward** (5% chance)
- Fixed probability for each bet
- Random evaluation: 5% chance to win

**2. VARIABLE Reward** (Starts at 1%, grows to 100%)
- Chance increases as jackpot pool grows
- When jackpot reaches max value → 100% chance
- Formula: `initialChance + (growthPercentage * growthRate)`
- Ensures jackpot is eventually won

**When a jackpot is won:**
- Reward record created with current jackpot amount
- Jackpot pool reset to initial value
- User receives the full jackpot amount

---

## API Endpoints

### 1. Place a Bet

Creates a new bet, contributes to jackpot, and evaluates for reward.

**Endpoint:** `POST /api/v1/bets`

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "jackpotId": 1,
    "betAmount": 100.00
  }'
```

**Request Body:**
```json
{
  "userId": 123,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Success Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "userId": 123,
    "jackpotId": 1,
    "betAmount": 100.00,
    "createdAt": "2025-10-25T14:30:00"
  },
  "message": "Bet placed successfully",
  "success": true
}
```

**What Happens:**
1. Bet saved to database
2. Outbox event created (will be published to Kafka)
3. Jackpot contribution calculated and jackpot pool updated
4. Reward evaluation performed (may win jackpot)

---

### 2. Get Jackpot Reward

Retrieves a jackpot reward record by ID.

**Endpoint:** `GET /api/v1/jackpot-rewards/{id}`

**Request:**
```bash
curl http://localhost:8081/api/v1/jackpot-rewards/1
```

**Success Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "betId": 42,
    "userId": 123,
    "jackpotId": 1,
    "jackpotRewardAmount": 50000.00,
    "createdAt": "2025-10-25T14:35:22"
  },
  "message": "Jackpot reward found",
  "success": true
}
```

**Not Found Response (404):**
```
(Empty body with 404 status code)
```

---

### 3. Health Check

Check if the application is running.

**Endpoint:** `GET /actuator/health`

**Request:**
```bash
curl http://localhost:8081/actuator/health
```

**Success Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## Configuration

### Jackpot Contribution Settings

Located in `src/main/resources/application.properties`:

```properties
# Fixed contribution: 10% of bet amount
jackpot.contribution.fixed.percentage=10.0

# Variable contribution: starts at 20%, decays by 0.1% per unit growth
jackpot.contribution.variable.initial-percentage=20.0
jackpot.contribution.variable.decay-rate=0.1
```

### Jackpot Reward Settings

```properties
# Fixed reward: 5% chance to win
jackpot.reward.fixed.chance=5.0

# Variable reward: starts at 1%, grows by 0.5% per unit growth
jackpot.reward.variable.initial-chance=1.0
jackpot.reward.variable.growth-rate=0.5
```

### Kafka Settings

```properties
# Kafka topic for bet events
kafka.topic.jackpot-bets=jackpot-bets

# Kafka connection (internal Docker network)
spring.kafka.bootstrap-servers=kafka:9093
```

---

## Monitoring Kafka Messages

### Option 1: Using Kafka UI (Recommended)

1. Open http://localhost:8080
2. Navigate to **Topics** → `jackpot-bets`
3. View messages in real-time

### Option 2: Using Command Line

```bash
# Consume messages from beginning
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic jackpot-bets \
  --from-beginning

# Consume only new messages
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic jackpot-bets
```

**Example Message:**
```json
{
  "betId": 1,
  "userId": 123,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

---

## Database Schema

### Tables

1. **bets** - Stores all placed bets
2. **jackpots** - Jackpot configurations and current values
3. **jackpot_contributions** - Records of bet contributions to jackpots
4. **jackpot_rewards** - Records of jackpot wins
5. **outbox_events** - Transactional outbox for Kafka events

### Access Database

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U betting_user -d betting_db

# View tables
\dt

# Query bets
SELECT * FROM bets;

# Query jackpot rewards
SELECT * FROM jackpot_rewards;

# Exit
\q
```

**Connection Details:**
- **Host**: localhost
- **Port**: 5432
- **Database**: betting_db
- **Username**: betting_user
- **Password**: betting_password

---

## Common Commands

### Managing Services

```bash
# Stop all services
docker-compose down

# Stop and remove all data (WARNING: deletes database)
docker-compose down -v

# Restart application after code changes
docker-compose up -d --build betting-app

# View application logs
docker-compose logs -f betting-app

# Check service status
docker-compose ps
```

### Kafka Operations

```bash
# List all topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9093 --list

# Create a new topic
docker-compose exec kafka kafka-topics \
  --bootstrap-server localhost:9093 \
  --create --topic my-topic \
  --partitions 3 \
  --replication-factor 1

# Describe topic
docker-compose exec kafka kafka-topics \
  --bootstrap-server localhost:9093 \
  --describe --topic jackpot-bets
```

---

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Application | 8081 | Spring Boot REST API |
| PostgreSQL | 5432 | Database |
| Kafka | 9092 | External connections |
| Kafka UI | 8080 | Web interface |

---

## Development

### Run Locally Without Docker

1. **Start only infrastructure:**
```bash
docker-compose up -d postgres kafka kafka-init
```

2. **Run application with Maven:**
```bash
./mvnw spring-boot:run
```

3. **Build the project:**
```bash
./mvnw clean package
```

4. **Run tests:**
```bash
./mvnw test
```

---

## Troubleshooting

### Application won't start

Check logs:
```bash
docker-compose logs betting-app
```

Look for errors related to database connection or Kafka.

### Clean restart

Remove all containers and data:
```bash
docker-compose down -v
docker-compose up -d
```

This will recreate the database from scratch.

### Port conflicts

If ports 8080, 8081, 5432, or 9092 are in use, modify them in `docker-compose.yml`.

### Database schema issues

If you see errors about missing tables:
```bash
# Check application.properties for:
spring.jpa.hibernate.ddl-auto=update

# Or manually create tables by connecting to database
docker-compose exec postgres psql -U betting_user -d betting_db
```

---

## Example Workflow

### Complete User Journey

1. **Create a jackpot** (via database or API - not shown in current endpoints)
   - Set initial value: $10,000
   - Set max value: $100,000
   - Set contribution type: VARIABLE
   - Set reward type: VARIABLE

2. **User places a bet:**
```bash
curl -X POST http://localhost:8081/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "jackpotId": 1, "betAmount": 100.00}'
```

3. **System processes:**
   - Saves bet to database
   - Creates outbox event
   - Calculates contribution (e.g., 20% of $100 = $20)
   - Updates jackpot pool ($10,000 → $10,020)
   - Evaluates reward (1% chance at this point)
   - Returns bet confirmation

4. **Background process (every 5 seconds):**
   - Reads pending outbox events
   - Publishes to Kafka topic `jackpot-bets`
   - Marks events as published

5. **If user wins jackpot:**
```bash
# Check reward
curl http://localhost:8081/api/v1/jackpot-rewards/1
```

Response shows user won $10,020, and jackpot is reset to $10,000.

---

## Architecture Highlights

- **Strategy Pattern** - Contribution and reward logic extensible via strategies
- **Transactional Outbox** - Ensures exactly-once message delivery
- **Event-Driven** - Kafka integration for downstream processing
- **Clean Architecture** - Separation of concerns (Controller → Service → Repository)
- **Configuration-Driven** - All percentages configurable via properties
- **Type Safety** - Enums for contribution and reward types

---

## License

This project is for educational purposes.
