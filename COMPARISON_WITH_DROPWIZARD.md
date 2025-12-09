# Wizard vs Dropwizard: Feature Comparison

---

## Document Information
- **Created**: December 9, 2025
- **Purpose**: Compare Wizard framework features with Dropwizard
- **Version**: 1.0

---

## 1. Executive Overview

### 1.1 What is Dropwizard?

Dropwizard is a mature, production-proven Java framework for building RESTful web services. It bundles together stable libraries from the Java ecosystem:
- **Jetty** for HTTP server
- **Jersey** for REST/JAX-RS
- **Jackson** for JSON
- **Logback** for logging
- **Metrics** library for monitoring
- **Hibernate Validator** for validation
- **JDBI/Hibernate** for database access
- **Liquibase** for database migrations

**Key Characteristics**:
- Java-focused (with Scala support)
- Opinionated with sensible defaults
- Production-ready out of the box
- 8.6k+ GitHub stars, used by 13,800+ projects
- Mature ecosystem since 2011

### 1.2 What is Wizard?

Wizard is a lightweight Kotlin web framework for building scalable HTTP applications on the JVM. Built on Eclipse Jetty, it provides a simple, expressive API inspired by modern frameworks.

**Key Characteristics**:
- Kotlin-first (Java interoperable)
- Simple, minimal API surface
- Built-in database support with connection pooling
- YAML-based migrations similar to Liquibase
- Pluggable authentication framework
- New framework under active development

---

## 2. High-Level Comparison

| Aspect | Dropwizard | Wizard |
|--------|-----------|---------|
| **Primary Language** | Java (Scala support) | Kotlin (Java interop) |
| **First Release** | 2011 | 2025 (New) |
| **Web Server** | Jetty | Jetty |
| **REST Framework** | Jersey (JAX-RS) | Custom lightweight API |
| **JSON Library** | Jackson | Jackson |
| **Configuration** | YAML + Java classes | YAML with env vars |
| **Database Access** | JDBI3 / Hibernate ORM | Raw SQL + Query Builder DSL |
| **Connection Pool** | Apache DBCP / HikariCP | HikariCP (default) |
| **Migrations** | Liquibase | Custom YAML-based (Liquibase-inspired) |
| **Validation** | Hibernate Validator (JSR 380) | Not included |
| **Authentication** | Optional (via bundles) | Built-in (Basic, JWT, Session) |
| **Metrics** | Dropwizard Metrics (Codahale) | Micrometer + Prometheus |
| **Health Checks** | Built-in (/healthcheck) | Built-in (/health, /health/live, /health/ready) |
| **Logging** | Logback (SLF4J) | Logback (SLF4J) |
| **Dependency Injection** | Constructor-based (no DI container) | Not included |
| **Testing Support** | Comprehensive (DropwizardTestSupport) | Standard JUnit5 |
| **API Style** | Annotation-based (JAX-RS) | Lambda/Function-based |
| **Admin Interface** | Separate admin port | Single port with dedicated routes |
| **Maturity** | Very mature, battle-tested | New, under development |
| **Community** | Large (8.6k stars, 13.8k users) | Growing |

---

## 3. Detailed Feature Comparison

### 3.1 HTTP & REST API

#### Route Definition Style

**Dropwizard (JAX-RS/Jersey)**:
```java
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public List<User> listUsers() {
        return userService.findAll();
    }

    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return userService.findById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(@Valid User user) {
        User created = userService.create(user);
        return Response.status(201).entity(created).build();
    }
}
```

**Wizard**:
```kotlin
WizardApplication.get("/users") { request, response ->
    userService.findAll()
}

WizardApplication.get("/users/{id}") { request, response ->
    val id = request.pathParam("id").toLong()
    userService.findById(id)
}

WizardApplication.post("/users") { request, response ->
    val user = request.body<User>()
    response.status = 201
    userService.create(user)
}
```

**Comparison**:
| Feature | Dropwizard | Wizard | Winner |
|---------|-----------|---------|--------|
| Registration style | Annotation-based (JAX-RS) | Lambda/function-based | Tie (preference) |
| Boilerplate | More (classes, annotations) | Less (inline lambdas) | **Wizard** |
| IDE autocomplete | Excellent (standard JAX-RS) | Good (Kotlin DSL) | **Dropwizard** |
| Learning curve | Steeper (JAX-RS spec) | Gentler (simple API) | **Wizard** |
| Standards compliance | JAX-RS standard | Custom API | **Dropwizard** |

#### Path Parameters & Query Parameters

**Dropwizard**:
```java
@GET
@Path("/{userId}/posts/{postId}")
public Post getPost(
    @PathParam("userId") Long userId,
    @PathParam("postId") Long postId,
    @QueryParam("format") @DefaultValue("json") String format) {
    // Implementation
}
```

**Wizard**:
```kotlin
WizardApplication.get("/users/{userId}/posts/{postId}") { request, response ->
    val userId = request.pathParam("userId").toLong()
    val postId = request.pathParam("postId").toLong()
    val format = request.queryParam("format") ?: "json"
    // Implementation
}
```

**Comparison**: Both frameworks support path and query parameters effectively. Dropwizard has more type safety via annotations.

#### Content Negotiation

**Dropwizard**:
- Built-in via JAX-RS `@Produces` and `@Consumes` annotations
- Automatic based on Accept header
- Supports JSON, XML, HTML, custom media types

**Wizard**:
- Built-in via Accept header checking
- Supports JSON, XML, plain text, HTML
- Custom serialization rules configurable

**Winner**: **Tie** - Both handle content negotiation well

---

### 3.2 Request/Response Handling

#### Request Body Parsing

**Dropwizard**:
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response createUser(@Valid User user) {
    // user is automatically deserialized from JSON
    // @Valid triggers Hibernate Validator
    return Response.ok(userService.create(user)).build();
}
```

**Wizard**:
```kotlin
WizardApplication.post("/users") { request, response ->
    val user = request.body<User>()  // Auto-deserialized
    // Validation not built-in, must be manual
    response.status = 201
    userService.create(user)
}
```

**Comparison**:
- **Dropwizard**: Automatic JSON/XML parsing, built-in validation via `@Valid`
- **Wizard**: Auto-detection based on Content-Type, typed parsing, no built-in validation

**Winner**: **Dropwizard** (built-in validation is significant)

#### Multipart File Upload

**Dropwizard**:
```java
@POST
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Response upload(
    @FormDataParam("file") InputStream fileInputStream,
    @FormDataParam("file") FormDataContentDisposition fileMetaData) {
    // Handle file upload
}
```

**Wizard**:
```kotlin
WizardApplication.post("/upload") { request, response ->
    val file = request.file("attachment")
    val description = request.formParam("description")
    // Handle file upload
}
```

**Comparison**: Both support multipart uploads. Wizard's API is simpler.

---

### 3.3 Validation

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Built-in Validation** | ✅ Yes (Hibernate Validator) | ❌ No |
| **JSR-380 Support** | ✅ Yes | ❌ No |
| **Custom Validators** | ✅ Yes | ❌ No (manual) |
| **Annotation-based** | ✅ `@NotNull`, `@Min`, `@Email`, etc. | ❌ N/A |
| **Error Messages** | ✅ Automatic 422 with details | ❌ Manual |

**Example - Dropwizard**:
```java
public class User {
    @NotNull
    @Length(min = 2, max = 255)
    private String name;

    @Email
    private String email;

    @Min(18)
    private int age;
}

@POST
public Response create(@Valid User user) {
    // Automatic validation, returns 422 if invalid
}
```

**Example - Wizard**:
```kotlin
data class User(val name: String, val email: String, val age: Int)

WizardApplication.post("/users") { request, response ->
    val user = request.body<User>()

    // Manual validation required
    if (user.name.length < 2) {
        response.status = 422
        return@post mapOf("error" to "Name too short")
    }

    userService.create(user)
}
```

**Winner**: **Dropwizard** - Built-in validation is a major productivity feature

---

### 3.4 Database Features

#### Database Access Approaches

**Dropwizard**:
Offers two options:
1. **JDBI3** - SQL-first, lightweight ORM
2. **Hibernate ORM** - Full-featured JPA implementation

**JDBI3 Example**:
```java
@RegisterBeanMapper(User.class)
public interface UserDao {
    @SqlQuery("SELECT * FROM users WHERE id = :id")
    User findById(@Bind("id") long id);

    @SqlUpdate("INSERT INTO users (name, email) VALUES (:name, :email)")
    @GetGeneratedKeys
    long insert(@BindBean User user);
}
```

**Hibernate Example**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;
}

// Usage
User user = session.get(User.class, userId);
```

**Wizard**:
Two approaches:
1. **Raw SQL** with named parameters
2. **Query Builder DSL** for type-safe queries

**Raw SQL Example**:
```kotlin
val users = db.query<User>(
    "SELECT * FROM users WHERE age > :age",
    mapOf("age" to 18)
)

val userId = db.insert(
    "INSERT INTO users (name, email) VALUES (:name, :email)",
    mapOf("name" to "John", "email" to "john@example.com")
)
```

**Query Builder Example**:
```kotlin
val users = db.select<User>()
    .from("users")
    .where("age", ">", 18)
    .and("status", "=", "active")
    .orderBy("created_at", DESC)
    .execute()
```

#### Database Feature Comparison

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **ORM Support** | ✅ Yes (Hibernate) | ❌ No |
| **SQL-first approach** | ✅ Yes (JDBI3) | ✅ Yes (Raw SQL) |
| **Query Builder** | ⚠️ Partial (JDBI3 fluent API) | ✅ Yes (Full DSL) |
| **Entity Mapping** | ✅ JPA annotations | ✅ Reflection-based |
| **Relationships** | ✅ @OneToMany, @ManyToOne, etc. | ❌ Manual joins |
| **Transactions** | ✅ @Transactional (Hibernate) | ✅ @Transactional + programmatic |
| **Connection Pool** | ✅ HikariCP / Apache DBCP | ✅ HikariCP (default) |
| **Lazy Loading** | ✅ Yes (Hibernate) | ❌ No |
| **Caching** | ✅ Second-level cache (Hibernate) | ❌ No |
| **Multi-database** | ⚠️ Possible but complex | ✅ Built-in with routing rules |

**Winner**: **Dropwizard** for complex applications needing ORM; **Wizard** for simple SQL-first applications

#### Database Migrations

**Dropwizard (Liquibase)**:
```xml
<databaseChangeLog>
    <changeSet id="1" author="dev">
        <createTable tableName="users">
            <column name="id" type="bigint" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="name" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

Migrations run via command:
```bash
java -jar myapp.jar db migrate config.yml
```

**Wizard (Custom YAML)**:
```yaml
version: 001
description: Create users table
database: ALL

changesets:
  - id: 1
    author: developer
    changes:
      - sql: |
          CREATE TABLE users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL
          )
    rollback:
      - sql: DROP TABLE users
```

Migrations run automatically on startup or via Gradle task.

| Feature | Dropwizard (Liquibase) | Wizard |
|---------|----------------------|---------|
| **Format** | XML, YAML, SQL, JSON | YAML with inline SQL |
| **Maturity** | Very mature, widely used | New |
| **Rollback Support** | ✅ Yes | ✅ Yes |
| **Change tracking** | ✅ DATABASECHANGELOG | ✅ WIZARD_CHANGELOG |
| **Checksum validation** | ✅ Yes | ✅ Yes |
| **Multi-database** | ✅ Possible | ✅ Built-in (database property) |
| **Auto-run on startup** | ⚠️ Via command | ✅ Configurable |
| **Diff generation** | ✅ Yes | ❌ No |
| **Preconditions** | ✅ Yes | ❌ No |

**Winner**: **Dropwizard** (Liquibase is more mature and feature-rich)

---

### 3.5 Authentication & Security

#### Authentication Support

**Dropwizard**:
- Authentication is **optional** and added via bundles
- No built-in auth (must add dependencies)
- Common bundles: dropwizard-auth for Basic/OAuth

**Example**:
```java
// Add bundle
bootstrap.addBundle(new AuthBundle<>());

// Create authenticator
public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
    @Override
    public Optional<User> authenticate(BasicCredentials credentials) {
        if ("secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername()));
        }
        return Optional.empty();
    }
}

// Apply to resources
@GET
@Auth
public String getProtected(@Auth User user) {
    return "Hello " + user.getName();
}
```

**Wizard**:
- Authentication is **built-in** to the framework
- Three strategies out of the box: Basic, JWT, Session
- Interceptor-based with route pattern matching

**Example**:
```kotlin
// Configure JWT
WizardApplication.configure {
    auth {
        jwt {
            secret = "your-secret"
            expirationMinutes = 60
        }
    }
}

// Protect routes
WizardApplication.auth()
    .jwt()
    .routes("/api/**")

// Access user in handler
WizardApplication.get("/api/profile") { request, response ->
    val user = request.user<User>()
    "Hello ${user.name}"
}
```

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Built-in Auth** | ❌ No (via bundles) | ✅ Yes |
| **Basic Auth** | ✅ Via dropwizard-auth | ✅ Built-in |
| **JWT** | ⚠️ Third-party libraries | ✅ Built-in |
| **OAuth** | ⚠️ Third-party bundles | ❌ No |
| **Session-based** | ⚠️ Manual setup | ✅ Built-in |
| **Route Protection** | `@Auth` annotation | Interceptor with patterns |
| **Password Hashing** | ⚠️ Manual (use bcrypt lib) | ✅ Built-in (bcrypt) |
| **Multiple Strategies** | ⚠️ Possible | ✅ Built-in |

**Winner**: **Wizard** for built-in support; **Dropwizard** for flexibility and OAuth support

---

### 3.6 Configuration Management

#### Configuration File Format

**Dropwizard**:
```yaml
# config.yml
server:
  applicationConnectors:
    - type: http
      port: 8080
  adminConnectors:
    - type: http
      port: 8081

database:
  driverClass: org.postgresql.Driver
  user: postgres
  password: secret
  url: jdbc:postgresql://localhost:5432/mydb

logging:
  level: INFO
  appenders:
    - type: console
```

```java
// Configuration class
public class MyConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDatabase() {
        return database;
    }
}
```

**Wizard**:
```yaml
# application.yml
server:
  port: ${PORT:8090}

database:
  primary:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:secret}

logging:
  level: ${LOG_LEVEL:INFO}
```

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Format** | YAML | YAML |
| **Type-safe Config** | ✅ Java classes | ✅ Programmatic access |
| **Environment Variables** | ⚠️ Via substitution | ✅ Built-in `${VAR:default}` |
| **Default Values** | ⚠️ In Java class | ✅ In YAML file |
| **Validation** | ✅ Hibernate Validator | ❌ No |
| **Separate Admin Port** | ✅ Yes | ❌ No (single port) |
| **Command-line Overrides** | ✅ Yes (`dw.` prefix) | ⚠️ Via env vars |
| **Configuration Objects** | ✅ Strongly typed | ⚠️ Less structured |

**Winner**: **Dropwizard** (more structured, validated, separate admin port)

---

### 3.7 Metrics & Monitoring

#### Metrics Libraries

**Dropwizard**:
- Uses **Dropwizard Metrics** (formerly Codahale Metrics)
- Industry standard library
- Annotations: `@Timed`, `@Metered`, `@ExceptionMetered`
- Admin endpoint: `http://localhost:8081/metrics` (admin port)

**Wizard**:
- Uses **Micrometer** with Prometheus registry
- Modern, vendor-neutral metrics facade
- Programmatic metrics API
- Endpoints: `/metrics` (Prometheus format), `/stats` (HTML dashboard)

#### Metrics Features

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Metrics Library** | Dropwizard Metrics | Micrometer |
| **Prometheus Format** | ⚠️ Via reporter | ✅ Native support |
| **Annotation-based** | ✅ `@Timed`, `@Metered` | ❌ No |
| **Programmatic API** | ✅ Yes | ✅ Yes |
| **JVM Metrics** | ✅ Yes | ✅ Yes |
| **Database Metrics** | ⚠️ Via instrumentation | ✅ Built-in (HikariCP) |
| **Per-route Metrics** | ✅ Automatic | ✅ Automatic |
| **HTML Dashboard** | ⚠️ Third-party | ✅ Built-in `/stats` |
| **Admin Port** | ✅ Separate port 8081 | ❌ Single port |

**Dropwizard Example**:
```java
@GET
@Timed
@Metered
public Response getUsers() {
    return Response.ok(userService.findAll()).build();
}

// Access: http://localhost:8081/metrics
```

**Wizard Example**:
```kotlin
WizardApplication.get("/users") { request, response ->
    Metrics.counter("user_requests").increment()
    userService.findAll()
}

// Access: http://localhost:8090/metrics (Prometheus)
// Access: http://localhost:8090/stats (HTML)
```

**Winner**: **Tie** - Dropwizard has annotations, Wizard has modern Micrometer + dashboard

---

### 3.8 Health Checks

#### Health Check Implementation

**Dropwizard**:
```java
public class DatabaseHealthCheck extends HealthCheck {
    private final Database database;

    @Override
    protected Result check() throws Exception {
        if (database.isConnected()) {
            return Result.healthy();
        }
        return Result.unhealthy("Cannot connect to database");
    }
}

// Register
environment.healthChecks().register("database", new DatabaseHealthCheck(database));

// Access: http://localhost:8081/healthcheck
```

**Wizard**:
```kotlin
WizardApplication.health {
    check("database") {
        if (db.testConnection()) {
            HealthStatus.UP
        } else {
            HealthStatus.DOWN
        }
    }
}

// Access: http://localhost:8090/health
// Access: http://localhost:8090/health/live
// Access: http://localhost:8090/health/ready
```

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Health Checks** | ✅ Yes | ✅ Yes |
| **Liveness Probe** | ⚠️ Manual | ✅ Built-in `/health/live` |
| **Readiness Probe** | ⚠️ Manual | ✅ Built-in `/health/ready` |
| **Custom Checks** | ✅ Extend HealthCheck | ✅ Lambda-based |
| **Database Check** | ⚠️ Manual | ✅ Automatic |
| **Admin Port** | ✅ Separate port | ❌ Single port |
| **Critical vs Non-critical** | ✅ Configurable | ⚠️ Not mentioned |
| **Health Listeners** | ✅ Yes | ❌ No |

**Winner**: **Wizard** for Kubernetes-ready probes; **Dropwizard** for advanced features

---

### 3.9 Operational Features

#### Graceful Shutdown

**Dropwizard**:
- Built-in graceful shutdown
- Configurable shutdown time
- Responds to SIGTERM

**Wizard**:
- Built-in graceful shutdown
- Configurable grace period
- Closes database connections
- Flushes logs

**Winner**: **Tie** - Both support graceful shutdown

#### Administrative Tasks

**Dropwizard**:
```java
public class CustomTask extends Task {
    public CustomTask() {
        super("custom-task");
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) {
        output.println("Task executed");
    }
}

// Access: POST http://localhost:8081/tasks/custom-task
```

**Wizard**:
- No built-in task system
- Can be implemented via regular endpoints

**Winner**: **Dropwizard** (built-in admin tasks)

#### Logging

| Feature | Dropwizard | Wizard |
|---------|-----------|---------|
| **Logging Library** | Logback (SLF4J) | Logback (SLF4J) |
| **Log Rotation** | ✅ Yes | ✅ Yes |
| **Runtime Level Change** | ✅ Via admin endpoint | ⚠️ Manual |
| **Async Logging** | ✅ Yes | ✅ Yes |
| **JSON Logging** | ✅ Via bundle | ⚠️ Manual config |
| **Request Logging** | ✅ Separate log | ✅ Configurable |
| **Multiple Appenders** | ✅ Console, file, syslog | ✅ Console, file |

**Winner**: **Dropwizard** (runtime log level change via admin endpoint)

---

## 4. Feature Summary Scorecard

### Dropwizard Wins
✅ **Request Validation** - Built-in Hibernate Validator
✅ **ORM Support** - Hibernate with relationships, lazy loading
✅ **Database Migrations** - Mature Liquibase with preconditions
✅ **Configuration** - Structured, validated configuration classes
✅ **Admin Interface** - Separate admin port for ops
✅ **JAX-RS Standard** - Industry standard REST API
✅ **Testing Support** - Comprehensive test utilities
✅ **Administrative Tasks** - Built-in task system
✅ **Maturity** - Battle-tested since 2011
✅ **Ecosystem** - Large community, many plugins

### Wizard Wins
✅ **Kotlin-First** - Idiomatic Kotlin API with lambdas
✅ **Built-in Authentication** - Basic, JWT, Session out-of-box
✅ **Simpler API** - Less boilerplate, faster to learn
✅ **Multi-Database** - Built-in routing rules
✅ **Query Builder DSL** - Type-safe query builder
✅ **Environment Variables** - First-class `${VAR:default}` support
✅ **Kubernetes Health Checks** - `/health/live` and `/health/ready` built-in
✅ **HTML Metrics Dashboard** - Built-in `/stats` endpoint
✅ **Modern Metrics** - Micrometer with Prometheus
✅ **Password Hashing** - Built-in bcrypt utilities

### Tied Features
🤝 **Web Server** - Both use Jetty
🤝 **JSON** - Both use Jackson
🤝 **Logging** - Both use Logback/SLF4J
🤝 **Connection Pool** - Both support HikariCP
🤝 **Health Checks** - Both have health check systems
🤝 **Graceful Shutdown** - Both support clean shutdown
🤝 **Content Negotiation** - Both handle multiple formats

---

## 5. When to Choose Each Framework

### Choose Dropwizard if:

1. **Enterprise Java Environment**
   - Team is primarily Java-focused
   - Need JAX-RS standard compliance
   - Want extensive enterprise library support

2. **Complex Domain Models**
   - Need ORM with relationships
   - Require lazy loading and caching
   - Complex entity hierarchies

3. **Request Validation is Critical**
   - Need declarative validation
   - Want automatic error responses
   - Require custom validators

4. **Mature, Proven Solution**
   - Production stability is paramount
   - Need extensive documentation
   - Want large community support

5. **Advanced Operations**
   - Separate admin interface required
   - Need runtime configuration changes
   - Want administrative tasks system

### Choose Wizard if:

1. **Kotlin Development**
   - Team prefers Kotlin
   - Want idiomatic Kotlin APIs
   - Using Kotlin coroutines (future)

2. **Simple, Lightweight Services**
   - Microservices architecture
   - SQL-first database approach
   - Don't need full ORM

3. **Quick Development**
   - Rapid prototyping
   - Minimal boilerplate
   - Fast learning curve

4. **Built-in Auth Required**
   - Need JWT/Session auth immediately
   - Don't want to configure auth bundles
   - Want route-based protection

5. **Modern Cloud-Native**
   - Kubernetes deployment
   - Prometheus metrics
   - Need liveness/readiness probes

6. **Multi-Database Applications**
   - Connecting to multiple databases
   - Need route-based database routing
   - Microservices with separate DBs

---

## 6. Migration Considerations

### From Dropwizard to Wizard

**Easier to Migrate**:
- Routes (mapping JAX-RS to lambdas)
- Configuration (YAML to YAML with env vars)
- Health checks (similar concept)
- Metrics (both support similar metrics)

**Harder to Migrate**:
- Database layer (Hibernate/JDBI to Raw SQL/DSL)
- Validation (need manual validation)
- Dependency injection (if using DI)
- Administrative tasks (no built-in equivalent)

**Example Migration**:

**Dropwizard**:
```java
@Path("/users")
public class UserResource {
    @GET
    public List<User> list() {
        return dao.findAll();
    }
}
```

**Wizard**:
```kotlin
WizardApplication.get("/users") { request, response ->
    db.query<User>("SELECT * FROM users")
}
```

### From Wizard to Dropwizard

**Easier to Migrate**:
- Raw SQL can stay as JDBI
- Configuration structure
- Basic routes

**Harder to Migrate**:
- Lambda-based routes to JAX-RS classes
- Built-in auth to bundle-based auth
- Multi-database routing

---

## 7. Overall Recommendation

### For New Projects

| Scenario | Recommendation | Reason |
|----------|---------------|---------|
| **Enterprise Java shop** | Dropwizard | Mature, standard-compliant, extensive support |
| **Kotlin microservices** | Wizard | Simpler, Kotlin-native, faster development |
| **Complex domain** | Dropwizard | ORM support, validation, relationships |
| **Simple CRUD APIs** | Wizard | Less boilerplate, easier to learn |
| **Auth-heavy app** | Wizard | Built-in auth strategies |
| **Need validation** | Dropwizard | Hibernate Validator is powerful |
| **Kubernetes deployment** | Wizard | Better health check endpoints |
| **Multi-database** | Wizard | Built-in routing support |

### Hybrid Approach?

You can **partially combine** both:
- Use Dropwizard's JDBI with Wizard's routing (if Wizard exposes Jetty)
- Use Dropwizard with Kotlin (Dropwizard is Kotlin-compatible)
- Add validation library to Wizard manually

However, **using both frameworks together is not recommended** due to overlapping concerns.

---

## 8. Conclusion

### Key Takeaways

**Dropwizard** is a **mature, production-proven** framework ideal for **enterprise Java applications** requiring **ORM, validation, and extensive operational tools**. It follows JAX-RS standards and has a large ecosystem.

**Wizard** is a **modern, lightweight** Kotlin framework for **rapid development** of **cloud-native microservices**. It provides **built-in authentication**, **simpler APIs**, and **better Kubernetes support**, but lacks ORM and validation.

### Feature Gaps in Wizard (vs Dropwizard)

To be competitive with Dropwizard, Wizard should consider adding:

1. **Request Validation Framework** - Consider Hibernate Validator integration
2. **Testing Utilities** - Test support similar to DropwizardTestSupport
3. **Admin Port/Tasks** - Separate operational interface
4. **ORM Support** - Optional Hibernate integration for complex domains
5. **More Bundles** - Plugin system for extensions
6. **Runtime Config Changes** - Dynamic log level adjustment
7. **OAuth Support** - Add OAuth 2.0 to auth framework
8. **Dependency Injection** - Optional DI container

### Strengths to Maintain in Wizard

Wizard's unique advantages:

1. ✅ Kotlin-first design with idiomatic APIs
2. ✅ Built-in authentication (huge time-saver)
3. ✅ Simpler, lambda-based routing
4. ✅ Multi-database with routing rules
5. ✅ Modern metrics (Micrometer/Prometheus)
6. ✅ Kubernetes-ready health probes
7. ✅ Environment variable support

### Final Verdict

**Both frameworks are excellent choices**, but for different use cases:

- **Dropwizard** = Enterprise, mature, feature-complete
- **Wizard** = Modern, simple, Kotlin-native, rapid development

Choose based on your team's language preference, complexity needs, and operational requirements.

---

**End of Comparison Document**
