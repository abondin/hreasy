---
name: java-coding-style
description: Use this skill when creating or editing Java source code.
---

# Java coding style recommendations

This document defines the expected coding style for Java files.
Follow these rules when creating or modifying `.java` files.

## Goals
- Keep code readable and predictable
- Prefer simple, maintainable solutions
- Match the existing project style when it differs slightly from this guide
- Avoid unnecessary abstractions and premature optimization

## General Principles
- Write clear, boring, production-friendly Java
- Prefer explicitness to cleverness
- Keep methods focused and short
- Minimize side effects
- Use descriptive but still short names
- Preserve backward compatibility unless the task explicitly requires a breaking change
- Avoid hidden behavior and magic

## Project Consistency
- Follow the surrounding code style in the touched module
- Reuse existing utilities, patterns, and conventions before introducing new ones
- Do not reformat unrelated code
- Do not rename symbols unless needed for the task

## Java Version
- The Java version is 25
- Java 25 features are preferred when they improve clarity
- Do not use Java 25+ preview features
- If unsure, prefer conservative syntax compatible with Java 25
- Remember that default charset is always UTF-8 in Java 25

## Imports
- Use and group imports according to standard IntelliJ IDEA settings
- Remove unused imports
- Prefer standard library and existing project dependencies over adding new libraries
- Prefer Java or Spring utility methods over other libraries

## Class member declaration order
- public constants
- private constants
- other static fields
- static blocks
- final fields initialized in constructor
- final fields with initializer expression
- other fields
- primary constructor that does not call other constructors
- other constructors
- public non-static non-overridden methods
- public non-static overridden methods
- protected methods
- private non-static methods
- private static methods
- public static methods
- private nested non-static classes
- private nested static classes
- public nested static classes

### Additional rules
- Separate members of the same group with a blank line.
- Order members within the same group according to their significance or life-cycle
- Try to order methods according to their calling of each other, callee below caller. 

## Naming

### Classes and Interfaces
- Use `PascalCase`
- Use nouns for classes, adjectives or nouns for interfaces as appropriate

### Methods
- Use `camelCase`
- Use verbs or verb phrases
- Examples:
    - `calculateTotal`
    - `findUserById`
    - `isEnabled`
  
### Variables, fields and parameters
- Use `camelCase`
- Names should reflect intent, not type
- Avoid single-letter names except for short-lived loop indices and lambda parameters.
- For unused lambda parameters on Java 25, prefer `_`.
- Do not replace a parameter with `_` if it is used.
- Do not rewrite existing `_` lambda parameters into named placeholders unless there is a concrete reason.

### Constants
- Use `UPPER_SNAKE_CASE`
- Only use for true constants, especially reused several times
- Never use constants for log formatting strings

## Formatting
- Use 4 spaces for indentation
- Do not use tabs; fix tabs to spaces if encountered in changed file
- Keep lines reasonably short; prefer readability to strict wrapping
- Use braces for all control-flow blocks, even single-line blocks
- Put one statement per line
- Add a blank line between logical sections
- Avoid dense code

## Control Flow
- Prefer early returns to deep nesting

## Methods
- Keep methods small and focused
- Prefer at most one clear responsibility per method
- Extract private helper methods when it improves readability
- Avoid long parameter lists; introduce a parameter object if needed
- Avoid hidden mutations

## Classes
- Keep classes cohesive
- Prefer composition to inheritance unless inheritance is clearly justified
- Avoid god classes
- Make fields private unless there is a strong reason not to

## Access Modifiers
- Use the narrowest visibility possible
- Prefer private for fields and helper methods
- Avoid package-private exposure
- Do not make things public by default
- Do not use `final` keyword unless there is a strong reason for it

## Null Handling
- Avoid returning null when a safer alternative exists
- Use `org.jetbrains.annotations.NotNull` and `org.jetbrains.annotations.Nullable` where relevant; never use other Nullable types.
- Do not use `Optional` unless it is effective for streaming

## Collections
- Prefer interfaces in declarations, for example:
  - List instead of ArrayList
  - Map instead of HashMap
- Use immutable collections where practical
- Avoid exposing mutable internal collections directly

## Exceptions
- Use exceptions for exceptional situations, not normal control flow
- Prefer throwing `PiException` with specific static factory method unless a specific exception is required
- Preserve useful context in exception messages
- Wrap lower-level exceptions into `PiException`, but only if it adds value

## Logging
- Use the Slf4j logging, including `@Slf4j` annotation from Lombok (unless injected logger is available).
- Log meaningful events, not noise
- Do not log sensitive data
- Use appropriate log levels
- Prefer structured, contextual messages

## Comments
- Prefer self-explanatory code to comments
- Add comments when the intent, constraint, or business rule is not obvious
- Keep comments accurate and update them when code changes
- Do not add redundant comments that restate the code
- Do not remove existing comments unless whole code block is removed

## Javadoc
- Add Javadoc for public classes and public methods
- Add Javadoc for classes with complex logic to summarize it
- Do not add boilerplate Javadoc for trivial getters/setters
- Do not add Javadoc for overridden methods unless complex logic needs to be explained

## Immutability
- Prefer immutable objects when practical
- Minimize shared mutable state

## Dependency Injection
- Prefer constructor injection for required dependencies
- Avoid field injection unless there is no other way
- Make dependencies explicit

## Concurrency
- Do not introduce concurrency unless required
- Be explicit about thread-safety assumptions
- Prefer existing concurrency utilities over custom synchronization
- Document non-obvious thread-safety behavior

## Performance
- Prefer readability first
- Optimize only when there is a clear need
- Avoid unnecessary allocations in hot paths if the context makes performance important
- Do not sacrifice correctness for micro-optimizations

## Spring Boot rules
- Prefer existing repo/platform patterns first; in Spring Boot modules, follow established Spring Boot conventions
- Do not place business logic in controllers, entities, configuration or repository classes
- Do not use interfaces for services unless two or more implementations are explicitly required
- Prefer constructor dependency injection with `@RequiredArgsConstructor` Lombok semantics
- Avoid `@Autowired` annotation unless there is no other way
- Prefer `@ConfigurationProperties` annotation to `@Value` annotation

## Lombok rules
- Avoid usage of `@Data` annotation; use specific annotations that apply in the context
- Avoid excessive Lombok that hides important behavior
- Avoid `@Builder` for classes with few fields

## When Editing Existing Code
- Make the smallest clean change that solves the task
- Preserve behavior unless the task requires changing it
- Improve nearby code only when it is low-risk and directly helpful
- Do not perform broad refactors unless requested

## Priority Order
When these rules conflict, use this order:
- Explicit user instructions
- Existing repository conventions
- This document
- Surrounding code style
- General Java / Spring Boot best practices

# Output Expectations for Codex
When generating or editing Java code:
- Produce compilable code
- Match existing package structure
- Keep imports clean
- Avoid unrelated formatting churn
- Briefly explain any non-obvious design choice
