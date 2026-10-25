# Kouah AI Board — Spring Boot microservices

Agile project-management platform (projects, boards, tasks, invitations, notifications) built as a Spring Boot 3 API plus two Node.js services, all started from one Docker Compose file. It is the second version of [Kouah-AI-Board](https://github.com/ABDO2121k/Kouah-AI-Board), which was first written in Jakarta EE.

## Architecture

```
                 Client (web)
                      │ REST + WebSocket
                      ▼
        ┌───────────────────────────────┐
        │  spring-app — Spring Boot 3   │
        │  JWT (stateless), RBAC,       │
        │  Redis cache, OpenAPI docs    │
        └──┬──────────┬─────────┬───────┘
           │ AMQP     │ gRPC    │ JDBC / S3
           ▼          ▼         ▼
     RabbitMQ   email-service   MySQL, MinIO (images)
           │    Node.js + MongoDB
           ▼    (private network)
  notification-service
  Node.js WebSockets
```

| Container | Role |
|---|---|
| `spring-app` | REST API: auth, RBAC, projects, tasks, invitations |
| `mysql` | Main database (Spring Data JPA) |
| `redis` | Cache (Spring Cache) |
| `minio` + `minio-setup` | Image storage |
| `rabbitmq` | Domain events from the API |
| `notification-service` | Consumes RabbitMQ events and pushes them to clients over WebSockets |
| `email-service` | Private gRPC service that sends e-mails, with MongoDB logs |
| `mongodb` | Storage for the e-mail service |

## Stack

Java 17, Spring Boot 3 (Web, Security, Data JPA, AMQP, Cache, Validation), JJWT, springdoc-openapi, gRPC + Protobuf, Node.js, RabbitMQ, Redis, MySQL, MongoDB, MinIO, Docker Compose.

## Run

```bash
docker compose up --build
```

Windows helpers: `start-all.bat`, `start-infrastructure-only.bat`, `stop-all.bat`, `view-logs.bat`.

## Design docs

UML (class, use-case and workflow diagrams), RBAC rules and code architecture are in [`project-spring-boot/conception/`](project-spring-boot/conception/).
