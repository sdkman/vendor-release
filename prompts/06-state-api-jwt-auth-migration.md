# Migrate State API Integration from Basic Auth to JWT Authentication

The State API is moving from HTTP Basic Auth to a simple OAuth-style JWT flow. A new `/login` endpoint accepts email/password credentials and issues a short-lived (10 minute) JWT. Subsequent mutating operations (POST, DELETE, etc.) require this JWT as a Bearer token in the Authorization header. The vendor-release service must migrate its `HttpStateApiClient` to authenticate via this new flow, caching the JWT for reuse and re-authenticating reactively on 401 responses.

## Requirements

- The State API client MUST authenticate via `POST /login` with JSON `{"email": "...", "password": "..."}` to obtain a JWT
- The JWT MUST be sent as `Authorization: Bearer <token>` on all State API requests (replacing Basic Auth)
- The JWT MUST be cached in memory and reused across requests until a 401 is received
- On receiving a 401 response, the client MUST re-authenticate (call `/login`), obtain a fresh JWT, and retry the original request exactly once
- If the retry also fails with 401, the error MUST be logged and the failure handled per existing fire-and-forget semantics (release still succeeds)
- Configuration MUST rename `STATE_API_USERNAME` to `STATE_API_EMAIL` (with `STATE_API_PASSWORD` kept as-is)
- The `application.conf` section `state-api.basic-auth` MUST be replaced with `state-api.auth` containing `email` and `password` keys
- Configuration.scala MUST expose `stateApiEmail` and `stateApiPassword` (replacing `stateApiBasicAuthUsername` and `stateApiBasicAuthPassword`)
- All existing non-auth behaviour MUST be preserved: Java filtering, dual-write flow, fire-and-forget error handling, platform/distribution mapping, checksum extraction
- The `/login` rate limit (429) MUST be logged as an error but not crash the service

## Rules

- rules/scala-rules.md
- rules/ddd-rules.md

## Domain

The auth flow introduces a stateful token cache into the otherwise stateless HTTP client:

```scala
// Token lifecycle
case class AuthToken(jwt: String, obtainedAt: Instant)

// Login request/response
case class LoginRequest(email: String, password: String)
case class LoginResponse(token: String)

// Auth flow (pseudocode)
def authenticatedRequest(request: HttpRequest): Future[HttpResponse] = {
  val token = cachedToken.getOrElse(login())
  val response = execute(request.withBearer(token))
  if (response.status == 401) {
    val freshToken = login()
    execute(request.withBearer(freshToken))  // single retry
  } else response
}
```

Key domain changes:
- **AuthToken**: Cached JWT with timestamp for potential future proactive refresh
- **Login**: `POST /login` with `{"email": "...", "password": "..."}` → `{"token": "eyJ..."}`
- **Bearer Auth**: `Authorization: Bearer <jwt>` replaces `Authorization: Basic <base64>`
- **Retry on 401**: Single re-auth + retry cycle per request

## Extra Considerations

- **Thread Safety**: The cached token may be accessed from multiple concurrent requests. Use an `AtomicReference[Option[String]]` or similar thread-safe container for the cached JWT
- **No Proactive Refresh**: We chose reactive 401-based re-auth over proactive TTL-based refresh for simplicity. The token's 10-minute TTL is generous for our request volume
- **Rate Limiting**: The `/login` endpoint has a 5 attempts/minute rate limit. Excessive 401 retries could hit this. Log 429 responses clearly so ops can investigate credential issues
- **Fire-and-Forget Preserved**: Auth failures (401 after retry, 429, 500 from login) are caught and logged but never fail the version release. The existing `recoverWith` in `conditionalStateApiPropagation` handles this
- **Backwards Compatibility**: The scalaj-http library supports setting arbitrary headers, so switching from `.auth()` to `.header("Authorization", "Bearer ...")` is straightforward
- **Login Failure Modes**: `POST /login` can return 401 (bad credentials), 429 (rate limited), or 500 (token creation failed). All should be logged with distinct messages
- **Invalidate on 401**: When a 401 is received on `/versions`, the cached token MUST be cleared before re-authenticating, to avoid re-using the same expired token

## Testing Considerations

### Test Data Files

Reuse existing test files in `src/test/resources/__files/`:
- Non-Java candidate (Groovy): `groovy-2.3.6.zip` (version 2.3.6)

### Cucumber Feature Scenarios

Update the existing `state_api_integration.feature` file and/or create a new feature file `state_api_jwt_auth.feature`.

**Happy Path:**
- Release Groovy 2.3.6 → client calls `/login`, receives JWT, posts to `/versions` with Bearer token, returns 201

**Token Caching:**
- Two consecutive releases → `/login` called once, both `/versions` calls use the same Bearer token

**Token Expiry (401 Retry):**
- Release with expired token → `/versions` returns 401, client re-authenticates via `/login`, retries `/versions` with new token, returns 201

**Auth Failure:**
- `/login` returns 401 (bad credentials) → release still succeeds (MongoDB persisted), State API propagation logged as error

**Rate Limiting:**
- `/login` returns 429 → release still succeeds, rate limit logged as error

### WireMock Stubs

- Stub `POST /login` to return `{"token": "test-jwt-token"}` with status 200
- Stub `POST /versions` to verify `Authorization: Bearer test-jwt-token` header
- For 401 retry test: stub `/versions` to return 401 on first call, then 204 on second call (after re-auth)
- For auth failure test: stub `/login` to return 401

### Unit Testing

Extend `HttpStateApiClientSpec` to verify:
- JWT is included as Bearer token in request headers
- Token caching works (single login for multiple requests)
- 401 triggers re-authentication and retry
- 429 and 500 from `/login` are handled gracefully

## Implementation Notes

**Scope of Changes:**

1. `application.conf` — Replace `basic-auth` block with `auth` block using `email`/`password` keys
2. `Configuration.scala` — Replace `stateApiBasicAuthUsername`/`stateApiBasicAuthPassword` with `stateApiEmail`/`stateApiPassword`
3. `HttpStateApiClient.scala` — Primary changes:
   - Add a `login()` method that calls `POST /login` with email/password and returns the JWT string
   - Add a thread-safe cached token (e.g. `AtomicReference[Option[String]]`)
   - Modify `upsertVersionStateApi` to use Bearer auth with cached token
   - Add 401 retry logic: on 401, clear cache → login → retry once
   - Remove `.auth()` call, replace with `.header("Authorization", s"Bearer $token")`
4. `StubSteps.scala` / step definitions — Update WireMock stubs to mock `/login` and verify Bearer auth
5. Feature files — Add/update scenarios for JWT auth flow

**Coding Style:**
- Keep the scalaj-http client (no migration to Akka HTTP client)
- The `login()` method should be synchronous (called within the existing `Future { ... }` block)
- Use `java.util.concurrent.atomic.AtomicReference` for the token cache
- Maintain existing logging patterns (LazyLogging)

**What NOT to Change:**
- `VersionReleaseRoutes.scala` — The dual-write orchestration and Java filtering remain untouched
- `PlatformMapper.scala`, `DistributionMapper.scala` — No changes needed
- The `StateVersion` case class and JSON protocol — Unchanged

## Specification by Example

### Example 1: Successful JWT Auth Flow

**Step 1 — Login:**
```http
POST /login HTTP/1.1
Content-Type: application/json

{"email": "vendor@sdkman.io", "password": "secret123"}
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{"token": "eyJhbGciOiJIUzI1NiIs..."}
```

**Step 2 — Version Upsert with Bearer Token:**
```http
POST /versions HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

{
  "candidate": "groovy",
  "version": "2.3.6",
  "distribution": "APACHE",
  "url": "https://example.com/groovy-2.3.6.zip",
  "platform": "UNIVERSAL",
  "visible": true,
  "md5sum": null,
  "sha256sum": null,
  "sha512sum": null
}
```

**Response:**
```http
HTTP/1.1 204 No Content
```

### Example 2: 401 Retry Flow

**Request 1 — Version Upsert with Stale Token:**
```http
POST /versions HTTP/1.1
Authorization: Bearer <expired-token>
```
→ `401 Unauthorized`

**Request 2 — Re-authenticate:**
```http
POST /login HTTP/1.1
Content-Type: application/json

{"email": "vendor@sdkman.io", "password": "secret123"}
```
→ `200 OK` with `{"token": "<fresh-token>"}`

**Request 3 — Retry Version Upsert:**
```http
POST /versions HTTP/1.1
Authorization: Bearer <fresh-token>
```
→ `204 No Content`

### Example 3: Cucumber Scenarios

```gherkin
Feature: State API JWT Authentication

  Background:
    Given the consumer has a valid auth token
    And the state API login endpoint returns a JWT token

  Scenario: Successful version release with JWT authentication
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
      """
      {
        "candidate": "groovy",
        "version": "2.3.6",
        "url": "http://localhost:8080/groovy-2.3.6.zip",
        "platform": "UNIVERSAL"
      }
      """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API received a POST request with a Bearer token

  Scenario: Version release succeeds when JWT re-authentication is needed
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the state API will return 401 on the first version request
    When a JSON POST on the /versions endpoint:
      """
      {
        "candidate": "groovy",
        "version": "2.3.6",
        "url": "http://localhost:8080/groovy-2.3.6.zip",
        "platform": "UNIVERSAL"
      }
      """
    Then the status received is 201 CREATED
    And the state API login endpoint was called 2 times
    And the state API received a POST request with a Bearer token

  Scenario: Version release succeeds when login credentials are invalid
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the state API login endpoint returns 401
    When a JSON POST on the /versions endpoint:
      """
      {
        "candidate": "groovy",
        "version": "2.3.6",
        "url": "http://localhost:8080/groovy-2.3.6.zip",
        "platform": "UNIVERSAL"
      }
      """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API did not receive any POST requests to the versions endpoint
```

## Verification

- [ ] `POST /login` is called with `{"email": "...", "password": "..."}` to obtain JWT
- [ ] JWT is sent as `Authorization: Bearer <token>` on `/versions` requests
- [ ] Basic Auth is fully removed from the State API client
- [ ] JWT is cached and reused across multiple version release requests
- [ ] On 401 from `/versions`, client re-authenticates and retries once
- [ ] If retry also returns 401, error is logged but release still succeeds
- [ ] Login 429 (rate limit) is logged as error but release still succeeds
- [ ] Login 500 is logged as error but release still succeeds
- [ ] `STATE_API_EMAIL` env var replaces `STATE_API_USERNAME`
- [ ] `application.conf` uses `state-api.auth.email` and `state-api.auth.password`
- [ ] Java version releases are still filtered from State API (unchanged)
- [ ] Non-Java releases still dual-write to MongoDB and State API
- [ ] All existing Cucumber tests pass (updated for new auth flow)
- [ ] New Cucumber scenarios cover: happy path, 401 retry, auth failure
- [ ] Token cache is thread-safe
- [ ] Code formatted with `sbt scalafmtAll`
- [ ] Small, incremental Git commits after each passing test
