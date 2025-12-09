# Product Requirements Document (PRD)
## Wizard Web Framework

---

### Document Information
- **Project Name**: Wizard
- **Document Version**: 1.0
- **Last Updated**: December 9, 2025
- **Status**: Draft

---

## 1. Executive Summary

### 1.1 Product Vision
Wizard is a lightweight, high-performance web framework for building scalable HTTP applications on the JVM. Written in Kotlin and built on Eclipse Jetty, Wizard provides a simple, expressive API that enables developers to quickly build production-ready web services with enterprise-grade features.

### 1.2 Target Audience
- Backend developers building RESTful APIs and web services
- Teams requiring a lightweight alternative to heavyweight frameworks
- Kotlin/Java developers seeking modern, idiomatic APIs
- Organizations needing scalable web applications with built-in database and authentication support

### 1.3 Key Differentiators
- Minimal boilerplate with maximum functionality
- Built-in database support with connection pooling and migrations
- Pluggable authentication framework
- Auto-detection and content negotiation
- Comprehensive metrics and monitoring out-of-the-box
- Simple configuration with environment variable support

---

## 2. Goals and Objectives

### 2.1 Business Goals
- Provide a production-ready web framework that reduces development time
- Enable rapid development of scalable web applications
- Lower the barrier to entry for building enterprise-grade services
- Support common use cases without requiring external libraries

### 2.2 Technical Goals
- Maintain high performance with efficient resource utilization
- Provide type-safe APIs leveraging Kotlin's type system
- Ensure thread-safe concurrent request handling
- Support synchronous request processing model (no async complexity)
- Enable easy testing and debugging

### 2.3 Success Metrics
- Developer productivity: Time to create a working API endpoint
- Performance: Request throughput and latency benchmarks
- Adoption: Community usage and feedback
- Stability: Error rates and production reliability
- Documentation completeness and clarity

---

## 3. Core Features Overview

Wizard will provide the following major feature categories:

| Feature Category | Description | Priority |
|-----------------|-------------|----------|
| HTTP Routing & Handlers | Route registration, path parameters, query parameters | P0 |
| Request Payload Parsing | Auto-detection and parsing of JSON, form data, multipart | P0 |
| Response Handling | Content negotiation, auto-serialization (JSON/XML/text) | P0 |
| Interceptors | Pre/post request processing with priority support | P0 |
| Database Support | MySQL/PostgreSQL with HikariCP connection pooling | P1 |
| Database Migrations | YAML-based schema migrations with rollback support | P1 |
| Authentication | Pluggable auth with Basic, JWT, Session support | P1 |
| Static File Serving | Serve static assets (HTML, CSS, JS, images) | P1 |
| Session Management | Built-in session handling | P1 |
| Cookie Handling | Easy API for reading/setting cookies | P1 |
| Response Compression | Gzip compression support | P2 |
| Configuration Management | Single config file with environment variable support | P0 |
| Logging | Configurable logging integration | P1 |
| Metrics & Monitoring | Detailed metrics endpoint (Prometheus-compatible) | P1 |
| Health Checks | Built-in health check endpoints | P1 |
| Graceful Shutdown | Proper handling of in-flight requests | P1 |

---

## 4. Detailed Feature Specifications

### 4.1 HTTP Routing and Request Handling

#### 4.1.1 Route Registration
**Description**: Simple API for registering routes with HTTP method and path

**Requirements**:
- Support all standard HTTP methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Routes registered via static methods: `WizardApplication.get(path, handler)`
- Handler receives Request and Response objects
- Handler can return values (auto-converted to response body) or set response.body directly

**API Example**:
```kotlin
WizardApplication.get("/users") { request, response ->
    "List of users"
}

WizardApplication.post("/users") { request, response ->
    val user = request.body<User>()
    // Create user logic
    response.status = 201
    user
}
```

#### 4.1.2 Path Parameters
**Description**: Extract dynamic segments from URL paths

**Requirements**:
- Support path variables like `/users/{id}` and `/posts/{postId}/comments/{commentId}`
- Path parameters accessible via `request.pathParam("id")`
- Type conversion support for common types (String, Int, Long, UUID)

**API Example**:
```kotlin
WizardApplication.get("/users/{id}") { request, response ->
    val userId = request.pathParam("id")
    // Fetch user logic
}
```

#### 4.1.3 Query Parameters
**Description**: Extract and parse query string parameters

**Requirements**:
- Access via `request.queryParam("name")`
- Support optional parameters with default values
- Support multiple values for same parameter name
- Type conversion support

**API Example**:
```kotlin
WizardApplication.get("/search") { request, response ->
    val query = request.queryParam("q") ?: ""
    val page = request.queryParam("page")?.toInt() ?: 1
    // Search logic
}
```

---

### 4.2 Request Payload Parsing

#### 4.2.1 Auto-Detection
**Description**: Automatically detect and parse request body based on Content-Type header

**Requirements**:
- Auto-detect content type from `Content-Type` header
- Support `application/json`, `application/x-www-form-urlencoded`, `multipart/form-data`
- Throw exception on parsing failure with clear error message

#### 4.2.2 JSON Parsing
**Description**: Parse JSON request bodies into typed Kotlin objects

**Requirements**:
- Use high-performance JSON library (Jackson or kotlinx.serialization)
- Support data classes with reflection-based mapping
- Access via `request.body<T>()` with reified type parameter
- Throw `PayloadParseException` on invalid JSON

**API Example**:
```kotlin
data class CreateUserRequest(val name: String, val email: String)

WizardApplication.post("/users") { request, response ->
    val userRequest = request.body<CreateUserRequest>()
    // Process request
}
```

#### 4.2.3 Form Data Parsing
**Description**: Parse URL-encoded form data

**Requirements**:
- Parse `application/x-www-form-urlencoded` content type
- Support nested fields
- Access via `request.formParam("fieldName")`
- Also support typed form data via `request.body<T>()`

#### 4.2.4 Multipart File Upload
**Description**: Handle file uploads via multipart/form-data

**Requirements**:
- Parse `multipart/form-data` requests
- Support multiple file uploads
- Access files via `request.files()` or `request.file("fieldName")`
- Provide file metadata (name, size, content type)
- Support form fields alongside files

**API Example**:
```kotlin
WizardApplication.post("/upload") { request, response ->
    val file = request.file("attachment")
    val description = request.formParam("description")
    // Save file logic
}
```

---

### 4.3 Response Handling and Content Negotiation

#### 4.3.1 Content Negotiation
**Description**: Automatically serialize responses based on Accept header

**Requirements**:
- Check `Accept` header to determine response format
- Support `application/json`, `application/xml`, `text/plain`, `text/html`
- Default to JSON if Accept header not specified or contains `*/*`
- Return 406 Not Acceptable if requested format not supported

#### 4.3.2 JSON Serialization
**Description**: Automatically serialize Kotlin/Java objects to JSON

**Requirements**:
- Use high-performance JSON library (Jackson recommended for performance)
- Serialize any returned object from handler to JSON
- Support custom serialization rules via configuration
- Support custom date/time formats
- Handle circular references gracefully

**Configuration Example**:
```kotlin
WizardApplication.configure {
    json {
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        serializationInclusion = NON_NULL // Don't serialize null fields
        prettyPrint = false
    }
}
```

**API Example**:
```kotlin
data class User(val id: Int, val name: String, val createdAt: LocalDateTime)

WizardApplication.get("/users/{id}") { request, response ->
    User(1, "John Doe", LocalDateTime.now()) // Auto-serialized to JSON
}
```

#### 4.3.3 XML Serialization
**Description**: Support XML responses when requested

**Requirements**:
- Serialize objects to XML when Accept header is `application/xml`
- Use Jackson XML module or similar
- Support custom XML configuration

#### 4.3.4 Plain Text Responses
**Description**: Return plain text responses

**Requirements**:
- When handler returns String and Accept is `text/plain`, return as-is
- Set `Content-Type: text/plain` header automatically

#### 4.3.5 Manual Response Control
**Description**: Allow manual control over response

**Requirements**:
- Set status code via `response.status = 201`
- Set headers via `response.header("X-Custom", "value")`
- Set body directly via `response.body = "content"`
- Set content type via `response.contentType = "application/pdf"`

---

### 4.4 Interceptors (Middleware)

#### 4.4.1 Pre-Request Interceptors
**Description**: Execute logic before route handlers

**Requirements**:
- Register interceptors globally for all routes
- Execute before route handler
- Access and modify request object
- Ability to short-circuit and prevent handler execution
- Return early response (e.g., 401 Unauthorized)

#### 4.4.2 Post-Request Interceptors
**Description**: Execute logic after route handlers

**Requirements**:
- Execute after route handler completes
- Access and modify response object
- Modify response headers, body, status code
- Cannot short-circuit (handler already executed)

#### 4.4.3 Priority System
**Description**: Control execution order of multiple interceptors

**Requirements**:
- Assign priority (integer) to each interceptor
- Lower priority number = higher priority (executes first)
- Default priority: 100
- Pre-request interceptors execute in ascending priority order
- Post-request interceptors execute in descending priority order

#### 4.4.4 Error Handling Interceptors
**Description**: Catch and handle exceptions from route handlers

**Requirements**:
- Register error interceptors for specific exception types
- Catch exceptions thrown by handlers or other interceptors
- Transform exceptions into appropriate HTTP responses
- Log errors for monitoring
- Support global error handler for uncaught exceptions

**API Example**:
```kotlin
// Pre-request interceptor
WizardApplication.intercept(priority = 10) { request, response, chain ->
    println("Logging request: ${request.method} ${request.path}")
    chain.proceed(request, response) // Continue to next interceptor/handler
}

// Authentication interceptor with short-circuit
WizardApplication.intercept(priority = 50) { request, response, chain ->
    val token = request.header("Authorization")
    if (token == null) {
        response.status = 401
        response.body = "Unauthorized"
        return@intercept // Short-circuit - don't call chain.proceed()
    }
    chain.proceed(request, response)
}

// Post-request interceptor
WizardApplication.interceptAfter(priority = 100) { request, response ->
    response.header("X-Powered-By", "Wizard")
}

// Error handling interceptor
WizardApplication.interceptError<ValidationException> { exception, request, response ->
    response.status = 400
    response.body = mapOf("error" to exception.message)
}
```

#### 4.4.5 Common Interceptor Use Cases
**Built-in interceptors should be provided for**:
- CORS handling
- Request logging
- Authentication validation
- Rate limiting (future)
- Request timing/metrics

---

### 4.5 Database Support

#### 4.5.1 Supported Databases
**Description**: Built-in support for popular relational databases

**Requirements**:
- MySQL support (JDBC driver included)
- PostgreSQL support (JDBC driver included)
- Automatic driver loading based on connection URL

#### 4.5.2 Connection Pooling with HikariCP
**Description**: High-performance connection pooling

**Requirements**:
- Use HikariCP as connection pool implementation
- Sensible default configuration values
- All HikariCP settings configurable via properties
- Automatic pool initialization on startup

**Default HikariCP Configuration**:
```yaml
database:
  pool:
    maximumPoolSize: 10
    minimumIdle: 5
    connectionTimeout: 30000  # 30 seconds
    idleTimeout: 600000       # 10 minutes
    maxLifetime: 1800000      # 30 minutes
    autoCommit: true
    poolName: WizardPool
```

#### 4.5.3 Raw SQL Queries
**Description**: Execute raw SQL with parameter binding

**Requirements**:
- Support for SELECT, INSERT, UPDATE, DELETE queries
- Named parameter binding for security (prevent SQL injection)
- Result set mapping to Kotlin objects via reflection
- Support for batch operations
- Return generated keys for INSERT operations

**API Example**:
```kotlin
// Simple query
val users = db.query<User>("SELECT * FROM users WHERE age > :age", mapOf("age" to 18))

// Single result
val user = db.queryOne<User>("SELECT * FROM users WHERE id = :id", mapOf("id" to 1))

// Insert with generated key
val userId = db.insert("INSERT INTO users (name, email) VALUES (:name, :email)",
    mapOf("name" to "John", "email" to "john@example.com"))

// Update
val rowsAffected = db.update("UPDATE users SET name = :name WHERE id = :id",
    mapOf("name" to "Jane", "id" to 1))

// Batch insert
db.batchInsert("INSERT INTO users (name, email) VALUES (:name, :email)",
    listOf(
        mapOf("name" to "User1", "email" to "user1@example.com"),
        mapOf("name" to "User2", "email" to "user2@example.com")
    ))
```

#### 4.5.4 Query Builder / DSL
**Description**: Type-safe query builder for common operations

**Requirements**:
- Fluent API for building queries
- Support SELECT, INSERT, UPDATE, DELETE operations
- Type-safe column references
- WHERE clause builder with AND/OR conditions
- Support for JOIN operations
- ORDER BY, GROUP BY, LIMIT, OFFSET

**API Example**:
```kotlin
// SELECT with WHERE
val users = db.select<User>()
    .from("users")
    .where("age", ">", 18)
    .and("status", "=", "active")
    .orderBy("created_at", DESC)
    .limit(10)
    .execute()

// INSERT
db.insert()
    .into("users")
    .values(mapOf("name" to "John", "email" to "john@example.com"))
    .execute()

// UPDATE
db.update("users")
    .set("status", "inactive")
    .where("last_login", "<", LocalDate.now().minusDays(30))
    .execute()

// DELETE
db.delete()
    .from("users")
    .where("id", "=", 123)
    .execute()
```

#### 4.5.5 Reflection-Based Result Mapping
**Description**: Automatically map query results to Kotlin objects

**Requirements**:
- Map result set columns to data class properties by name
- Support for type conversion (SQL types to Kotlin types)
- Handle nullable columns
- Support for camelCase to snake_case conversion
- Support for nested objects (join results)

**Example**:
```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)

// Automatic mapping from users table
val users = db.query<User>("SELECT id, name, email, created_at FROM users")
```

#### 4.5.6 Transaction Management
**Description**: Programmatic and declarative transaction support

**Requirements**:
- Programmatic transactions with begin/commit/rollback
- `@Transactional` annotation support for methods
- Transaction propagation (REQUIRED, REQUIRES_NEW, NESTED)
- Automatic rollback on exception
- Savepoint support for nested transactions

**API Example**:
```kotlin
// Programmatic transaction
db.transaction {
    val userId = insert("INSERT INTO users (name) VALUES (:name)", mapOf("name" to "John"))
    insert("INSERT INTO user_profile (user_id, bio) VALUES (:userId, :bio)",
        mapOf("userId" to userId, "bio" to "Hello"))
    // Auto-commit if no exception, auto-rollback on exception
}

// Declarative transaction
@Transactional
fun createUserWithProfile(name: String, bio: String) {
    val userId = db.insert("INSERT INTO users (name) VALUES (:name)", mapOf("name" to name))
    db.insert("INSERT INTO user_profile (user_id, bio) VALUES (:userId, :bio)",
        mapOf("userId" to userId, "bio" to bio))
}
```

#### 4.5.7 Connection Interface
**Description**: Extensible connection management

**Requirements**:
- JDBC connections created automatically by default
- Interface for custom connection providers
- Can override default connection creation logic
- Support for connection testing and validation

**Interface Example**:
```kotlin
interface ConnectionProvider {
    fun getConnection(): Connection
    fun releaseConnection(connection: Connection)
    fun testConnection(): Boolean
}

// Custom implementation
class CustomConnectionProvider : ConnectionProvider {
    override fun getConnection(): Connection {
        // Custom logic
    }
}

// Register custom provider
WizardApplication.configure {
    database {
        connectionProvider = CustomConnectionProvider()
    }
}
```

#### 4.5.8 Multi-Database Support
**Description**: Connect to multiple databases simultaneously

**Requirements**:
- Configure multiple database connections with unique names
- Route queries to specific database based on request context
- Customizable routing rules
- Default database for requests without specific routing

**Configuration Example**:
```yaml
databases:
  primary:
    url: jdbc:mysql://localhost:3306/app_db
    username: user
    password: pass

  analytics:
    url: jdbc:postgresql://localhost:5432/analytics_db
    username: user
    password: pass

  cache:
    url: jdbc:mysql://localhost:3306/cache_db
    username: user
    password: pass

routing:
  default: primary
  rules:
    - pattern: "/api/reports/**"
      database: analytics
    - pattern: "/api/cache/**"
      database: cache
```

**API Example**:
```kotlin
// Explicit database selection
val users = db.using("analytics").query<User>("SELECT * FROM users")

// Route-based selection (automatic based on request path)
WizardApplication.get("/api/reports/users") { request, response ->
    // Automatically uses 'analytics' database based on routing rules
    db.query<User>("SELECT * FROM users")
}
```

---

### 4.6 Database Migrations

#### 4.6.1 YAML-Based Migrations
**Description**: Liquibase-inspired migrations using YAML format

**Requirements**:
- Migrations stored in `src/main/resources/db/migrations/` directory
- File naming convention: `{version}_{description}.yml` (e.g., `001_create_users_table.yml`)
- Version number prefix for ordering (001, 002, 003, etc.)
- Support inline SQL in YAML
- Support both DDL (CREATE, ALTER, DROP) and DML (INSERT, UPDATE, DELETE)

**YAML Structure Example**:
```yaml
version: 001
description: Create users table
database: ALL  # or specific database name like 'primary', 'analytics'

changesets:
  - id: 1
    author: developer
    changes:
      - sql: |
          CREATE TABLE users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
          )
    rollback:
      - sql: DROP TABLE users

  - id: 2
    author: developer
    changes:
      - sql: |
          CREATE INDEX idx_users_email ON users(email)
    rollback:
      - sql: DROP INDEX idx_users_email ON users
```

#### 4.6.2 Migration Tracking
**Description**: Track applied migrations similar to Liquibase

**Requirements**:
- Create `WIZARD_CHANGELOG` table automatically on first run
- Table structure:
  - `version` (INT): Migration version number
  - `description` (VARCHAR): Migration description
  - `filename` (VARCHAR): Migration file name
  - `executed_at` (TIMESTAMP): When migration was applied
  - `execution_time_ms` (INT): How long it took
  - `status` (VARCHAR): SUCCESS or FAILED
  - `checksum` (VARCHAR): MD5 hash of migration file
- Detect changed migrations by comparing checksums
- Warn if applied migration file has been modified

#### 4.6.3 Migration Execution
**Description**: Execute migrations automatically or manually

**Requirements**:
- **Automatic execution on startup** (configurable)
- **CLI command** for manual execution: `./gradlew migrate` or similar
- Execute migrations in version number order
- Skip already-applied migrations
- Stop on first failure
- Transactional execution (rollback on failure)

**Configuration**:
```yaml
migrations:
  enabled: true
  autoRun: true  # Run on startup
  onStartup: true
  validateChecksums: true
  locations:
    - classpath:db/migrations
```

#### 4.6.4 Rollback Support
**Description**: Rollback migrations to previous version

**Requirements**:
- Each changeset should have rollback section
- Support rollback to specific version
- Support rollback of last N migrations
- Rollback executes in reverse order
- Update `WIZARD_CHANGELOG` table after rollback

**CLI Example**:
```bash
# Rollback to specific version
./gradlew migrateRollback --version=003

# Rollback last migration
./gradlew migrateRollback --count=1

# Rollback all migrations
./gradlew migrateRollback --all
```

#### 4.6.5 Multi-Database Migrations
**Description**: Support migrations for multiple databases

**Requirements**:
- Each migration file has `database` property
- `database: ALL` - applies to all configured databases
- `database: primary` - applies only to database named "primary"
- Separate tracking per database (separate `WIZARD_CHANGELOG` table in each)
- Can run migrations for specific database only

**Example**:
```yaml
# 001_create_users_primary.yml
version: 001
database: primary
description: Create users in primary database
changesets:
  - id: 1
    changes:
      - sql: CREATE TABLE users (...)

# 002_create_analytics_tables.yml
version: 002
database: analytics
description: Create analytics tables
changesets:
  - id: 1
    changes:
      - sql: CREATE TABLE page_views (...)
```

#### 4.6.6 Migration Validation
**Description**: Validate migrations before execution

**Requirements**:
- Check for duplicate version numbers
- Check for missing version numbers in sequence
- Validate YAML syntax
- Validate SQL syntax (basic validation)
- Check that rollback is defined for each changeset
- Warn about potentially dangerous operations (DROP TABLE, TRUNCATE)

---

### 4.7 Authentication Framework

#### 4.7.1 Pluggable Architecture
**Description**: Flexible authentication system supporting multiple strategies

**Requirements**:
- Plugin interface for custom authentication providers
- Built-in support for Basic Auth, JWT, and Session-based auth
- Multiple authentication strategies can be active simultaneously
- Route-specific authentication via interceptors with pattern matching

**Interface**:
```kotlin
interface AuthenticationProvider {
    fun authenticate(request: Request): AuthenticationResult
    fun supports(request: Request): Boolean
}

sealed class AuthenticationResult {
    data class Success(val user: User) : AuthenticationResult()
    data class Failure(val reason: String) : AuthenticationResult()
}
```

#### 4.7.2 Basic Authentication
**Description**: HTTP Basic Authentication support

**Requirements**:
- Parse Authorization header with Basic scheme
- Base64 decode credentials
- Validate username/password against configured provider
- Support custom credential validation logic

**API Example**:
```kotlin
WizardApplication.configure {
    auth {
        basic {
            realm = "Wizard App"
            validator = { username, password ->
                // Custom validation logic
                userService.validateCredentials(username, password)
            }
        }
    }
}

// Protect routes with Basic Auth
WizardApplication.auth()
    .basic()
    .routes("/api/admin/**")
```

#### 4.7.3 JWT Authentication
**Description**: JSON Web Token authentication

**Requirements**:
- Generate JWT tokens with configurable claims
- Validate JWT signature and expiration
- Support HS256, HS384, HS512, RS256 algorithms
- Extract user information from token claims
- Refresh token support
- Configurable token expiration

**Configuration**:
```kotlin
WizardApplication.configure {
    auth {
        jwt {
            secret = "your-secret-key"  // or read from env
            algorithm = HS256
            issuer = "wizard-app"
            audience = "wizard-users"
            expirationMinutes = 60
            refreshTokenExpirationDays = 30
        }
    }
}
```

**API Example**:
```kotlin
// Generate token
val token = auth.jwt().generate(userId = 123, claims = mapOf("role" to "admin"))

// Protect routes with JWT
WizardApplication.auth()
    .jwt()
    .routes("/api/**")

// Access user in handler
WizardApplication.get("/api/profile") { request, response ->
    val user = request.user()  // Automatically populated by auth interceptor
    user
}
```

#### 4.7.4 Session-Based Authentication
**Description**: Server-side session management

**Requirements**:
- Create and manage server-side sessions
- Session storage (in-memory default, pluggable for Redis/database)
- Session ID in cookie (configurable name)
- Configurable session timeout
- CSRF protection support

**Configuration**:
```kotlin
WizardApplication.configure {
    auth {
        session {
            cookieName = "WIZARD_SESSION"
            timeout = Duration.ofHours(24)
            secure = true  // HTTPS only
            httpOnly = true
            sameSite = SameSite.Strict
        }
    }
}
```

**API Example**:
```kotlin
// Login endpoint creates session
WizardApplication.post("/login") { request, response ->
    val credentials = request.body<LoginRequest>()
    val user = userService.authenticate(credentials.username, credentials.password)

    if (user != null) {
        request.session().set("userId", user.id)
        request.session().set("role", user.role)
        response.status = 200
        mapOf("message" to "Login successful")
    } else {
        response.status = 401
        mapOf("error" to "Invalid credentials")
    }
}

// Protected route checks session
WizardApplication.auth()
    .session()
    .routes("/dashboard/**")

WizardApplication.get("/dashboard") { request, response ->
    val userId = request.session().get("userId")
    // Load user data
}
```

#### 4.7.5 User Context in Request
**Description**: Authenticated user available in request object

**Requirements**:
- `request.user()` returns authenticated user object
- `request.isAuthenticated()` checks if user is authenticated
- User object populated by authentication interceptor
- Type-safe user access with generics

**API Example**:
```kotlin
data class AppUser(val id: Int, val username: String, val role: String)

WizardApplication.get("/api/profile") { request, response ->
    if (request.isAuthenticated()) {
        val user = request.user<AppUser>()
        mapOf("username" to user.username, "role" to user.role)
    } else {
        response.status = 401
        mapOf("error" to "Not authenticated")
    }
}
```

#### 4.7.6 Password Hashing with BCrypt
**Description**: Secure password storage utilities

**Requirements**:
- BCrypt hashing for passwords
- Configurable cost factor (default: 12)
- Utility methods for hashing and verification

**API Example**:
```kotlin
// Hash password
val hashedPassword = PasswordUtil.hash("user-password")

// Verify password
val isValid = PasswordUtil.verify("user-password", hashedPassword)

// Store in database
db.insert("INSERT INTO users (username, password_hash) VALUES (:username, :password)",
    mapOf("username" to "john", "password" to hashedPassword))
```

#### 4.7.7 Interceptor-Based Route Protection
**Description**: Protect routes with authentication interceptors

**Requirements**:
- Pattern-based route matching (glob patterns)
- Support multiple auth strategies per route
- Strategies tried in order until one succeeds
- Return 401 if all strategies fail
- Configurable failure behavior

**API Example**:
```kotlin
// Single strategy
WizardApplication.auth()
    .jwt()
    .routes("/api/**")

// Multiple strategies (try JWT first, fallback to Basic)
WizardApplication.auth()
    .strategies(jwt(), basic())
    .routes("/api/**")

// Different auth for different routes
WizardApplication.auth()
    .jwt()
    .routes("/api/user/**")

WizardApplication.auth()
    .basic()
    .routes("/api/admin/**")

// Public routes (no auth)
// Just don't register auth interceptor for those routes
WizardApplication.get("/public/info") { request, response ->
    "Public information"
}
```

---

### 4.8 Additional HTTP Features

#### 4.8.1 Static File Serving
**Description**: Serve static assets like HTML, CSS, JavaScript, images

**Requirements**:
- Configure directory for static files
- Map URL path to filesystem directory
- Support for index files (index.html)
- Proper MIME type detection
- Caching headers (ETag, Last-Modified)
- Support for range requests (partial content)

**Configuration**:
```kotlin
WizardApplication.configure {
    staticFiles {
        directory = "src/main/resources/public"
        urlPath = "/static"
        indexFile = "index.html"
        cacheControl = "public, max-age=3600"
    }
}
```

**Example**:
```
Files in src/main/resources/public/:
  - index.html
  - css/styles.css
  - js/app.js
  - images/logo.png

Accessible at:
  - http://localhost:8090/static/index.html
  - http://localhost:8090/static/css/styles.css
  - http://localhost:8090/static/js/app.js
  - http://localhost:8090/static/images/logo.png
```

#### 4.8.2 Cookie Handling
**Description**: Easy API for reading and setting cookies

**Requirements**:
- Read cookies via `request.cookie("name")`
- Set cookies via `response.cookie(name, value, options)`
- Support cookie options: maxAge, path, domain, secure, httpOnly, sameSite
- Delete cookies via `response.deleteCookie("name")`

**API Example**:
```kotlin
WizardApplication.get("/set-cookie") { request, response ->
    response.cookie("user_pref", "dark_mode", CookieOptions(
        maxAge = Duration.ofDays(30),
        httpOnly = true,
        secure = true,
        sameSite = SameSite.Lax
    ))
    "Cookie set"
}

WizardApplication.get("/get-cookie") { request, response ->
    val preference = request.cookie("user_pref") ?: "default"
    "User preference: $preference"
}

WizardApplication.get("/delete-cookie") { request, response ->
    response.deleteCookie("user_pref")
    "Cookie deleted"
}
```

#### 4.8.3 Session Management
**Description**: Built-in server-side session handling

**Requirements**:
- Create session via `request.session()`
- Store key-value pairs in session
- Session persistence (in-memory default, pluggable)
- Session timeout and cleanup
- Session regeneration (prevent fixation attacks)

**API Example**:
```kotlin
WizardApplication.get("/login") { request, response ->
    request.session().set("userId", 123)
    request.session().set("username", "john")
    "Logged in"
}

WizardApplication.get("/profile") { request, response ->
    val userId = request.session().get<Int>("userId")
    if (userId != null) {
        "User ID: $userId"
    } else {
        response.status = 401
        "Not logged in"
    }
}

WizardApplication.get("/logout") { request, response ->
    request.session().invalidate()
    "Logged out"
}
```

#### 4.8.4 Response Compression
**Description**: Automatic Gzip compression for responses

**Requirements**:
- Compress response body with Gzip
- Check Accept-Encoding header
- Configurable minimum size threshold
- Configurable compression level
- Exclude certain content types (images, videos already compressed)

**Configuration**:
```kotlin
WizardApplication.configure {
    compression {
        enabled = true
        minimumSize = 1024  // Only compress if response > 1KB
        level = 6  // Compression level 1-9
        excludeMimeTypes = listOf("image/*", "video/*", "application/zip")
    }
}
```

---

### 4.9 Configuration Management

#### 4.9.1 Configuration File
**Description**: Single YAML configuration file with environment variable support

**Requirements**:
- Configuration file: `application.yml` in `src/main/resources/`
- Support environment variable substitution: `${ENV_VAR:default_value}`
- Default values for all settings
- Type-safe configuration access in code

**Example Configuration File** (`application.yml`):
```yaml
server:
  port: ${PORT:8090}
  host: ${HOST:0.0.0.0}
  contextPath: ${CONTEXT_PATH:/}

  threads:
    min: ${MIN_THREADS:10}
    max: ${MAX_THREADS:200}
    idleTimeout: ${IDLE_TIMEOUT:60000}  # milliseconds

database:
  primary:
    url: ${DB_URL:jdbc:mysql://localhost:3306/app_db}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    pool:
      maximumPoolSize: ${DB_POOL_SIZE:10}
      minimumIdle: ${DB_MIN_IDLE:5}

logging:
  level: ${LOG_LEVEL:INFO}
  pattern: ${LOG_PATTERN:%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n}
  file: ${LOG_FILE:logs/wizard.log}

auth:
  jwt:
    secret: ${JWT_SECRET:change-this-secret}
    expirationMinutes: ${JWT_EXPIRATION:60}

migrations:
  enabled: ${MIGRATIONS_ENABLED:true}
  autoRun: ${MIGRATIONS_AUTO_RUN:true}
```

#### 4.9.2 Port Configuration
**Description**: Configurable server port

**Requirements**:
- Default port: 8090
- Configurable via `application.yml`
- Overridable via environment variable `PORT`
- Programmatic configuration option

**API Example**:
```kotlin
// Via configuration file
WizardApplication.run()  // Uses port from application.yml

// Programmatic
WizardApplication.configure {
    server {
        port = 9000
    }
}.run()
```

#### 4.9.3 Thread Pool Configuration
**Description**: Configure thread pool for handling concurrent requests

**Requirements**:
- Configure minimum threads (always active)
- Configure maximum threads (peak load)
- Configure idle timeout for threads
- Sensible defaults for typical workloads
- No support for async/reactive requests (synchronous model only)

**Default Thread Pool Settings**:
```yaml
server:
  threads:
    min: 10      # Always keep 10 threads ready
    max: 200     # Scale up to 200 threads under load
    idleTimeout: 60000  # Kill idle threads after 60 seconds
```

#### 4.9.4 Logging Configuration
**Description**: Integrate with SLF4J for flexible logging

**Requirements**:
- Use SLF4J as logging facade
- Support multiple backends (Logback, Log4j2, java.util.logging)
- Configure log level per package
- Configure log format/pattern
- File and console logging
- Log rotation support

**Configuration**:
```yaml
logging:
  level: INFO
  packages:
    com.wizard: DEBUG
    com.zaxxer.hikari: WARN
  console:
    enabled: true
    pattern: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    enabled: true
    path: logs/wizard.log
    maxSize: 10MB
    maxHistory: 30  # Keep 30 days of logs
```

---

### 4.10 Operational Features

#### 4.10.1 Graceful Shutdown
**Description**: Properly handle server shutdown with in-flight requests

**Requirements**:
- Stop accepting new requests immediately on shutdown signal
- Wait for in-flight requests to complete
- Configurable shutdown timeout
- Force shutdown after timeout expires
- Close database connections gracefully
- Flush logs before exit
- Respond to SIGTERM and SIGINT signals

**Configuration**:
```yaml
server:
  shutdown:
    gracePeriod: 30000  # Wait up to 30 seconds for requests to complete
    forceAfter: 60000   # Force shutdown after 60 seconds
```

**Behavior**:
1. Receive shutdown signal (SIGTERM/SIGINT)
2. Stop accepting new connections
3. Wait for active requests to complete (up to gracePeriod)
4. Close database connection pools
5. Flush and close log files
6. Exit cleanly

#### 4.10.2 Health Check Endpoints
**Description**: Built-in endpoints for container orchestration

**Requirements**:
- `/health` - Overall health status
- `/health/live` - Liveness probe (is server running?)
- `/health/ready` - Readiness probe (can server handle requests?)
- Check database connectivity
- Check critical dependencies
- Configurable custom health checks
- Return 200 OK if healthy, 503 Service Unavailable if unhealthy

**Default Endpoints**:
```
GET /health
Response:
{
  "status": "UP",
  "timestamp": "2025-12-09T10:30:00Z",
  "checks": {
    "database": "UP",
    "diskSpace": "UP"
  }
}

GET /health/live
Response:
{
  "status": "UP"
}

GET /health/ready
Response:
{
  "status": "UP",
  "checks": {
    "database": "UP"
  }
}
```

**Custom Health Checks**:
```kotlin
WizardApplication.health {
    check("redis") {
        try {
            redis.ping()
            HealthStatus.UP
        } catch (e: Exception) {
            HealthStatus.DOWN
        }
    }

    check("externalAPI") {
        val response = httpClient.get("https://api.example.com/status")
        if (response.status == 200) HealthStatus.UP else HealthStatus.DOWN
    }
}
```

#### 4.10.3 Metrics and Monitoring
**Description**: Detailed metrics endpoint similar to ServiceNow stats.do

**Requirements**:
- Prometheus-compatible metrics format
- Built-in metrics: request count, latency, errors, throughput
- Per-route metrics
- Database connection pool metrics
- JVM metrics (memory, GC, threads)
- Custom application metrics support
- Configurable metrics retention

**Metrics Endpoint**:
```
GET /metrics

# Request metrics
http_requests_total{method="GET",path="/api/users",status="200"} 1523
http_requests_total{method="POST",path="/api/users",status="201"} 342
http_requests_total{method="GET",path="/api/users",status="500"} 5

http_request_duration_seconds{method="GET",path="/api/users",quantile="0.5"} 0.023
http_request_duration_seconds{method="GET",path="/api/users",quantile="0.95"} 0.156
http_request_duration_seconds{method="GET",path="/api/users",quantile="0.99"} 0.324

# Database metrics
database_connections_active{pool="primary"} 5
database_connections_idle{pool="primary"} 3
database_connections_total{pool="primary"} 8
database_query_duration_seconds{query="SELECT"} 0.012

# JVM metrics
jvm_memory_used_bytes{area="heap"} 268435456
jvm_memory_max_bytes{area="heap"} 1073741824
jvm_gc_collections_total{gc="G1 Young Generation"} 127
jvm_threads_current 45
```

**HTML Metrics Dashboard** (similar to ServiceNow stats.do):
```
GET /stats

Displays web interface with:
- Real-time request rate graph
- Response time percentiles (p50, p95, p99)
- Error rate graph
- Top slowest endpoints table
- Database connection pool status
- JVM memory usage graph
- Thread pool utilization
- Recent errors list
```

**Custom Metrics API**:
```kotlin
// Counter
val loginCounter = Metrics.counter("user_logins_total")
loginCounter.increment()

// Gauge
Metrics.gauge("queue_size") { messageQueue.size() }

// Histogram
val requestTimer = Metrics.histogram("custom_operation_duration")
requestTimer.record(duration)

// In route handler
WizardApplication.post("/api/login") { request, response ->
    Metrics.counter("login_attempts").increment()
    val result = authService.login(credentials)
    if (result.success) {
        Metrics.counter("login_success").increment()
    } else {
        Metrics.counter("login_failure").increment()
    }
    result
}
```

**Configuration**:
```yaml
metrics:
  enabled: true
  endpoint: /metrics
  dashboard: /stats
  includeJvmMetrics: true
  includeDatabaseMetrics: true
  retention:
    maxAge: 3600  # Keep metrics for 1 hour
```

---

## 5. Technical Requirements

### 5.1 Technology Stack
- **Language**: Kotlin 2.1.0+
- **JVM**: Java 11+ (LTS versions recommended)
- **Web Server**: Eclipse Jetty 11.x
- **Database**: JDBC with HikariCP connection pooling
- **JSON Library**: Jackson (for performance)
- **XML Library**: Jackson XML module
- **Logging**: SLF4J facade with Logback
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: JUnit Jupiter 5.x

### 5.2 Key Dependencies
```kotlin
dependencies {
    // Core web server
    implementation("org.eclipse.jetty:jetty-server:11.0.x")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.x")

    // Database
    implementation("com.zaxxer:HikariCP:5.x")
    implementation("mysql:mysql-connector-java:8.x")
    implementation("org.postgresql:postgresql:42.x")

    // JSON/XML
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.x")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.x")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.x")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.x")

    // Authentication
    implementation("com.auth0:java-jwt:4.x")  // JWT
    implementation("org.mindrot:jbcrypt:0.4")  // BCrypt

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.x")
    implementation("ch.qos.logback:logback-classic:1.4.x")

    // Metrics
    implementation("io.micrometer:micrometer-core:1.11.x")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.x")

    // YAML parsing
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.x")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.x")
}
```

### 5.3 Module Structure
```
wizard/
├── core/                    # Framework library
│   ├── http/               # HTTP routing, request/response
│   ├── database/           # Database support, migrations
│   ├── auth/               # Authentication framework
│   ├── config/             # Configuration management
│   ├── interceptor/        # Interceptor system
│   ├── metrics/            # Metrics and monitoring
│   └── util/               # Utilities
├── app/                     # Demo application
└── utils/                   # Shared utilities
```

---

## 6. Non-Functional Requirements

### 6.1 Performance
- **Request Handling**: Support 10,000+ requests per second on standard hardware
- **Response Time**: p95 < 50ms for simple routes, p99 < 100ms
- **Memory**: Efficient memory usage, < 500MB heap for typical workload
- **Database**: Connection pool should handle 1000+ queries per second
- **Startup Time**: Application startup < 5 seconds

### 6.2 Scalability
- **Concurrent Requests**: Handle 200+ concurrent requests per instance
- **Thread Pool**: Configurable thread pool supporting synchronous request model
- **Database Connections**: Efficient connection pooling with HikariCP
- **Horizontal Scaling**: Stateless design (except sessions) for easy scaling
- **Multi-Database**: Support connecting to multiple databases

### 6.3 Reliability
- **Error Handling**: Graceful error handling with proper HTTP status codes
- **Circuit Breakers**: (Future) Circuit breaker pattern for external dependencies
- **Health Checks**: Built-in health endpoints for monitoring
- **Graceful Shutdown**: Proper cleanup of resources on shutdown
- **Transaction Support**: ACID transactions with rollback on failure

### 6.4 Security
- **SQL Injection**: Parameterized queries prevent SQL injection
- **XSS**: Proper output encoding
- **Authentication**: Secure authentication with bcrypt password hashing
- **Session Security**: Secure session cookies (httpOnly, secure, sameSite)
- **HTTPS**: Support for SSL/TLS configuration
- **Secrets**: Environment variable support for sensitive configuration

### 6.5 Maintainability
- **Code Quality**: Clean, idiomatic Kotlin code
- **Documentation**: Comprehensive API documentation
- **Testing**: High test coverage (>80%)
- **Logging**: Structured logging for debugging
- **Error Messages**: Clear, actionable error messages

### 6.6 Usability
- **Simple API**: Intuitive, expressive APIs
- **Convention over Configuration**: Sensible defaults
- **Quick Start**: Get running with minimal code
- **Error Messages**: Developer-friendly error messages
- **Examples**: Comprehensive examples in documentation

---

## 7. Implementation Priorities

### Phase 1: Core Framework (P0)
1. HTTP routing and request handling
2. Request payload parsing (JSON, form data, multipart)
3. Response handling with content negotiation
4. Configuration management (YAML with env var support)
5. Interceptor system with priority
6. Basic error handling

**Success Criteria**: Can build a simple REST API with JSON payloads

### Phase 2: Database Support (P1)
1. HikariCP connection pooling
2. Raw SQL queries with parameter binding
3. Query builder DSL
4. Reflection-based result mapping
5. Transaction management
6. Multi-database support

**Success Criteria**: Can build CRUD applications with database persistence

### Phase 3: Database Migrations (P1)
1. YAML-based migration files
2. Migration tracking (WIZARD_CHANGELOG)
3. Automatic and manual execution
4. Rollback support
5. Multi-database migrations
6. Validation

**Success Criteria**: Can manage database schema evolution

### Phase 4: Authentication (P1)
1. Pluggable auth architecture
2. Basic Authentication
3. JWT authentication with token generation
4. Session-based authentication
5. BCrypt password hashing
6. Route protection with interceptors

**Success Criteria**: Can build secure APIs with authentication

### Phase 5: Additional Features (P1-P2)
1. Static file serving
2. Cookie handling
3. Session management
4. Response compression
5. Logging configuration
6. Thread pool configuration

**Success Criteria**: Production-ready web applications

### Phase 6: Operational Features (P1)
1. Graceful shutdown
2. Health check endpoints
3. Metrics collection
4. Prometheus endpoint
5. HTML metrics dashboard
6. Custom metrics API

**Success Criteria**: Observable, production-ready applications

---

## 8. Out of Scope

The following features are explicitly **not included** in this version:

- WebSocket support
- HTTP/2 support
- Built-in rate limiting
- Request validation framework
- Async/reactive programming model
- Built-in caching layer
- Template engines (Thymeleaf, FreeMarker, etc.)
- GraphQL support
- gRPC support
- Built-in API documentation generation (Swagger/OpenAPI)
- Scheduled tasks/cron jobs
- Message queue integration
- Email sending
- File storage abstraction

These may be considered for future versions based on user demand.

---

## 9. Success Metrics

### 9.1 Developer Experience
- Time to first working endpoint: < 5 minutes
- Lines of code for typical REST API: < 100 lines
- Developer satisfaction survey: > 4/5 stars

### 9.2 Performance Benchmarks
- Simple route (Hello World): > 50,000 req/sec
- JSON serialization route: > 20,000 req/sec
- Database query route: > 5,000 req/sec
- p95 latency: < 50ms
- p99 latency: < 100ms

### 9.3 Adoption Metrics
- GitHub stars: Track community interest
- Maven/Gradle downloads: Track usage
- Community contributions: PRs, issues, discussions

---

## 10. Documentation Requirements

### 10.1 Getting Started Guide
- Quick start tutorial
- Installation instructions
- First application example
- Configuration guide

### 10.2 API Documentation
- All public APIs documented with KDoc
- Code examples for each feature
- Best practices guide
- Migration guide (when updating versions)

### 10.3 Architecture Documentation
- Framework architecture overview
- Request lifecycle diagram
- Extension points for customization
- Performance tuning guide

### 10.4 Deployment Guide
- Docker deployment
- Kubernetes deployment
- Environment variable reference
- Production checklist

---

## 11. Appendix

### 11.1 Glossary
- **Handler**: Function that processes HTTP requests
- **Route**: Mapping of HTTP method + path to handler
- **Interceptor**: Middleware that runs before/after handlers
- **Provider**: Pluggable implementation of framework interface
- **Changeset**: Unit of database migration

### 11.2 References
- Eclipse Jetty: https://www.eclipse.org/jetty/
- HikariCP: https://github.com/brettwooldridge/HikariCP
- Jackson: https://github.com/FasterXML/jackson
- JWT: https://jwt.io/
- Prometheus: https://prometheus.io/

---

**End of Document**
