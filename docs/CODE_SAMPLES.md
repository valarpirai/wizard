# Wizard Framework - Code Samples

---

## Document Information
- **Created**: December 9, 2025
- **Purpose**: Comprehensive code examples for Wizard framework features
- **Version**: 1.0

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [HTTP Routing](#2-http-routing)
3. [Request Handling](#3-request-handling)
4. [Response Handling](#4-response-handling)
5. [Interceptors](#5-interceptors)
6. [Database Operations](#6-database-operations)
7. [Authentication](#7-authentication)
8. [Configuration](#8-configuration)
9. [Complete Applications](#9-complete-applications)

---

## 1. Quick Start

### 1.1 Minimal Application

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/") { request, response ->
        "Hello, Wizard!"
    }

    WizardApplication.get("/health") { request, response ->
        response.status = 200
        mapOf("status" to "healthy")
    }

    app.run()  // Starts server on port 8090
}
```

**Run the application**:
```bash
./gradlew run
```

**Test the endpoints**:
```bash
curl http://localhost:8090/
# Output: Hello, Wizard!

curl http://localhost:8090/health
# Output: {"status":"healthy"}
```

### 1.2 Hello World with JSON

```kotlin
import com.wizard.WizardApplication

data class Greeting(val message: String, val timestamp: Long)

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/greet/{name}") { request, response ->
        val name = request.pathParam("name")
        Greeting(
            message = "Hello, $name!",
            timestamp = System.currentTimeMillis()
        )
    }

    app.run()
}
```

**Test**:
```bash
curl http://localhost:8090/greet/John
# Output: {"message":"Hello, John!","timestamp":1702123456789}
```

---

## 2. HTTP Routing

### 2.1 All HTTP Methods

```kotlin
import com.wizard.WizardApplication

data class Item(val id: Int, val name: String, val price: Double)

fun main() {
    val app = WizardApplication()

    // GET - Retrieve items
    WizardApplication.get("/items") { request, response ->
        listOf(
            Item(1, "Laptop", 999.99),
            Item(2, "Mouse", 29.99)
        )
    }

    // GET - Retrieve single item
    WizardApplication.get("/items/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        Item(id, "Laptop", 999.99)
    }

    // POST - Create item
    WizardApplication.post("/items") { request, response ->
        val item = request.body<Item>()
        response.status = 201
        item.copy(id = 3) // Return with new ID
    }

    // PUT - Update entire item
    WizardApplication.put("/items/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val item = request.body<Item>()
        item.copy(id = id)
    }

    // PATCH - Partial update
    WizardApplication.patch("/items/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val updates = request.body<Map<String, Any>>()
        mapOf("id" to id, "updated" to updates)
    }

    // DELETE - Remove item
    WizardApplication.delete("/items/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        response.status = 204 // No Content
        null
    }

    app.run()
}
```

### 2.2 Path Parameters

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    // Single path parameter
    WizardApplication.get("/users/{userId}") { request, response ->
        val userId = request.pathParam("userId")
        mapOf("userId" to userId, "name" to "John Doe")
    }

    // Multiple path parameters
    WizardApplication.get("/users/{userId}/posts/{postId}") { request, response ->
        val userId = request.pathParam("userId")
        val postId = request.pathParam("postId")
        mapOf(
            "userId" to userId,
            "postId" to postId,
            "title" to "Sample Post"
        )
    }

    // Type conversion
    WizardApplication.get("/products/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val quantity = request.queryParam("qty")?.toIntOrNull() ?: 1

        mapOf(
            "productId" to id,
            "quantity" to quantity,
            "total" to (id * 10.99 * quantity)
        )
    }

    app.run()
}
```

**Test**:
```bash
curl http://localhost:8090/users/123/posts/456
# Output: {"userId":"123","postId":"456","title":"Sample Post"}

curl "http://localhost:8090/products/5?qty=3"
# Output: {"productId":5,"quantity":3,"total":164.85}
```

### 2.3 Query Parameters

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/search") { request, response ->
        val query = request.queryParam("q") ?: ""
        val page = request.queryParam("page")?.toInt() ?: 1
        val limit = request.queryParam("limit")?.toInt() ?: 10
        val sort = request.queryParam("sort") ?: "name"
        val order = request.queryParam("order") ?: "asc"

        mapOf(
            "query" to query,
            "page" to page,
            "limit" to limit,
            "sort" to sort,
            "order" to order,
            "results" to listOf("Item 1", "Item 2", "Item 3")
        )
    }

    // Multiple values for same parameter
    WizardApplication.get("/filter") { request, response ->
        val categories = request.queryParams("category") // Returns List<String>
        val minPrice = request.queryParam("minPrice")?.toDouble()
        val maxPrice = request.queryParam("maxPrice")?.toDouble()

        mapOf(
            "categories" to categories,
            "priceRange" to mapOf(
                "min" to minPrice,
                "max" to maxPrice
            )
        )
    }

    app.run()
}
```

**Test**:
```bash
curl "http://localhost:8090/search?q=laptop&page=2&limit=20&sort=price&order=desc"
# Output: {"query":"laptop","page":2,"limit":20,"sort":"price","order":"desc","results":["Item 1","Item 2","Item 3"]}

curl "http://localhost:8090/filter?category=electronics&category=computers&minPrice=100&maxPrice=1000"
# Output: {"categories":["electronics","computers"],"priceRange":{"min":100.0,"max":1000.0}}
```

---

## 3. Request Handling

### 3.1 JSON Request Body

```kotlin
import com.wizard.WizardApplication
import java.time.LocalDateTime

data class CreateUserRequest(
    val username: String,
    val email: String,
    val age: Int
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val age: Int,
    val createdAt: LocalDateTime
)

fun main() {
    val app = WizardApplication()

    WizardApplication.post("/users") { request, response ->
        // Auto-parse JSON body to typed object
        val userRequest = request.body<CreateUserRequest>()

        // Create user
        val user = User(
            id = 123,
            username = userRequest.username,
            email = userRequest.email,
            age = userRequest.age,
            createdAt = LocalDateTime.now()
        )

        response.status = 201
        user
    }

    app.run()
}
```

**Test**:
```bash
curl -X POST http://localhost:8090/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "age": 30
  }'
# Output: {"id":123,"username":"johndoe","email":"john@example.com","age":30,"createdAt":"2025-12-09T12:30:45"}
```

### 3.2 Form Data

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    WizardApplication.post("/contact") { request, response ->
        val name = request.formParam("name")
        val email = request.formParam("email")
        val message = request.formParam("message")

        mapOf(
            "received" to true,
            "data" to mapOf(
                "name" to name,
                "email" to email,
                "message" to message
            )
        )
    }

    app.run()
}
```

**Test**:
```bash
curl -X POST http://localhost:8090/contact \
  -d "name=John Doe" \
  -d "email=john@example.com" \
  -d "message=Hello"
# Output: {"received":true,"data":{"name":"John Doe","email":"john@example.com","message":"Hello"}}
```

### 3.3 File Upload (Multipart)

```kotlin
import com.wizard.WizardApplication
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val app = WizardApplication()

    WizardApplication.post("/upload") { request, response ->
        val file = request.file("attachment")
        val description = request.formParam("description")

        // Save file
        val uploadDir = Paths.get("uploads")
        Files.createDirectories(uploadDir)

        val savedPath = uploadDir.resolve(file.filename)
        Files.copy(file.inputStream, savedPath)

        mapOf(
            "uploaded" to true,
            "filename" to file.filename,
            "size" to file.size,
            "contentType" to file.contentType,
            "description" to description,
            "path" to savedPath.toString()
        )
    }

    app.run()
}
```

**Test**:
```bash
curl -X POST http://localhost:8090/upload \
  -F "attachment=@document.pdf" \
  -F "description=Important document"
# Output: {"uploaded":true,"filename":"document.pdf","size":12345,"contentType":"application/pdf","description":"Important document","path":"uploads/document.pdf"}
```

### 3.4 Request Headers

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/info") { request, response ->
        val userAgent = request.header("User-Agent")
        val accept = request.header("Accept")
        val authorization = request.header("Authorization")
        val customHeader = request.header("X-Custom-Header")

        mapOf(
            "userAgent" to userAgent,
            "accept" to accept,
            "hasAuth" to (authorization != null),
            "customHeader" to customHeader,
            "allHeaders" to request.headers()
        )
    }

    app.run()
}
```

**Test**:
```bash
curl http://localhost:8090/info \
  -H "X-Custom-Header: MyValue" \
  -H "Authorization: Bearer token123"
```

### 3.5 Cookies

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/session-info") { request, response ->
        val sessionId = request.cookie("SESSION_ID")
        val preferences = request.cookie("user_preferences")

        mapOf(
            "sessionId" to sessionId,
            "preferences" to preferences,
            "allCookies" to request.cookies()
        )
    }

    app.run()
}
```

---

## 4. Response Handling

### 4.1 JSON Responses (Auto-serialization)

```kotlin
import com.wizard.WizardApplication
import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class Product(val id: Int, val name: String, val price: Double)

fun main() {
    val app = WizardApplication()

    // Automatic JSON serialization
    WizardApplication.get("/products/{id}") { request, response ->
        val id = request.pathParam("id").toInt()

        val product = Product(id, "Laptop", 999.99)
        ApiResponse(
            success = true,
            data = product
        )
    }

    app.run()
}
```

### 4.2 Custom Status Codes

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    // 201 Created
    WizardApplication.post("/users") { request, response ->
        response.status = 201
        mapOf("id" to 123, "created" to true)
    }

    // 204 No Content
    WizardApplication.delete("/users/{id}") { request, response ->
        response.status = 204
        null // Empty body
    }

    // 404 Not Found
    WizardApplication.get("/users/{id}") { request, response ->
        val id = request.pathParam("id").toInt()

        if (id > 100) {
            response.status = 404
            mapOf("error" to "User not found")
        } else {
            mapOf("id" to id, "name" to "John")
        }
    }

    // 400 Bad Request
    WizardApplication.post("/validate") { request, response ->
        val age = request.body<Map<String, Int>>()["age"]

        if (age == null || age < 18) {
            response.status = 400
            mapOf("error" to "Age must be 18 or older")
        } else {
            mapOf("valid" to true)
        }
    }

    app.run()
}
```

### 4.3 Custom Response Headers

```kotlin
import com.wizard.WizardApplication
import java.time.Duration

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/api/data") { request, response ->
        // Set custom headers
        response.header("X-API-Version", "1.0")
        response.header("X-Request-ID", request.id())
        response.header("Cache-Control", "public, max-age=3600")
        response.header("X-Rate-Limit-Remaining", "99")

        mapOf("data" to "some data")
    }

    app.run()
}
```

### 4.4 Setting Cookies

```kotlin
import com.wizard.WizardApplication
import java.time.Duration

data class CookieOptions(
    val maxAge: Duration? = null,
    val path: String = "/",
    val domain: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = true,
    val sameSite: SameSite = SameSite.Lax
)

enum class SameSite { Strict, Lax, None }

fun main() {
    val app = WizardApplication()

    WizardApplication.post("/login") { request, response ->
        val credentials = request.body<Map<String, String>>()

        // Set session cookie
        response.cookie("SESSION_ID", "abc123", CookieOptions(
            maxAge = Duration.ofHours(24),
            httpOnly = true,
            secure = true,
            sameSite = SameSite.Strict
        ))

        // Set preferences cookie
        response.cookie("theme", "dark", CookieOptions(
            maxAge = Duration.ofDays(365),
            httpOnly = false
        ))

        mapOf("loggedIn" to true)
    }

    // Delete cookie
    WizardApplication.post("/logout") { request, response ->
        response.deleteCookie("SESSION_ID")
        mapOf("loggedOut" to true)
    }

    app.run()
}
```

### 4.5 Content Negotiation (XML/JSON/Text)

```kotlin
import com.wizard.WizardApplication

data class User(val id: Int, val name: String, val email: String)

fun main() {
    val app = WizardApplication()

    // Automatic content negotiation based on Accept header
    WizardApplication.get("/users/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val user = User(id, "John Doe", "john@example.com")

        // Framework automatically serializes based on Accept header:
        // - application/json -> JSON
        // - application/xml -> XML
        // - text/plain -> String
        user
    }

    app.run()
}
```

**Test**:
```bash
# Request JSON (default)
curl http://localhost:8090/users/1
# Output: {"id":1,"name":"John Doe","email":"john@example.com"}

# Request XML
curl -H "Accept: application/xml" http://localhost:8090/users/1
# Output: <User><id>1</id><name>John Doe</name><email>john@example.com</email></User>

# Request plain text
curl -H "Accept: text/plain" http://localhost:8090/users/1
# Output: User(id=1, name=John Doe, email=john@example.com)
```

### 4.6 File Downloads

```kotlin
import com.wizard.WizardApplication
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val app = WizardApplication()

    WizardApplication.get("/download/{filename}") { request, response ->
        val filename = request.pathParam("filename")
        val file = Paths.get("files", filename)

        if (!Files.exists(file)) {
            response.status = 404
            return@get mapOf("error" to "File not found")
        }

        response.contentType = Files.probeContentType(file) ?: "application/octet-stream"
        response.header("Content-Disposition", "attachment; filename=\"$filename\"")
        response.header("Content-Length", Files.size(file).toString())

        Files.readAllBytes(file)
    }

    app.run()
}
```

---

## 5. Interceptors

### 5.1 Logging Interceptor

```kotlin
import com.wizard.WizardApplication
import java.time.LocalDateTime

fun main() {
    val app = WizardApplication()

    // Request/Response logging with timing
    WizardApplication.intercept(priority = 10) { request, response, chain ->
        val startTime = System.currentTimeMillis()

        println("[${LocalDateTime.now()}] → ${request.method} ${request.path}")
        println("  Query: ${request.queryParams()}")
        println("  Headers: ${request.header("User-Agent")}")

        chain.proceed(request, response)

        val duration = System.currentTimeMillis() - startTime
        println("[${LocalDateTime.now()}] ← ${response.status} (${duration}ms)")
    }

    WizardApplication.get("/test") { request, response ->
        Thread.sleep(100) // Simulate work
        "Done"
    }

    app.run()
}
```

### 5.2 Authentication Interceptor

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    // API Key authentication
    WizardApplication.intercept(priority = 20) { request, response, chain ->
        val apiKey = request.header("X-API-Key")

        if (apiKey == null || !isValidApiKey(apiKey)) {
            response.status = 401
            response.body = mapOf(
                "error" to "Unauthorized",
                "message" to "Valid API key required"
            )
            return@intercept // Short-circuit - don't proceed
        }

        // Store user context
        request.setAttribute("userId", getUserIdFromApiKey(apiKey))
        chain.proceed(request, response)
    }

    WizardApplication.get("/protected") { request, response ->
        val userId = request.getAttribute("userId")
        mapOf("userId" to userId, "message" to "Access granted")
    }

    app.run()
}

fun isValidApiKey(key: String): Boolean {
    return key == "secret-key-123"
}

fun getUserIdFromApiKey(key: String): Int {
    return 42
}
```

### 5.3 CORS Interceptor

```kotlin
import com.wizard.WizardApplication

fun main() {
    val app = WizardApplication()

    // CORS configuration
    WizardApplication.intercept(priority = 5) { request, response, chain ->
        response.header("Access-Control-Allow-Origin", "*")
        response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key")
        response.header("Access-Control-Max-Age", "3600")

        // Handle preflight OPTIONS request
        if (request.method == "OPTIONS") {
            response.status = 204
            return@intercept
        }

        chain.proceed(request, response)
    }

    WizardApplication.get("/api/data") { request, response ->
        mapOf("data" to "some data")
    }

    app.run()
}
```

### 5.4 Error Handling Interceptors

```kotlin
import com.wizard.WizardApplication

// Custom exceptions
class NotFoundException(message: String) : Exception(message)
class ValidationException(val errors: Map<String, String>) : Exception("Validation failed")

fun main() {
    val app = WizardApplication()

    // Handle not found exceptions
    WizardApplication.interceptError<NotFoundException> { exception, request, response ->
        response.status = 404
        response.body = mapOf(
            "error" to "Not Found",
            "message" to exception.message,
            "path" to request.path
        )
    }

    // Handle validation exceptions
    WizardApplication.interceptError<ValidationException> { exception, request, response ->
        response.status = 422
        response.body = mapOf(
            "error" to "Validation Error",
            "message" to exception.message,
            "errors" to exception.errors
        )
    }

    // Global exception handler
    WizardApplication.interceptError<Exception> { exception, request, response ->
        println("Unhandled exception: ${exception.message}")
        exception.printStackTrace()

        response.status = 500
        response.body = mapOf(
            "error" to "Internal Server Error",
            "requestId" to request.id()
        )
    }

    // Routes that throw exceptions
    WizardApplication.get("/users/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        if (id > 100) {
            throw NotFoundException("User with ID $id not found")
        }
        mapOf("id" to id, "name" to "John")
    }

    WizardApplication.post("/users") { request, response ->
        val user = request.body<Map<String, Any>>()
        val errors = mutableMapOf<String, String>()

        if (!user.containsKey("email")) errors["email"] = "Email is required"
        if (!user.containsKey("name")) errors["name"] = "Name is required"

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        mapOf("created" to true)
    }

    app.run()
}
```

---

## 6. Database Operations

### 6.1 Raw SQL Queries

```kotlin
import com.wizard.WizardApplication

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val createdAt: java.time.LocalDateTime
)

fun main() {
    val app = WizardApplication()

    // SELECT - Get all users
    WizardApplication.get("/users") { request, response ->
        val users = db.query<User>("SELECT * FROM users ORDER BY created_at DESC LIMIT 10")
        mapOf("users" to users, "count" to users.size)
    }

    // INSERT - Create user
    WizardApplication.post("/users") { request, response ->
        val data = request.body<Map<String, String>>()

        val userId = db.insert(
            "INSERT INTO users (username, email) VALUES (:username, :email)",
            mapOf(
                "username" to data["username"],
                "email" to data["email"]
            )
        )

        response.status = 201
        mapOf("id" to userId, "created" to true)
    }

    // UPDATE - Update user
    WizardApplication.put("/users/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val data = request.body<Map<String, String>>()

        db.update(
            "UPDATE users SET username = :username WHERE id = :id",
            mapOf("id" to id, "username" to data["username"])
        )

        mapOf("updated" to true)
    }

    // Transactions
    WizardApplication.post("/transfer") { request, response ->
        db.transaction {
            // All operations in one transaction
            update("UPDATE accounts SET balance = balance - 100 WHERE id = 1")
            update("UPDATE accounts SET balance = balance + 100 WHERE id = 2")
            insert("INSERT INTO transactions (from_account, to_account, amount) VALUES (1, 2, 100)")
        }

        mapOf("success" to true)
    }

    app.run()
}
```

---

## 9. Complete Blog API Example

```kotlin
import com.wizard.WizardApplication
import java.time.LocalDateTime

data class Post(
    val id: Int? = null,
    val title: String,
    val content: String,
    val authorId: Int,
    val published: Boolean = false
)

fun main() {
    val app = WizardApplication()

    // Logging interceptor
    WizardApplication.intercept(priority = 10) { request, response, chain ->
        val start = System.currentTimeMillis()
        println("[${LocalDateTime.now()}] → ${request.method} ${request.path}")
        chain.proceed(request, response)
        println("[${LocalDateTime.now()}] ← ${response.status} (${System.currentTimeMillis() - start}ms)")
    }

    // Error handler
    WizardApplication.interceptError<Exception> { exception, request, response ->
        response.status = 500
        response.body = mapOf("error" to exception.message)
    }

    // List posts
    WizardApplication.get("/posts") { request, response ->
        db.query<Post>("SELECT * FROM posts ORDER BY id DESC LIMIT 10")
    }

    // Get post
    WizardApplication.get("/posts/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        db.queryOne<Post>("SELECT * FROM posts WHERE id = :id", mapOf("id" to id))
            ?: run {
                response.status = 404
                mapOf("error" to "Post not found")
            }
    }

    // Create post
    WizardApplication.post("/posts") { request, response ->
        val post = request.body<Post>()
        val id = db.insert(
            "INSERT INTO posts (title, content, author_id, published) VALUES (:title, :content, :authorId, :published)",
            mapOf("title" to post.title, "content" to post.content, "authorId" to post.authorId, "published" to post.published)
        )
        response.status = 201
        mapOf("id" to id)
    }

    // Update post
    WizardApplication.put("/posts/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        val post = request.body<Post>()
        db.update(
            "UPDATE posts SET title = :title, content = :content WHERE id = :id",
            mapOf("id" to id, "title" to post.title, "content" to post.content)
        )
        mapOf("updated" to true)
    }

    // Delete post
    WizardApplication.delete("/posts/{id}") { request, response ->
        val id = request.pathParam("id").toInt()
        db.delete("DELETE FROM posts WHERE id = :id", mapOf("id" to id))
        response.status = 204
        null
    }

    println("Blog API running on port 8090")
    app.run()
}
```

**Test the API**:
```bash
# Create post
curl -X POST http://localhost:8090/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","content":"World","authorId":1,"published":true}'

# List posts
curl http://localhost:8090/posts

# Get post
curl http://localhost:8090/posts/1

# Update post
curl -X PUT http://localhost:8090/posts/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated","content":"New content","authorId":1}'

# Delete post
curl -X DELETE http://localhost:8090/posts/1
```

---

**End of Code Samples**
