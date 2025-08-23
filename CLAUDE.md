# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

The SDKMAN! Vendor Release API is a Scala-based REST service that allows software vendors to release new candidate versions on the SDKMAN! platform. Built using Akka HTTP, it provides endpoints for managing SDK candidates and their versions.

## Build System & Dependencies

- **Build Tool**: SBT 1.4.3
- **Scala Version**: 2.12.10
- **Key Dependencies**:
  - Akka HTTP 10.2.4 for REST API
  - MongoDB for persistence via `sdkman-mongodb-persistence`
  - Spray JSON for JSON handling
  - URL validation via `sdkman-url-validator`

## Development Commands

### Building
```bash
sbt compile
```

### Testing
```bash
sbt test
```

### Code Formatting
```bash
sbt scalafmtAll
```
Always format code before committing.

### Running Locally
Start MongoDB first:
```bash
docker run --rm -d -p="27017:27017" --name=mongo mongo:3.2
```

### Release Process
The project uses sbt-release plugin with automated Docker publishing:
```bash
sbt release
```

## Architecture

### Core Components

**HTTP Server** (`HttpServer.scala`):
- Main application entry point
- Combines all route traits using the cake pattern
- Runs on configurable host/port (default: 0.0.0.0:9000)

**Route Modules**:
- `VersionReleaseRoutes`: POST endpoints for releasing SDK versions
- `CandidateReleaseRoutes`: POST endpoints for creating/updating candidates
- `CandidateDefaultRoutes`: PUT endpoints for setting default versions
- `HealthRoutes`: Simple health check endpoint

**Configuration** (`Configuration.scala`):
- Trait providing access to Typesafe Config
- Service settings (host, port, auth tokens)
- MongoDB connection details from environment variables

**Security**:
- Token-based authorization via `Authorisation` trait
- Admin consumer validation
- Request validation for URLs, platforms, and data integrity

### Database Integration

Uses external SDKMAN! MongoDB persistence library:
- Candidates stored in MongoDB
- Version metadata and binaries tracked
- Platform-specific releases supported (Linux, macOS, Windows)

## Testing Strategy

**Cucumber BDD Tests**:
- Feature files in `src/test/resources/features/`
- Step definitions in `src/test/scala/steps/`
- Test scenarios cover:
  - Health endpoints
  - Version release workflows (happy/unhappy paths)
  - Security and authorization
  - Input validation
  - Multi-platform releases

**Test Support**:
- `RunCukes.scala`: Main test runner
- WireMock for HTTP stubbing
- MongoDB test utilities
- Custom step definitions for API interactions

## Configuration

**Application Config** (`application.conf`):
- Akka logging configuration
- Service host/port settings
- MongoDB connection parameters
- Environment variable overrides supported

**Environment Variables**:
- `ADMIN_CONSUMER`: Override default admin consumer
- `SERVICE_TOKEN`: API authentication token
- `MONGO_HOST/PORT/DATABASE`: MongoDB connection details
- `MONGO_USERNAME/PASSWORD`: MongoDB authentication

## Key Patterns

1. **Cake Pattern**: Dependency injection using Scala traits
2. **Future-based**: Asynchronous operations throughout
3. **Route Composition**: Akka HTTP routes combined using `~` operator
4. **JSON Marshalling**: Spray JSON for request/response serialization
5. **Validation Pipeline**: Multi-layer validation for security and data integrity