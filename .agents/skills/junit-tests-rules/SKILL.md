---
name: junit-tests-rules
description: Use this skill when creating, updating, debugging, or reviewing JUnit tests in this repository.
---

# JUnit Tests Rules

Follow these rules when creating or modifying tests in this repository.

## Core Constraints

- Use JUnit 5 only.
- Do not use JUnit 4 classes, annotations, runners, or assertions.
- Do not use Mockito unless the user explicitly requests it.
- Do not use `sun.misc.Unsafe`, `jdk.internal.misc.Unsafe`, or any other `Unsafe` access even in tests.
- Keep test changes minimal and directly related to the behavior under test.
- Do not change production code only to make a weak test pass unless the task explicitly requires a production fix.
- Do not fix unaffected tests or Javadoc only to comply the rules in this document.

## Testing scopes

- Prefer a single-class test when the class can be tested with real collaborators or trivial handwritten fakes.
- Prefer a multi-class test when:
  - the behavior under test naturally spans multiple tightly coupled classes, and
  - isolating one class would require non-trivial stubs/fakes that duplicate production logic.

Decision table:
- Pure or utility class: single-class test
- One class, trivial fake needed: single-class test
- One class, heavy stubbing needed: consider multi-class test
- Cross-class observable behavior: multi-class test

## Test Naming

### Test class names

- Create tests in package with format `%p.test.%s` where `%p` is primary module package, `%s` should mirror the production subpackage being verified. If no clear subpackage exists, use the nearest functional area package name.
- Use `Test` suffix for test class names.
- If testing one class, use its class name + `Test` suffix.
- If testing multiple classes, use format `%d%f%vTest` where `%d` is bounded domain or module area, `%f` - feature or operation, `%v` - expected behavior.

Examples:
- For testing `io.pione.lib.domain.example.SomeClass` in module `lib/domain` create test `io.pione.lib.domain.test.example.SomeClassTest`
- For testing multiple classes in `lib/domain` for batch functionality to verify splitting create test `io.pione.lib.domain.test.batch.DomainBatchSplitTest`

### Test method names

- Do not use `test` prefix or suffix in test method names.

## Test Design Rules

- Test one behavior at a time.
- Verify observable behavior, not implementation details, unless there is no stable public seam.
- Prefer real value objects and simple hand-written stubs/fakes over mocking frameworks.
- Reuse existing test utilities and fixtures before introducing new helpers.
- Keep fixtures local to the test class unless reuse is already established in the module.
- Avoid randomness, time sensitivity, shared mutable state, and hidden external dependencies.
- Do not use sleeps, polling waits, Awaitility, or per-test timeouts in ordinary unit tests. Unit tests must be deterministic and should not slow CI/CD with wall-clock waiting.
- Timeouts and polling waits are allowed only for workflow/integration style tests that run outside the normal unit-test path, for example tests under `test/workflow` marked `@Disabled` or tests marked `@IntegrationTest`.
- Make inputs explicit in the test body so the scenario is easy to understand from one screen.
- Use clear test names that describe the scenario and expected result.

## Javadoc For Test Methods

Add Javadoc for test methods when creating or updating tests.
Each test method Javadoc must explicitly describe:

- `Test goal` - why the test exists and what behavior it verifies.
- `Precondition` - any special setup or state required before the main action.
- `Action` - the operation performed in the test.
- `Verification` - the exact outcome/assertion that proves correct behavior.

Use a compact structure like this:

```java
/**
 * Test goal: verifies that ...
 * <p>Precondition: ...
 * <p>Action: ...
 * <p>Verification: ...
 */
```

## Assertions And Structure

- Use JUnit 5 assertions unless a specific assertion makes more sense in the context.
- Keep assertions specific and meaningful; avoid broad `assertTrue` checks when a stronger assertion is available.
- Prefer `assertThrows` for exception checks and verify the message or error details when that is part of the contract.
- Keep arrange, act, and assert phases visually clear.
- Extract private helper methods only when they reduce duplication without hiding the scenario.

## Test Execution Scope

- Execute tests in the minimum possible scope.
- Do not run extra tests just in case.
- Start with the affected module only.
- Prefer targeted execution for the exact class or method being changed.
- Do not use `-am` by default.
- Use `-am` only when it is the first build in the session, when upstream modules changed, or when Maven reports missing artifacts.
- Do not run root-level test or build commands for local troubleshooting.

## Preferred Commands

- Run one module:
  `mvn -pl <module> test`
- Run one test class:
  `mvn -pl <module> -Dtest=<ClassName> test`
- Run one test method:
  `mvn -pl <module> -Dtest=<ClassName>#<methodName> test`

## Review Checklist

Before finalizing test changes, check that:

- the test uses JUnit 5 only;
- Mockito was not introduced unless requested;
- no `Unsafe` usage was introduced;
- Javadoc includes goal, precondition, action, and verification;
- assertions verify externally visible behavior;
- execution scope stays minimal;
- no unrelated tests or fixtures were changed.

## Priority Order

When these rules conflict, use this order:

- Explicit user instructions
- Repository instructions in `AGENTS.md`
- This document
- Existing module conventions
- General testing best practices

## Output Expectations for Codex

When generating or editing tests:

- produce deterministic tests;
- keep the scope tight;
- explain any non-obvious fixture or stub choice;
- report exactly which tests were run, or state that tests were not run.
