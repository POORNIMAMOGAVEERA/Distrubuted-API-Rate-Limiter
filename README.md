# Distributed Rate Limiter

A production-oriented distributed rate limiting platform built using Java, Spring Boot, Redis, Prometheus, Grafana, and Docker.

The system supports multiple rate limiting algorithms, distributed enforcement through Redis, atomic request evaluation using Lua scripts, dynamic policy management, observability, alerting, and horizontal scaling.

---

## Features

### Rate Limiting Algorithms

* Fixed Window
* Sliding Window Counter
* Token Bucket

### Distributed Enforcement

* Shared rate limit state using Redis
* Multi-instance support
* Consistent enforcement across application nodes
* Atomic request evaluation using Redis Lua scripts

### Dynamic Policy Management

Supports configurable rate limits based on:

* User
* API
* IP Address
* Tier

Example:

| Tier    | Limit             |
| ------- | ----------------- |
| Free    | 100 requests/min  |
| Premium | 1000 requests/min |

### Observability

Integrated with:

* Micrometer
* Prometheus
* Grafana

Metrics:

* requests_total
* requests_allowed_total
* requests_blocked_total
* rate_limit_decision_latency
* redis_latency

### Alerting

Implemented using Prometheus AlertManager.

Alerts:

* High rejection rate
* High Redis latency
* High decision latency
* Service unavailable

### Load Testing

Implemented using k6.

Benchmarks executed at:

* 1K RPS
* 10K RPS
* 50K RPS (best effort)

### Horizontal Scaling

Supports:

* Multiple Spring Boot instances
* Shared Redis state
* Nginx load balancing
* Consistent distributed enforcement

---

## Architecture

```text
                    ┌─────────────┐
                    │     k6      │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    Nginx    │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
 ┌────────────┐     ┌────────────┐     ┌────────────┐
 │ App Node 1 │     │ App Node 2 │     │ App Node 3 │
 └──────┬─────┘     └──────┬─────┘     └──────┬─────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
                    ┌─────────────┐
                    │    Redis    │
                    └─────────────┘

                           │
                           ▼
                    ┌─────────────┐
                    │ Prometheus  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Grafana   │
                    └─────────────┘
```

---

## Technology Stack

### Backend

* Java 21
* Spring Boot

### Storage

* Redis

### Monitoring

* Micrometer
* Prometheus
* Grafana

### Alerting

* AlertManager

### Testing

* JUnit 5
* Testcontainers

### Performance Testing

* k6

### Infrastructure

* Docker
* Docker Compose
* Nginx

---

## Project Structure

```text
distributed-rate-limiter
│
├── src
│   ├── main
│   └── test
│
├── monitoring
│   ├── prometheus
│   ├── grafana
│   └── alertmanager
│
├── load-tests
│
├── nginx
│
├── docs
│
├── Dockerfile
│
└── docker-compose-distributed.yml
```

---

## Running Locally

### Start Redis

```bash
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7
```

### Start Application

```bash
mvn spring-boot:run
```

### Test Endpoint

```bash
curl "http://localhost:8080/lua-limit?key=user1"
```

---

## Monitoring Stack

Start:

```bash
cd monitoring

docker compose up -d
```

Services:

| Component    | URL                   |
| ------------ | --------------------- |
| Prometheus   | http://localhost:9090 |
| Grafana      | http://localhost:3000 |
| AlertManager | http://localhost:9093 |

---

## Distributed Deployment

Build application image:

```bash
mvn clean package

docker build -t distributed-rate-limiter .
```

Start cluster:

```bash
docker compose -f docker-compose-distributed.yml up -d
```

Components:

* Nginx
* App Node 1
* App Node 2
* App Node 3
* Redis

Load-balanced endpoint:

```bash
http://localhost:8081/lua-limit?key=user1
```

---

## Load Testing

Run:

```bash
k6 run load-tests/1k-rps.js
```

or

```bash
k6 run load-tests/10k-rps.js
```

or

```bash
k6 run load-tests/50k-rps.js
```

---

## Engineering Decisions

### Why Redis?

Redis provides:

* Fast in-memory access
* Shared distributed state
* Atomic operations
* Lua script support

### Why Lua Scripts?

Without Lua:

```text
GET
CHECK
INCR
```

Multiple requests may violate limits under concurrency.

Lua enables:

```text
Read + Evaluate + Update
```

as a single atomic operation.

### Why Token Bucket?

Allows traffic bursts while maintaining average request rates.

Useful for:

* API gateways
* Public APIs
* Microservices

### Why Prometheus + Grafana?

Provides:

* Metrics collection
* Real-time monitoring
* Operational visibility
* Alerting support

---

## Future Improvements

* Redis Cluster
* Multi-region deployment
* Adaptive rate limiting
* Rate limit caching layer
* Kubernetes deployment
* OpenTelemetry tracing
* Circuit breaker integration

---

## Resume Highlights

* Designed and implemented a distributed rate limiting platform using Java, Spring Boot, Redis, and Docker supporting horizontal scalability and configurable traffic policies.
* Built atomic rate limiting using Redis Lua scripts to ensure consistency under concurrent load.
* Implemented observability using Micrometer, Prometheus, Grafana, and AlertManager with latency, throughput, and rejection-rate monitoring.
* Performed load testing with k6 and analyzed bottlenecks under high request volumes.
