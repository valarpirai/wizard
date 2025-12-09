# Wizard

A lightweight web framework for building HTTP applications on the JVM, written in Kotlin. Built on Eclipse Jetty, Wizard provides a simple and expressive API for creating web services.

## Features

**Current:**
- HTTP method support (GET, POST, PUT, PATCH, DELETE, OPTIONS)
- Simple route registration with lambda handlers
- Request/Response wrapper API
- Jetty-based server (runs on port 8090)

**Planned:**
- Payload parsing
- JSON response handling
- Interceptor support

## Quick Start

```kotlin
import com.wizard.WizardApplication

class MyApp : WizardApplication()

fun main() {
    val app = MyApp()

    WizardApplication.get("/") { request, response ->
        "Hello, Wizard!"
    }

    WizardApplication.get("/health") { request, response ->
        response.status = 200
        "OK"
    }

    app.run()  // Starts server on port 8090
}
```

## Project Structure

This is a multi-module Gradle project:

- **`core/`** - The Wizard web framework library
- **`app/`** - Demo application showing framework usage
- **`utils/`** - Utility library with kotlinx ecosystem integrations
- **`buildSrc/`** - Convention plugin for shared build logic

## Building and Running

This project uses [Gradle](https://gradle.org/) with the Gradle Wrapper.

```bash
# Build and run the demo application
./gradlew run

# Build all modules
./gradlew build

# Run all tests and checks
./gradlew check

# Run tests for specific module
./gradlew :core:test
./gradlew :app:test

# Clean build outputs
./gradlew clean
```

The demo application in `app/` will start a server on **port 8090** with example routes at `/`, `/health`, and `/status`.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

## Configuration

The project uses modern Gradle features:
- Version catalog in `gradle/libs.versions.toml` for dependency management
- Build cache and configuration cache enabled in `gradle.properties`
- Kotlin 2.1.0 with Java 11 toolchain
- JUnit Jupiter for testing

## License

MIT License - see LICENSE file for details.
