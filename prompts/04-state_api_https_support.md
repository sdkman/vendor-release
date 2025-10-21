# State API Client HTTPS Support

The State API HTTP client in `HttpStateApiClient` is currently configured to only communicate over HTTP. This works fine in tests where WireMock is used (HTTP on localhost), but fails when attempting to communicate with the actual live State API service which uses HTTPS. The Akka HTTP client needs to be configured to support HTTPS connections.

## Requirements

- Configure the Akka HTTP client in `HttpStateApiClient` to support HTTPS connections
- Ensure backward compatibility with existing HTTP connections (for local development and testing)
- The solution must work seamlessly with the existing WireMock-based tests (which use HTTP)
- The solution must enable production communication with HTTPS-based State API endpoints
- Maintain existing authentication mechanism (HTTP Basic Auth)
- No changes to the existing API or method signatures
- Configuration should use the protocol specified in the `stateApiBaseUrl` configuration value

## Akka HTTP Client HTTPS Reference

According to the Akka HTTP documentation, the `singleRequest()` method automatically detects HTTPS based on the URI scheme. The key patterns are:

### Default HTTPS Context Setup
```scala
// Option 1: Use system default HTTPS context (recommended for standard use cases)
Http().setDefaultClientHttpsContext(Http().defaultClientHttpsContext)
```

### HTTPS Connection Context Priority
The Akka HTTP client follows this priority for HTTPS configuration:
1. Explicitly provided `httpsContext` parameter
2. Default client-side `HttpsContext` (set via `setDefaultClientHttpsContext`)
3. System default TLS configuration (Java's default TLS settings)

### Auto-Detection via URI Scheme
```scala
// The singleRequest() method automatically uses HTTPS when the URI starts with https://
http.singleRequest(HttpRequest(uri = "https://api.example.com/endpoint"))
// Automatically uses HTTPS connection context

http.singleRequest(HttpRequest(uri = "http://localhost:8080/endpoint"))
// Uses regular HTTP connection
```

### Current Implementation Location
File: `src/main/scala/io/sdkman/vendor/release/routes/HttpStateApiClient.scala`

Line 37:
```scala
lazy val http = Http(actorSystem)
```

Lines 65-78:
```scala
http.singleRequest(
  HttpRequest(
    uri = s"$stateApiBaseUrl/versions",
    method = HttpMethods.POST,
    entity = HttpEntity(ContentTypes.`application/json`, stateVersion.toJson.compactPrint)
  ).withHeaders(
    Authorization(BasicHttpCredentials(stateApiBasicAuthUsername, stateApiBasicAuthPassword))
  )
)
```

## Extra Considerations

- The Akka HTTP `singleRequest()` method automatically handles HTTPS when the URI scheme is `https://`
- The default HTTPS context uses Java's default TLS settings, which is appropriate for standard SSL/TLS certificates
- WireMock tests use `http://localhost:8089` as the base URL (see `HttpStateApiClientSpec.scala:47`)
- Production will use HTTPS URLs with valid SSL certificates
- No custom SSL certificate validation or trust store configuration should be needed
- The existing connection pooling and timeout behavior should remain unchanged
- The solution should leverage Akka's built-in automatic protocol detection

## Testing Considerations

- All existing unit tests in `HttpStateApiClientSpec` must continue to pass without modification
- All existing BDD tests (Cucumber features) must continue to pass without modification
- Tests use WireMock over HTTP and should not be affected by HTTPS support
- Manual verification should be possible by pointing to an HTTPS endpoint
- Consider adding a test comment documenting that HTTPS support is present

## Implementation Notes

- Follow the Akka HTTP documentation for client-side HTTPS support: https://doc.akka.io/libraries/akka-http/current/client-side/client-https-support.html
- Use the default `HttpsConnectionContext` provided by Akka HTTP
- The solution should be minimal and leverage Akka's built-in HTTPS support
- Avoid adding external dependencies or custom SSL/TLS configuration
- Keep the code changes localized to `HttpStateApiClient.scala`
- Use Scala's lazy initialization pattern consistent with the existing codebase
- Maintain the existing logging behavior
- The default client HTTPS context should be set on the `Http` extension instance

## Specification by Example

### Current Behavior (HTTP Only)
```scala
// Configuration
stateApiBaseUrl = "http://localhost:8080"

// Works fine - HTTP request succeeds
http.singleRequest(HttpRequest(uri = s"$stateApiBaseUrl/versions", ...))
// ✅ Success
```

```scala
// Configuration
stateApiBaseUrl = "https://api.sdkman.io"

// Fails - HTTPS not properly configured
http.singleRequest(HttpRequest(uri = s"$stateApiBaseUrl/versions", ...))
// ❌ Fails with SSL/TLS error
```

### Expected Behavior (HTTP and HTTPS)
```scala
// Configuration
stateApiBaseUrl = "http://localhost:8080"

// Still works - HTTP request succeeds
http.singleRequest(HttpRequest(uri = s"$stateApiBaseUrl/versions", ...))
// ✅ Success
```

```scala
// Configuration
stateApiBaseUrl = "https://api.sdkman.io"

// Now works - HTTPS properly configured
http.singleRequest(HttpRequest(uri = s"$stateApiBaseUrl/versions", ...))
// ✅ Success with proper SSL/TLS handling
```

## Verification

- [ ] All existing unit tests in `HttpStateApiClientSpec` pass
- [ ] All existing Cucumber BDD tests pass
- [ ] Code compiles without warnings
- [ ] Code formatted with `sbt scalafmtAll`
- [ ] The implementation follows Akka HTTP best practices from official documentation
- [ ] No new dependencies added to `build.sbt`
- [ ] HTTP connections still work (for local development and testing)
- [ ] HTTPS connections work (for production use)
- [ ] Basic authentication continues to work over HTTPS
- [ ] Logging output remains unchanged
