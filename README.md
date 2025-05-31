# SMS API Service

This project is a Java-based backend service for handling and storing SMS messages. It splits long messages into
multiple parts with customizable suffixes and handles storage with robust error handling.

## How to Run:

```bat
./mvnw spring-boot:run
```

## 📦 Features

- Accepts SMS requests via DTO (`SmsRequestDto`)
- SMS behavior is configurable via `SmsConfiguration`:
    - Custom suffix template for multi-part messages
    - Maximum SMS length per message part
- Automatically splits messages that exceed the max SMS length
- Adds a custom suffix (e.g., `... - Part 1 of 3`) to multi-part messages
- Saves each message information into a database for history tracking
- Unit-tested with JUnit 5 and Mockito
- Exception handling for database errors (`DatabaseTransactionException`)
- Database Initiation with
- Database Audit
- Spring Boot Actuator integration for health and metrics endpoints:
    - `/actuator/health`: Basic health status
    - `/actuator/info`: Application metadata
    - `/actuator/metrics`: JVM, memory, and HTTP request metrics

## 🧾 Technologies

- Java 21+
- Spring Boot
- JUnit 5
- Mockito
- Maven

## ⚙️ Configuration

`SmsConfigurationService` provides runtime configuration for:

- `getMaxSmsLength()`: Max character length per SMS (e.g., 160)
- `getSuffixTemplate()`: Format for message suffixes (e.g., `"... - Part %d of %d"`)

## 🧪 Testing

### Prerequisites

Make sure your environment has:

- JDK 21+
- Maven
- IDE or terminal setup for JUnit 5

### Important Notes

- The test class `SmsHandlerServiceImplTest` uses reflection to test private methods like `processMessage()` and
  `generateMessages()`.
- Avoid referencing non-static fields in static methods (e.g., `smsConfigurationService` inside `messageProvider()`), as
  this will cause compilation issues. Refactor the provider to not use instance-specific values or inject configuration
  via test method arguments.

## 💥 Known Issues

- `Non-static field 'smsConfigurationService' cannot be referenced from a static context`  
  **Solution:** Avoid using `smsConfigurationService` inside static test methods like `messageProvider()`. Instead,
  hard-code values or move logic inside the test body.

## 🛠 Example Usage

### SMS Input

```json
{
  "from": "Service",
  "to": "+1234567890",
  "message": "Hello, this is a test message that might need to be split."
}
```

## 🧠 Project Q&A

### 1. Architecture Decisions

The architecture was designed with **clean separation of concerns** in mind:

- The `SmsHandlerServiceImpl` class is responsible for orchestration, without directly managing persistence logic.
- The `SmsConfigurationService` abstracts configuration so it can be reused or swapped (e.g., for dynamic runtime
  configs).
- Data is transferred using DTOs to decouple internal logic from external requests.
- Exception handling is centralized via custom exceptions like `DatabaseTransactionException` to avoid leaking
  implementation details.

This modular structure helps improve **testability, reusability, and clarity**, especially important for
production-grade services.

---

### 2. API Design

The API was designed to be **simple and extensible**:

- Input: Uses a `SmsRequestDto` with `from`, `to`, and `message` fields.
- Output: Designed to clearly reflect how the message was split and processed.

**Versioning and error formats** were not implemented in this iteration due to time constraints, but:

- Future versions could prefix endpoints with `/v1/sms`, `/v2/sms`, etc.
- Errors should ideally be standardized in a format like:
  ```json
  {
    "timestamp": "2025-05-30T12:00:00Z",
    "error": "Bad Request",
    "message": "Message content cannot be empty",
    "path": "/sms"
  }
  ```

### 3. State Management (If implemented)

No front-end was implemented.

### 4. Security

Token-based login/auth was **not implemented**, but I did configure **basic authentication** for production using
Spring Security:

- In `prod` profile:
    - HTTP Basic Auth is enabled using in-memory credentials.
    - Credentials (`SMS_API_USER`, `SMS_API_PASSWORD`, and `SMS_API_ROLES`) are injected from environment variables.
    - Passwords are encoded using `BCryptPasswordEncoder` for secure storage.
    - Only specific endpoints (`/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`) are publicly accessible; all others
      require authentication.

- In `dev` profile:
    - All endpoints are accessible without authentication to ease local development.

This setup allows for quick deployment with **secure role-based access** in production while preserving developer
agility in non-prod environments.

**Next steps for production-hardening:**

- Replace Basic Auth with **JWT-based authentication**.
- Store credentials in a secure **vault or secret manager**.
- Implement **role-based authorization annotations** on controller methods.
- Add audit logging for authentication/authorization events.

### 5. Scalability & Maintainability

If the project were to scale to support teams and thousands of users, the following architectural and operational
improvements would be necessary:

#### 🧱 Architectural Changes

- **Authentication**: Replace basic authentication with a robust solution like **JWT** or **OAuth2**, possibly with
  integration to an Identity Provider (e.g., Keycloak, Auth0).
- **Database**: Move from in-memory or static configurations to a **persistent relational database** (e.g., PostgreSQL)
  or **scalable NoSQL store** depending on the data access pattern.
- **Asynchronous Processing**: Introduce **message queues** (e.g., RabbitMQ, Kafka) for decoupled and reliable handling
  of high-volume tasks like SMS sending.
- **Horizontal Scalability**: Deploy as stateless microservices behind a **load balancer** (e.g., Kubernetes + Ingress)
  for better horizontal scaling and fault tolerance.

#### 🧪 Code & Design Improvements

- **Modularization**: Further separate concerns by splitting into dedicated modules or services (e.g., AuthService,
  SmsService, AuditService).
- **Interface Contracts**: Use **OpenAPI** specifications to generate consistent client/server interfaces and ensure
  compatibility as the API evolves.
- **Error Handling**: Introduce a global error handler with a **standard error response structure** (e.g., timestamp,
  error code, message, trace ID).

#### 🔧 Operational Improvements

- **CI/CD**: Implement continuous delivery pipelines (e.g., GitHub Actions, Jenkins) with automated tests, builds, and
  deployments.
- **Observability**: Add metrics, structured logs, and distributed tracing (e.g., Spring Boot Actuator +
  Prometheus/Grafana + OpenTelemetry).
- **Secrets Management**: Use tools like **HashiCorp Vault** or **AWS Secrets Manager** to manage sensitive
  configurations securely.

#### 🧹 Maintainability

- **Documentation**: Maintain up-to-date developer and user documentation, including API docs and onboarding guides.
- **Testing**: Expand test coverage with **integration and end-to-end tests**, and use **Testcontainers** for isolated
  environments.
- **Code Quality**: Enforce code quality and consistency using tools like **SonarQube**, **Checkstyle**, and **SpotBugs
  **.

These changes would ensure the system remains performant, secure, and easy to develop as it grows.

### 6. Time Constraints

Due to time constraints, several features and enhancements were intentionally skipped or left as future improvements:

#### 🔐 Authentication Enhancements

- **JWT-based authentication** and refresh token logic were not implemented. Instead, basic HTTP authentication with
  in-memory user configuration was used.
- No user registration or role-based access control mechanisms beyond the default roles setup via environment variables.

#### ✉️ SMS Sending Logic

- Actual integration with an **SMS gateway provider** (like Twilio, Nexmo, etc.) was mocked or skipped. The
  implementation focuses on the API design and service structure rather than real SMS delivery.

#### 🧪 Testing

- Only minimal unit tests were written, and integration or end-to-end tests were not implemented.
- Code coverage not completed.

#### 📈 Observability and Monitoring

- No centralized logging, monitoring, or alerting systems (like Prometheus/Grafana, ELK stack) were added.
- No traceability or request correlation implemented for debugging in production.

#### 🗃️ Persistence and Data Store

- No persistent storage layer or database integration. All configurations and credentials are handled in memory or via
  environment variables.

#### 📦 API Versioning

- API versioning (e.g., `/v1/sms`) was not added. This would be essential in a production-grade API to support backward
  compatibility.

These trade-offs allowed focus on delivering a clean, working prototype with extensibility in mind while staying within
limited time.
