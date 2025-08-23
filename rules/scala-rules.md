# Functional Scala Programming

Guidelines for writing functional Scala code using Future-based async patterns, avoiding heavy functional programming libraries like Cats or ZIO in favor of stdlib and Akka ecosystem.

## Context

*Applies to:* All Scala code in SDKMAN! services and libraries
*Level:* Tactical - guides daily coding practices and code structure
*Audience:* Scala developers working on SDKMAN! platform

## Core Principles

1. *Future-First Async:* Use Scala's Future for all asynchronous operations, avoiding blocking calls
2. *Trait Composition:* Leverage the cake pattern for dependency injection and modular architecture
3. *Immutable Data:* Prefer immutable case classes and collections for all data structures
4. *Pure Functions:* Favor pure functions that are referentially transparent and side-effect free
5. *Explicit Error Handling:* Use Option/Either types and Future failures for predictable error flows

## Rules

### Must Have (Critical)

- *RULE-001:* All async operations MUST use Future, never block with Await except in main() methods
- *RULE-002:* Data models MUST be immutable case classes, never use mutable var fields
- *RULE-003:* Traits MUST use self-type annotations for dependencies (e.g., `self: SomeDependency =>`)
- *RULE-004:* Future composition MUST use for-comprehensions or flatMap/map, never nested callbacks
- *RULE-005:* Side effects MUST be isolated to designated traits/objects, not mixed with pure logic

### Should Have (Important)

- *RULE-101:* Use Option instead of null values for optional fields in case classes
- *RULE-102:* Prefer `lazy val` over `val` for expensive computations and configurations
- *RULE-103:* Group related functionality into cohesive traits following single responsibility
- *RULE-104:* Use meaningful names that describe business domain rather than technical implementation
- *RULE-105:* Handle Future failures explicitly with recover/recoverWith or proper error types

### Could Have (Preferred)

- *RULE-201:* Use `private` modifiers liberally to minimize public API surface
- *RULE-202:* Prefer `def` over `val` for methods that perform computation or have parameters
- *RULE-203:* Use pattern matching on case classes rather than accessor methods when destructuring
- *RULE-204:* Group imports by standard library, third-party, then local packages with blank lines

## Patterns & Anti-Patterns

### ✅ Do This

```scala
// Functional Future composition with for-comprehension
def processRelease(req: ReleaseRequest): Future[HttpResponse] = 
  for {
    candidate <- findCandidate(req.candidateName)
    version   <- validateVersion(req.version)
    result    <- saveRelease(candidate, version)
  } yield createdResponse(result)

// Cake pattern trait with self-types
trait ReleaseService {
  self: CandidatesRepo with VersionsRepo =>
  
  def release(candidate: String): Future[Version] = 
    findCandidate(candidate).flatMap(saveVersion)
}

// Immutable case class with Option types
case class Release(
  candidate: String,
  version: String,
  platform: String,
  vendor: Option[String] = None,
  checksums: Option[Map[String, String]] = None
)
```

### ❌ Don't Do This

```scala
// Blocking operations in async context
def badAsync(): Future[String] = Future {
  val result = Await.result(someOtherFuture, Duration.Inf)  // Never do this
  result.toUpperCase
}

// Mutable data structures
case class BadRelease(
  var candidate: String,  // Never use var in case classes
  var platform: String
)

// Nested Future callbacks (callback hell)
def nestedCallbacks(): Future[String] = 
  service1().flatMap { result1 =>
    service2(result1).flatMap { result2 =>
      service3(result2).map { result3 =>  // Deeply nested - use for-comprehension
        result3.toString
      }
    }
  }
```

## Decision Framework

*When choosing between patterns:*
1. Prefer Future over blocking operations for any I/O
2. Use for-comprehensions over nested flatMap when dealing with 3+ operations
3. Choose traits over classes for composable functionality

*When handling errors:*
- Use Future.failed() for business logic errors
- Use Option for values that may not exist
- Use Either for computations that can fail with detailed error information

## Exceptions & Waivers

*Valid reasons for exceptions:*
- Main application bootstrap code (Await for server binding)
- Test utilities where blocking is acceptable for simplicity
- Integration with Java libraries that require blocking calls

*Process for exceptions:*
1. Document the blocking operation with explanatory comment
2. Isolate to dedicated utility functions
3. Consider timeout values for any blocking operations

## Quality Gates

- *Automated checks:* Scalafmt formatting, compiler warnings for unused imports
- *Code review focus:* Future composition patterns, immutability of data structures, trait dependencies
- *Testing requirements:* Test async code with ScalaTest's async testing support, no Await in tests

## Related Rules

- None currently defined - this establishes the foundation for Scala coding standards

## References

- [Scala Future Documentation](https://docs.scala-lang.org/overviews/core/futures.html)
- [Cake Pattern Guide](https://www.scala-lang.org/old/node/2718)
- [Effective Scala](https://twitter.github.io/effectivescala/)

---

## TL;DR

*Key Principles:*
- Use Future for all async operations, compose with for-comprehensions
- Build with immutable case classes and trait-based dependency injection  
- Keep side effects isolated and pure functions everywhere else

*Critical Rules:*
- Must use Future instead of blocking operations
- Must define data as immutable case classes only
- Must compose traits with self-type annotations for dependencies

*Quick Decision Guide:*
When in doubt: favor immutability, async Future operations, and trait composition over classes.