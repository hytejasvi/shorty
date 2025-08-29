# Shorty - URL Shortener Service

## Project Description
Shorty is a minimalistic URL shortening service. It allows users to convert long URLs into short, easy-to-share URLs, and provides redirection and lookup capabilities for short URLs.

## What We Do
- Accept a long URL and generate a unique short code.
- Redirect requests to the original URL using the short code.
- Provide an endpoint to retrieve the original URL without redirecting.

## How We Do
- **Architecture:** Spring Boot application with layered architecture:
  - Controller layer (`UrlController`) for REST endpoints.
  - Service layer (`UrlService`) for business logic.
  - Repository/Port layer for persistence abstraction.
- **Design Choices:**
  - Base62 encoding with a configurable minimum short code length.
  - URL normalization and validation before persisting.
  - H2 in-memory database for easy testing and development.
  - Global exception handling via `@RestControllerAdvice`.
  - Unit and integration tests for core functionality.
  - Configurable `baseUrl` and minimum short code length.

## Build & Run Instructions

### Build Project
```bash
./gradlew build

./gradlew bootRun

./gradlew test
```

---

## API Endpoints

### 1️⃣ Shorten URL
**POST** `/shorten`

**Request Body:**
```json

{
  "originalUrl": "https://www.example.com"
}
```
**Response:**

```json

{
  "originalUrl": "https://www.example.com",
  "shortUrl": "http://localhost:8080/abc123"
}
```

### 2️⃣ Redirect to Original URL
**GET**  `/{shortUrlCode}`

**Response:**
```
HTTP 302 redirect to the original URL.
```

### 3️⃣ Get Original URL
**GET**  `/original/{shortUrlCode}`

**Response:**
```
"https://www.example.com"
```


** HTTP 200 OK with original URL in the body. **

---

## Configurations

```
| Property             | Description                               | Default Value          |
|----------------------|-------------------------------------------|-----------------------|
| `baseUrl`            | Base URL for generated short URLs         | http://localhost:8080 |
| `minShortCodeLength` | Minimum length for generated short codes  | 6                     |

```

## Testing

- **Service Unit Tests:** `UrlServiceTest.kt` covers happy paths, existing URLs, and repository errors.
- **Integration Tests:** `UrlControllerIntegrationTest.kt` covers end-to-end scenarios including validation and exception handling.


## Future Improvements 

- Persist URLs in a production-ready database like PostgreSQL.
- Functional programming improvements (Arrow) for better error handling and return types.
- Implement user authentication and URL ownership.
- Add metrics and monitoring for short URL usage.
- Introduce a caching layer for faster lookups.
- Add Swagger/OpenAPI documentation for the APIs.

## Scalability Considerations

While this project is designed at personal level, several areas can be improved for scalability in real-world deployments:

### 1. Database Writes
Currently, short codes are generated using the auto-incremented database ID (`GenerationType.IDENTITY`). This requires:
- 1 read/insert to generate the ID
- 1 update to persist the short code  

This approach works for a personal project but introduces unnecessary database writes, which can become a bottleneck at scale.  

**Possible improvements:**
- **UUIDs/ULIDs:** Generate identifiers at the application layer and encode them directly into short codes, eliminating the second write.
- **Distributed ID Generators:** Use external ID services (e.g., Snowflake IDs, Redis atomic counters, PostgreSQL sequences) for globally unique IDs in one step.
- **Pre-generated Short Codes:** Maintain an in-memory or Redis-backed pool of available short codes to assign instantly during persistence.

---

### 2. Database Scalability
- **MVP:** H2 in-memory DB for fast testing.  
- **Future:** Replace with PostgreSQL or another production-ready DB. Partitioning/sharding could be considered if the dataset grows to billions of URLs.  

---

### 3. Caching Layer
Most frequent operation will be URL lookup (`GET /{shortUrlCode}`).  
- **MVP:** Direct DB lookup.  
- **Future:** Add a Redis or in-memory cache to serve hot URLs quickly and reduce DB load.

---

### 4. Horizontal Scaling
The stateless nature of the service (short code generation + redirect) allows multiple instances to run behind a load balancer.  
- Short code uniqueness must be guaranteed across instances (via distributed ID generator or DB constraint).  
- Cache invalidation strategies will be required in distributed setups.

---

### 5. Monitoring & Metrics
- Track most requested short codes and redirection latencies.  
- Monitor DB query times and error rates.  
- Add distributed tracing (e.g., OpenTelemetry) for debugging bottlenecks.
