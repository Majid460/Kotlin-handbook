# Kotlin Handbook

A practical, example-driven handbook for learning Kotlin — from language fundamentals to common idioms, object-oriented features, coroutines, and practical patterns. This repository collects short examples and explanations intended to be used as a quick reference and learning resource while practicing Kotlin in IntelliJ/Gradle.

[Project structure screenshot]: ./docs/project-structure.png

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project layout](#project-layout)
- [Examples & Topics Covered](#examples--topics-covered)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Contributing](#contributing)
- [Code Style](#code-style)
- [License](#license)
- [Contact](#contact)

---

## Overview

Kotlin Handbook is a compact collection of Kotlin examples and explanations organized by topic. Each file is intended to be small and focused so you can quickly jump to the feature you want to review or experiment with.

---

## Features

- Concise examples covering Kotlin basics and advanced topics
- Demonstrations of coroutines, flows, channels
- Object-oriented features (classes, companion objects, value types)
- Property delegation examples (lazy, observable)
- Ready-to-run examples compatible with Gradle/IntelliJ

---

## Prerequisites

- JDK 11+ (or the version configured in `gradle.properties`)
- Gradle Wrapper included (you can use `./gradlew` on Unix/macOS or `gradlew.bat` on Windows)
- Recommended: IntelliJ IDEA (Community or Ultimate) for best Kotlin support

---

## Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/Majid460/Kotlin-handbook.git
   cd Kotlin-handbook
   ```

2. Open the project in IntelliJ (File → Open... and select the repo root) or build from the command line:
   ```bash
   ./gradlew build
   ```

3. Run an example:
   - From IntelliJ: right-click `Main.kt` (or any `main` function) and Run.
   - From CLI (if configured): `./gradlew run` (depends on project `application` configuration).

---

## Project layout

This repo is organized by topic. Example structure (reflects the project screenshot):

- build.gradle.kts
- gradle.properties
- gradlew / gradlew.bat
- .gitignore
- src/
  - main/
    - kotlin/
      - basic/
        - ConditionalStatements.kt
        - KotlinTypes.kt
        - Loops.kt
      - coroutines/
        - BasicCoroutines.kt
        - Channels.kt
        - CoroutineBuilder.kt
        - flows.kt
      - oop/
        - classes/
          - Classes.kt
          - SealedClasses.kt
          - ValueClass.kt
        - objects/
          - CompanionObject.kt
          - DataObject.kt
          - Object.kt
          - DelegateProperties.kt
          - Example.kt
          - Inheritance.kt
          - Interface.kt
          - VisibilityModifier.kt
        - Main.kt
    - resources/
  - test/

(If any file names differ in your current tree, update the layout section above to match.)

---

## Examples & Topics Covered

Sample topics and short descriptions:

- Kotlin fundamentals
  - Variables, types, immutability
  - Control flow: `if`, `when`, loops
  - Functions & extensions

- OOP
  - Classes, inheritance, visibility modifiers
  - Sealed classes and value classes
  - Companion objects and singletons (object declarations)

- Property Delegation
  - `lazy` initialization
  - `Delegates.observable` and other delegation patterns

- Concurrency & Coroutines
  - `launch`, `async`, coroutine builders
  - Channels, flows and reactive-like streams

- Patterns & Best Practices
  - Null-safety idioms
  - Effective use of extension functions
  - Structured concurrency and exception handling in coroutines

Example snippet (property delegation):
```kotlin
class DelegateProperties {
    var name: String by Delegates.observable("unknown") { property, oldValue, newValue ->
        println("$oldValue -> $newValue")
    }

    val lazyValue: String by lazy {
        println("computed!")
        "Hello"
    }
}

fun main() {
    val user = DelegateProperties()
    user.name = "first"
    user.name = "second"

    println(user.lazyValue)
    println(user.lazyValue)
}
```

---

## Build & Run

- Build:
  ```bash
  ./gradlew build
  ```
- Run tests:
  ```bash
  ./gradlew test
  ```
- Run a specific Kotlin `main` (if `application` plugin configured):
  ```bash
  ./gradlew run --args='arg1 arg2'
  ```
- Open with IntelliJ and run individual example files with `main` functions.

---

## Testing

- Tests are written in `src/test` and executed with Gradle (`./gradlew test`).
- Add unit tests for examples or core utilities as you refactor examples into reusable components.

---

## Contributing

Contributions are welcome! Suggested workflow:

1. Fork the repository.
2. Create a branch for your change:
   ```bash
   git checkout -b feat/topic-name
   ```
3. Add or improve examples and docs. Keep examples small and focused.
4. Run build and tests locally.
5. Open a Pull Request with a clear description of changes.

Please include:
- Purpose of the new example or change
- How to run the new example
- If applicable, reference issues or learning goals

---

## Code Style

- Follow Kotlin coding conventions: https://kotlinlang.org/docs/coding-conventions.html
- Consider using formatting/linting tools:
  - ktlint
  - detekt

Add a CI job in the future to run `ktlint` and `./gradlew test` on PRs.

---

## License

This repository is available under the MIT License. See LICENSE file for details (or add one if not present).

---

## Contact

Maintainer: Majid460 — open an issue or PR for questions, corrections, or new examples.

---

Thank you for building and sharing learning resources — if you want, I can:
- commit this README.md directly to `main` (or a new branch) for you, or
- create a PR with the README added — tell me which you prefer and confirm the repository path (owner/repo) to use.
