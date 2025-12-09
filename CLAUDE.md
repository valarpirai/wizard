# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Wizard is a lightweight web framework for building HTTP applications on the JVM, written in Kotlin. It's built on Eclipse Jetty and provides a simple, expressive API for registering routes and handling requests.

## Common Commands

```bash
# Build and run the demo application (app module)
./gradlew run

# Build all modules
./gradlew build

# Run all tests and checks
./gradlew check

# Run tests for a specific module
./gradlew :core:test
./gradlew :app:test
./gradlew :utils:test

# Clean all build outputs
./gradlew clean

# Run tests with specific test class
./gradlew :core:test --tests WizardServiceTest
```

The demo application runs on **port 8090** (hardcoded in WizardService.kt:23).

## Multi-Module Architecture

This project follows a multi-module Gradle setup with three modules:

- **core/** - The web framework library itself
- **app/** - Demo application showing framework usage
- **utils/** - Utility library (separate from framework)
- **buildSrc/** - Convention plugin with shared build logic

Dependencies: `app` depends on both `core` and `utils`. The other modules are independent.

Build configuration uses:
- Version catalog in `gradle/libs.versions.toml` for dependency management
- Convention plugin in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts` for shared build logic
- Build cache and configuration cache enabled in `gradle.properties`

## Framework Architecture

### Request Flow (Singleton Pattern)

The framework uses a singleton pattern to manage server state:

1. **WizardApplication** (core/src/main/kotlin/com/wizard/WizardApplication.kt)
   - Public API with static methods: `get()` and `post()`
   - Delegates to WizardService singleton via `getInstance()`

2. **WizardService** (core/src/main/kotlin/com/wizard/WizardService.kt)
   - Singleton managing the Jetty Server instance
   - Maintains single RouteHandler instance
   - `run()` starts server on port 8090

3. **RouteHandler** (core/src/main/kotlin/com/wizard/RouteHandler.kt)
   - Extends Jetty's AbstractHandler
   - Stores routes in HashMap with `"METHOD:PATH"` string keys (line 27)
   - Routes incoming requests to registered handlers
   - Returns 404 for unregistered routes

### Route Registration Pattern

Routes are registered using:
```kotlin
WizardApplication.get("/path") { request, response ->
    // Handler code
    return "Response body"  // Any non-Unit return value is converted to string
}
```

The key insight: routes are stored as `"$method:$path"` composite keys (e.g., `"GET:/health"`).

### Response Handling

- Route handlers accept `(Request, Response) -> Any?`
- If handler returns non-Unit value, it's converted to string and written to response body
- Response status defaults to 200, can be changed via `response.status`
- Response body can also be set via `response.body`
- Both handler return value AND response.body are written if both exist

## Key Files and Their Roles

**Core Framework:**
- `WizardApplication.kt` - Public API entry point
- `WizardService.kt` - Singleton server lifecycle manager
- `RouteHandler.kt` - Request routing and dispatch
- `Route.kt` - Functional interface for route handlers
- `Request.kt` / `Response.kt` - Servlet wrappers
- `HttpMethod.kt` - Enum for HTTP methods

**Demo Application:**
- `app/src/main/kotlin/App.kt` - Example showing two usage patterns:
  1. Class-based: extending WizardApplication
  2. Inline: direct static method calls

## Planned Features

From README tasks:
- Payload parsing (not yet implemented)
- JSON response handling (not yet implemented)
- Interceptor support (not yet implemented)

When implementing these, consider that the current architecture stores routes in a simple HashMap and processes requests synchronously through RouteHandler.handle().
