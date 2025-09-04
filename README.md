# URL Shortening Service (Java + Spring Boot + MySQL)

Project URL: https://roadmap.sh/projects/realtime-leaderboard-system

A simple, clean RESTful API to create, retrieve, update, delete short URLs and fetch access statistics. Includes an optional redirect endpoint and a minimal frontend at `/`.

## Tech Stack
- Java 21, Spring Boot 3 (Web, Data JPA, Validation)
- MySQL (Homebrew)
- Lombok

## Prerequisites
- Java 21+
- MySQL running locally (Homebrew)
  - Check: `brew services list | grep -i mysql`

## Configuration
Default datasource values are set in `src/main/resources/application.properties`:
- `spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener?createDatabaseIfNotExist=true...`
- `spring.datasource.username=root`
- `spring.datasource.password=` (empty)

Override via environment variables (recommended):
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

## Build
```bash
mvn -DskipTests package
```

## Run
```bash
java -jar target/url-shortening-service-1.0-SNAPSHOT.jar
```
The app listens on `http://localhost:8080`.

## Minimal Frontend
Open `http://localhost:8080/` to create and fetch short URLs interactively.

## API Endpoints
- Create Short URL
  - POST `/shorten`
  - Body: `{ "url": "https://www.example.com/long" }`
  - 201 Created, returns JSON with id, url, shortCode, createdAt, updatedAt
- Retrieve Original URL (increments access count)
  - GET `/shorten/{shortCode}`
  - 200 OK
- Update Short URL
  - PUT `/shorten/{shortCode}`
  - Body: `{ "url": "https://www.example.com/updated" }`
  - 200 OK
- Delete Short URL
  - DELETE `/shorten/{shortCode}`
  - 204 No Content
- Get Statistics
  - GET `/shorten/{shortCode}/stats`
  - 200 OK, includes `accessCount`
- Optional Redirect
  - GET `/r/{shortCode}` → 302 Found to the original URL (increments access count)

## cURL Examples
```bash
# Create
curl -s -X POST http://localhost:8080/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/very/long/path"}' | jq .

# Get
curl -s http://localhost:8080/shorten/abc123 | jq .

# Update
curl -s -X PUT http://localhost:8080/shorten/abc123 \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/updated"}' | jq .

# Stats
curl -s http://localhost:8080/shorten/abc123/stats | jq .

# Delete
curl -i -X DELETE http://localhost:8080/shorten/abc123

# Redirect
curl -i http://localhost:8080/r/abc123
```

## Notes
- Short codes are generated randomly, length 6+, collision-checked, and lengthened on repeated collisions.
- `accessCount` is incremented on GET `/shorten/{shortCode}` and `/r/{shortCode}`; not on stats retrieval.
- Schema is auto-managed (`spring.jpa.hibernate.ddl-auto=update`).
