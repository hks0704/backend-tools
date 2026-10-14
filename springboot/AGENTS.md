# AGENTS.md

## Project Overview

This project is a simple bulletin board backend.

The application provides REST APIs for:

- User management
- Post creation
- Post retrieval
- Post modification
- Post deletion
- Basic comments

The project is intended as a learning and portfolio project.

Prioritize simple, maintainable architecture over unnecessary complexity.

---

## Technology Stack

- Java 21
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA
- Hibernate
- MySQL
- Gradle
- JUnit 5

Use Java and Spring Boot idioms rather than introducing unnecessary frameworks.

---

## Architecture

Use the following basic layered architecture:

    Controller
        ↓
    Service
        ↓
    Repository
        ↓
    Entity
        ↓
    MySQL

### Controller

Controllers are responsible for:

- HTTP request handling
- Request validation
- Calling services
- Returning HTTP responses

Controllers must NOT contain:

- Business logic
- Database access
- Complex data processing

---

### Service

Services are responsible for:

- Business logic
- Transaction boundaries
- Coordinating multiple repositories
- Domain-level validation

Use `@Transactional` where transaction management is required.

Prefer:

    @Service
    @RequiredArgsConstructor
    public class PostService {
        ...
    }

Do not put business logic inside controllers.

---

### Repository

Repositories use Spring Data JPA.

Example:

    public interface PostRepository extends JpaRepository<Post, Long> {
    }

Repositories should focus on data access.

Do not put business logic inside repositories.

---

### Entity

Entities represent database/domain models.

Example:

    @Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        private String content;
    }

Rules:

- Do not expose public setters by default.
- Prefer meaningful domain methods for state changes.
- Do not expose entities directly through REST APIs.
- Avoid unnecessary entity relationships.
- Be careful about lazy loading and N+1 queries.

---

## API Design

Use REST-style endpoints.

Example:

    GET    /api/posts
    GET    /api/posts/{id}
    POST   /api/posts
    PUT    /api/posts/{id}
    DELETE /api/posts/{id}

Use appropriate HTTP status codes.

Examples:

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 404 Not Found
- 409 Conflict

---

## DTO Rules

Do not expose JPA entities directly from controllers.

Use DTOs for API requests and responses.

Example:

    public record CreatePostRequest(
        String title,
        String content
    ) {
    }

Example response:

    public record PostResponse(
        Long id,
        String title,
        String content
    ) {
    }

Prefer Java `record` for simple immutable request/response DTOs.

---

## Validation

Use Jakarta Bean Validation.

Example:

    public record CreatePostRequest(

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        String content

    ) {
    }

Validate input at the API boundary.

Do not rely only on database constraints for user input validation.

---

## Exception Handling

Use a centralized exception handling approach.

Prefer:

    @RestControllerAdvice
    public class GlobalExceptionHandler {
        ...
    }

Do not duplicate exception handling logic across controllers.

For example:

    PostNotFoundException
            ↓
    404 NOT FOUND

Use meaningful custom exceptions when they improve readability.

---

## Database

Use MySQL.

Database-related rules:

- Use `Long` for generated IDs.
- Use `BIGINT` for IDs in MySQL.
- Add appropriate indexes for frequently searched columns.
- Do not add indexes without a reason.
- Avoid unnecessary native SQL.
- Prefer JPQL or Spring Data query methods when sufficient.

For the initial project, prioritize correctness and readability over premature database optimization.

---

## Transaction Rules

Use transactions at the service layer.

Example:

    @Transactional
    public void updatePost(Long postId, UpdatePostRequest request) {
        ...
    }

For read-only operations:

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        ...
    }

Do not put `@Transactional` on controllers.

---

## Naming Conventions

Use standard Java naming conventions.

Classes:

- `PostController`
- `PostService`
- `PostRepository`
- `PostResponse`
- `CreatePostRequest`

Methods should clearly describe their behavior.

Prefer:

- `createPost()`
- `updatePost()`
- `deletePost()`
- `findPost()`

Avoid vague names such as:

- `process()`
- `handle()`
- `doSomething()`

---

## Lombok

Lombok may be used to reduce boilerplate.

Preferred annotations:

- `@Getter`
- `@RequiredArgsConstructor`
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`

Avoid `@Data` on JPA entities.

Do not use Lombok merely for the sake of reducing every line of code.

---

## Testing

Use JUnit 5.

Tests should primarily focus on:

- Service business logic
- Important controller behavior
- Repository behavior where necessary

When implementing a new business feature, add or update relevant tests.

Prefer meaningful test names.

Example:

    @Test
    void 존재하지_않는_게시글을_조회하면_예외가_발생한다() {
        ...
    }

---

## Code Modification Rules

Before modifying code:

1. Inspect the existing implementation.
2. Understand the dependency between related classes.
3. Check existing tests.
4. Make the smallest reasonable change.
5. Avoid unrelated refactoring.

Do not rewrite entire files when a small change is sufficient.

Do not introduce new libraries without explaining why they are necessary.

---

## Dependency Rules

Before adding a dependency:

1. Check whether existing Java or Spring Boot features can solve the problem.
2. Check whether the project already has a dependency providing similar functionality.
3. Prefer standard Spring Boot functionality.
4. Explain the reason for introducing a new dependency.

Do not add dependencies automatically.

---

## Database Migration

For the initial project, database schema changes may be handled using SQL scripts.

Do not silently change the database schema.

When a schema change is required:

1. Explain the change.
2. Identify affected entities.
3. Identify affected queries.
4. Identify whether existing data could be affected.

---

## Performance

Do not optimize prematurely.

However, when modifying database-related code, consider:

- N+1 queries
- unnecessary joins
- unnecessary entity loading
- pagination
- indexes

For post listing APIs, prefer pagination rather than loading every post.

Example:

    Page<Post> findAll(Pageable pageable);

---

## Security

Never hard-code:

- Database passwords
- API keys
- Secret keys
- Access tokens

Use environment variables or external configuration.

Do not commit secrets to Git.

---

## Git

Keep commits focused.

Prefer commits such as:

- `feat: add post creation API`
- `feat: add post pagination`
- `fix: handle missing post`
- `test: add post service tests`
- `refactor: separate post response DTO`

Avoid unrelated changes in the same commit.

---

## Agent Behavior

When asked to modify code:

1. First inspect relevant files.
2. Explain the intended change briefly.
3. Implement the change.
4. Inspect the modified code.
5. Run relevant tests when possible.
6. Report:
   - files changed
   - changes made
   - tests executed
   - remaining issues

Do not modify unrelated files.

If requirements are ambiguous and the ambiguity can materially change the implementation, ask for clarification.

If the intent is clear, proceed without unnecessary questions.

---

## Important Principle

Keep the project simple.

Do not introduce the following unless explicitly requested:

- CQRS
- Event Sourcing
- Microservices
- unnecessary design patterns
- excessive abstraction
- unnecessary interfaces

The goal is a clean Spring Boot backend that demonstrates sound engineering fundamentals.
