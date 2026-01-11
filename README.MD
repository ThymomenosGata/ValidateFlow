# ValidateFlow

ValidateFlow is a lightweight, type-safe DSL validation library for [
`Kotlin Coroutines Flow`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/).

ValidateFlow provides three strategies for processing stream data:

- <b>Latest (UI/UX):</b> Processes only the latest emmited item, cancelling previous validations (perfect for "
  search-as-you-type" of form input)
- <b>Sequential:</b> Processes all elements one by one, preserving strict order
- <b>Parallel:</b> Processes multiple elements concurrently for maximum performance when order doesn't matter

<hr>

# Quick Start

1. <b>UI/UX: Processing the latest item</b>

Uses `flatMapLatest` under the hood. If a new user arrives before the previous one is validated, the old validation is
cancelled.

```kotlin
users.validateReactiveLatest {
    validateField({ name }) {
        check(
            message = "Error",
            code = "ERR_SHORT_NAME"
        ) {
            it.length > 3
        }
    }
}.collect { result ->
    if (result.isValid) println("Welcome")
    else println("Errors: ${result.errors}")
}
```

2. <b>Sequential: Processing all items</b>

Uses `map`. Guarantees that elements are processed in the exact order they were emmited.

```kotlin
users.validateReactive {
    validateField({ age }) {
        check(
            message = "Error",
            code = "ERR_NOT_AN_ADULT"
        ) {
            it >= 18
        }
    }
}.collect { /*...*/ }
```

3. <b>Parallel: High performance</b>

Uses `flatMapMerge`. Validates multiple items at once (default concurrency is 16)

```kotlin
users.validateParallel(concurrency = 5) {
    validateField({ name }) {
        checkAsync(
            message = "Name is occupied",
            code = "ERR_NAME_IS_OCCUPIED"
        ) {
            isNicknameUnique(it)
        }
    }
}.collect { /*...*/ }
```

<hr>

# Advanced Validation

#### Async Checks

For long-running operations like database lookups or API calls, use `checkAsync`:

```kotlin
validateField({ name }) {
    checkAsync(
        message = "Name is occupied",
        code = "ERR_NAME_IS_OCCUPIED"
    ) { name ->
        api.isNicknameUnique(name)
    }
}
```

#### Result Structure

The `collect` block receives a `ValidatorResult` object:

|   field   |          Type           |               Description               |
|:---------:|:-----------------------:|:---------------------------------------:|
| `isValid` |        `Boolean`        |      `true` if all checks passed.       |
| `errors`  | `List<ValidationError>` | A list of failures for the current item |

Each ValidatorError contains:

|   field   |   Type   |                        Description                         |
|:---------:|:--------:|:----------------------------------------------------------:|
| `message` | `String` |            A human-readable error description.             |
|  `code`   | `String` | An optional machine-readable string (e.g., `ERR_AUTH_01`). |

<hr>

# Why ValidateFlow?
- <b>Zero Overhead:</b> Extensive use of `inline` functions ensures no extra objects are created during validation
- <b>Type Safe:</b> Full compile-time safety for your models.
- <b>Coroutine Native:</b> Built specifically for Kotlin Coroutines

<hr>