# Kotlin Design Patterns & Best Practices

A curated, example-driven collection of classic and modern design patterns implemented in idiomatic **Kotlin**, alongside a showcase of **language features**, **coroutines**, **functional style**, **testing strategies**, and **architecture best practices**.

> Goal: Help you recognize problems, pick the right pattern (or decide you don’t need one), and implement clean, maintainable, testable Kotlin code.

---

## 🚀 Highlights

- Classic GoF patterns rewritten with Kotlin expressiveness
- Modern alternatives using:
  - `sealed` hierarchies
  - `inline` + reified generics
  - Extension functions & DSL-style builders
  - Coroutines & Flows
  - Immutability & functional composition
- Practical trade-offs & anti-pattern warnings
- Clean Architecture & modular thinking sprinkled in
- Test-first philosophy with focused examples

---

## 🧪 Running & Exploring

```bash
# List tasks
./gradlew tasks

# Run all tests
./gradlew test

# Run a single pattern example (if exposed as application)
./gradlew :patterns:creational:builder:run
```

If each pattern has a `main()` for demo:

```bash
kotlin -classpath build/libs/<module>.jar fully.qualified.MainKt
```

---

## 🧱 Creational Patterns (with Kotlin Nuance)

| Pattern | Kotlin Angle | When to Prefer |
|---------|--------------|----------------|
| Singleton | `object` declarations (thread-safe by default) | Shared stateless utility |
| Builder | DSL builders using lambdas with receiver | Complex object graphs |
| Factory Method | Companion + `when` on enum / sealed | Polymorphic creation |
| Abstract Factory | Composition of factory lambdas | Families of products |
| Prototype | `data class.copy()` | Immutable variations |

```kotlin
// DSL Builder Example
class HttpRequest private constructor(
    val url: String,
    val headers: Map<String, String>,
    val method: Method,
    val body: String?
) {
    enum class Method { GET, POST, PUT, DELETE }

    class Builder {
        private var url: String = ""
        private val headers = mutableMapOf<String, String>()
        private var method: Method = Method.GET
        private var body: String? = null

        fun url(value: String) = apply { url = value }
        fun method(value: Method) = apply { method = value }
        fun header(name: String, value: String) = apply { headers[name] = value }
        fun body(value: String) = apply { body = value }

        fun build() = HttpRequest(url, headers.toMap(), method, body)
    }
}

fun httpRequest(block: HttpRequest.Builder.() -> Unit) =
    HttpRequest.Builder().apply(block).build()

val request = httpRequest {
    url("https://api.example.com")
    method(HttpRequest.Method.POST)
    header("Authorization", "Bearer token")
    body("""{"ping":true}""")
}
```

---

## 🧩 Structural Patterns (Concise Kotlin)

| Pattern | Kotlin Feature Aid |
|---------|--------------------|
| Adapter | Extension functions / delegation |
| Decorator | `by` delegation |
| Facade | Top-level functions / cohesive API |
| Composite | Recursive sealed types |
| Proxy | Higher-order function wrappers |
| Flyweight | Object pooling + `object` singletons |
| Bridge | Interface + DI |

```kotlin
// Decorator via delegation
interface DataSource { fun read(): String; fun write(data: String) }

class FileDataSource(private val path: String): DataSource {
    override fun read() = "raw from $path"
    override fun write(data: String) = println("write $data to $path")
}

class CompressionDataSource(private val wrap: DataSource): DataSource by wrap {
    override fun write(data: String) = wrap.write(compress(data))
    override fun read(): String = decompress(wrap.read())
    private fun compress(s: String) = "ZIP($s)"
    private fun decompress(s: String) = s.removePrefix("ZIP(").removeSuffix(")")
}
```

---

## 🧠 Behavioral Patterns (Expressive Sealed + Lambdas)

| Pattern | Kotlin Superpower |
|---------|-------------------|
| Strategy | Function types |
| Command | Data class + invoke operator |
| Observer | Flow / Channel |
| State | Sealed classes + when |
| Chain of Responsibility | Linked lambdas or list fold |
| Template Method | Default interface impl + final algorithm |
| Visitor | Multimethod using sealed + when |
| Iterator | `sequence {}` builders |
| Interpreter | Recursive descent + sealed AST |

```kotlin
// Strategy with function type
class PaymentContext(private val strategy: (Long) -> Boolean) {
    fun pay(amount: Long) = strategy(amount)
}

val payPal = PaymentContext { amount -> println("PayPal $amount"); true }
val stripe = PaymentContext { amount -> println("Stripe $amount"); true }
```

---

## ⚙️ Modern Kotlin Best Practices Showcased

- Prefer immutability: `val` + pure functions
- Represent state & failure explicitly: `sealed interface`, `Result<T>`, domain error types
- Avoid overusing exceptions for flow-control
- Prefer composition over inheritance; use top-level functions and extension functions
- Domain modeling with value classes (`@JvmInline`)
- Coroutines for async; `Flow` for streams
- Structured concurrency (use `supervisorScope`, `withContext`)
- Minimize global shared state (inject dependencies)
- Keep functions small and intention-revealing
- Write tests first for patterns that encapsulate decisions

---

## 🌊 Coroutines & Concurrency Examples

| Use Case | Recommendation |
|----------|----------------|
| Parallel independent IO | `coroutineScope { async { } ... }` |
| Reactive stream | `Flow` with operators |
| Cancelable chains | Propagate scope, avoid `GlobalScope` |
| Backpressure | `buffer()`, `conflate()`, or explicit channel |

```kotlin
suspend fun loadAll(ids: List<Int>): List<String> = coroutineScope {
    ids.map { id -> async { loadRemote(id) } }.awaitAll()
}
```

---

## 🧪 Testing Philosophy

| Layer | Tooling |
|-------|---------|
| Unit | JUnit5 + Assertions |
| Coroutines | `runTest` from kotlinx-coroutines-test |
| Property-based (optional) | Kotest |
| Snapshot / Golden (optional) | Custom approvals |

Example:

```kotlin
class StrategyTest {
    @Test
    fun `stripe strategy returns success`() {
        val strategy = PaymentContext { it > 0 }
        assertTrue(strategy.pay(100))
    }
}
```

---

## 🧼 Code Quality

Suggested integrations:

- Detekt (`detekt.yml` tuned for clarity over rigidity)
- Ktlint (formatting)
- Binary compatibility validator (for public libs)
- GitHub Actions workflow: build + test + static analysis

---

## 🧱 Dependency Injection

Demonstrate both:

1. Lightweight manual DI (constructor injection + provider functions)
2. Framework-based (e.g., Koin or Hilt) — highlight trade-offs for small vs large projects.

---

## 🗃️ Error Handling Strategy

- Domain: sealed error hierarchy  
- Boundary mapping: convert infra exceptions to domain errors  
- Avoid: swallowing exceptions, broad `try { } catch (e: Exception)`  

```kotlin
sealed interface UserError {
    data object NotFound: UserError
    data class Validation(val reason: String): UserError
    data object Unknown: UserError
}

suspend fun loadUser(id: String): Result<User> =
    runCatching { remoteFetch(id) }
        .mapError { toUserError(it) }

inline fun <T> Result<T>.mapError(block: (Throwable) -> Throwable) =
    fold(onSuccess = { Result.success(it) },
         onFailure = { Result.failure(block(it)) })
```

---

## 🧭 Choosing a Pattern (Decision Cheatsheet)

| Symptom | Consider |
|---------|----------|
| Too many constructors | Builder / Factory |
| Growing conditional logic by type | Polymorphism / Strategy / State |
| Repetitive transforms | Template Method / Higher-order fn |
| Need cross-cutting feature | Decorator |
| Observable async events | Flow / Observer |
| Nested switch on type + operation | Visitor (or sealed + when) |
| Complex object graph creation | Abstract Factory / DI |

---

## 🛑 Anti-Patterns Avoided

- Over-engineering simple functions into patterns
- God objects / giant managers
- Excessive inheritance trees
- Null misuse where sealed states suffice
- Leaking coroutine scopes
- Static mutable singletons

---

## 🧭 Suggested Learning Path

1. Kotlin language fundamentals refresh (data / sealed / inline classes)
2. Creational patterns with Kotlin-specific twists
3. Structural patterns leveraging delegation
4. Behavioral patterns using sealed + lambdas
5. Coroutines + structured concurrency refactors
6. Replace patterns with simpler language features where possible
7. Introduce DI + modular boundaries

---

## 📚 Recommended Resources

| Topic | Resource |
|-------|----------|
| Kotlin Language | [Kotlin Docs](https://kotlinlang.org/docs/home.html) |
| Coroutines | [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html) |
| Effective Kotlin | Book by Marcin Moskala |
| Design Patterns | "Design Patterns: Elements of Reusable Object-Oriented Software" |
| Clean Architecture | Robert C. Martin |

---

## 🤝 Contributing

1. Open an issue proposing a pattern or improvement
2. Follow naming + directory conventions
3. Add tests + docs for new examples
4. Keep examples minimal, focused, idiomatic

Suggested commit style:

```
pattern(strategy): add flow-based variant
best-practice(error-handling): introduce sealed error mapping
```

---

---

If you share your actual directory structure or patterns already implemented, I can tailor this further. Feel free to request additions (e.g., Jetpack Compose variants, Android-specific examples, multiplatform notes).

Happy designing! 🧠
