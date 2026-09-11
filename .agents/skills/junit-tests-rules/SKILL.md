---
name: junit-tests-rules
description: Create, update, debug, or review HREasy backend tests using the repository's JUnit 5, Reactor, Spring Boot, Testcontainers, and Mockito patterns.
---

# HREasy JUnit Conventions

Follow `backend/AGENTS.md` and nearby tests in the affected module.

## Test Style

- Use JUnit 5; the repository contains no JUnit 4 tests.
- Keep tests deterministic and focused on observable behavior.
- Reuse existing bases and fixtures such as `BaseServiceTest`, `BasePostgresTest`, and `PostgreSQLTestContainerContextInitializer`.
- Use `StepVerifier` or an existing bounded Reactor test pattern for reactive behavior.
- Mockito is available and already used; use it when it keeps the test narrower than a Spring integration test or handwritten fake.
- Do not introduce sleeps, polling, `Unsafe`, real personal data, or machine-local absolute paths.
- Keep a test in the production package or follow the affected module's nearby package convention.
- Use the `Test` class suffix. Follow nearby method naming; both descriptive names and legacy `test...` names exist.
- Add Javadoc only when setup, business intent, or verification is not clear from the test name and body.
- Prefer specific JUnit assertions and verify error details when they are part of the contract.

## Execution

Run targeted commands from the repository root so reactor dependencies are available:

- Module: `mvn -q -f backend/pom.xml -pl <module> -am test`
- Class: `mvn -q -f backend/pom.xml -pl <module> -am -Dtest=<ClassName> "-Dsurefire.failIfNoSpecifiedTests=false" test`
- Method: `mvn -q -f backend/pom.xml -pl <module> -am "-Dtest=<ClassName>#<methodName>" "-Dsurefire.failIfNoSpecifiedTests=false" test`

Start with the affected test. Run the full backend reactor only for genuinely cross-module changes.

Report exactly which command ran, or state that tests were not run.
