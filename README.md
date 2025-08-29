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

