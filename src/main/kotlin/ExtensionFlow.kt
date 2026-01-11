package org.wordy.validate.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.wordy.validate.flow.ValidatorScope.FieldScope

/**
 * Performs reactive validation of flow elements.
 * * Processes each element sequentially. If validation [FieldScope.checkAsync]
 * takes time, the next element will wait for the previous one to complete.
 * It is ideal for processing queues of data where order is important.
 *
 * ### Example:
 * ```kotlin
 * userFlow.validateReactive {
 *      validateField({name}) {
 *          check("Empty name", "ERR_EMPTY_NAME") {
 *              it.isNotEmpty()
 *          }
 *      }
 * }
 * ```
 * @param T The type of data in the stream
 * @param block DSL-block for describing the validation rules
 * @return A stream with validation results [ValidatorResult] for each incoming item
 * @see [validateReactiveLatest] for UI scenarios with cancellation of previous tasks
 * @see [validateParallel] for high performance
 */
inline fun <T> Flow<T>.validateReactive(
    crossinline block: suspend ValidatorScope<T>.() -> Unit
): Flow<ValidatorResult> = map { data ->
    validateSingle(data, block)
}

/**
 * Performs reactive validation of flow elements.
 * * Processing the latest item. If validation [FieldScope.checkAsync]
 * takes time, and new validation element arrives before the previous one is validated, the old validation is
 * cancelled.
 *
 * ### Example:
 * ```kotlin
 * userFlow.validateReactiveLatest {
 *      validateField({name}) {
 *          check("Empty name", "ERR_EMPTY_NAME") {
 *              it.isNotEmpty()
 *          }
 *      }
 * }
 * ```
 * @param T The type of data in the stream
 * @param block DSL-block for describing the validation rules
 * @return A stream with validation results [ValidatorResult] for each incoming item
 * @see [validateReactive] for processes each element sequentially
 * @see [validateParallel] for high performance
 */
@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T> Flow<T>.validateReactiveLatest(
    crossinline block: suspend ValidatorScope<T>.() -> Unit
): Flow<ValidatorResult> = flatMapLatest { data ->
    flow { emit(validateSingle(data, block)) }
}

/**
 * Performs reactive validation of flow elements.
 * * Validates multiple items at once (default concurrency is 16). Processes multiple elements concurrently for maximum performance when order doesn't matter.
 *
 * ### Example:
 * ```kotlin
 * userFlow.validateParallel(concurrency = 5) {
 *      validateField({name}) {
 *          check("Empty name", "ERR_EMPTY_NAME") {
 *              it.isNotEmpty()
 *          }
 *      }
 * }
 * ```
 * @param concurrency controls the number of in-flight flows, at most concurrency flows are collected at the same time. By default, it is equal to 16
 * @param T The type of data in the stream
 * @param block DSL-block for describing the validation rules
 * @return A stream with validation results [ValidatorResult] for each incoming item
 * @see [validateReactive] for processes each element sequentially
 * @see [validateReactiveLatest] for UI scenarios with cancellation of previous tasks
 */
@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T> Flow<T>.validateParallel(
    concurrency: Int = 16,
    crossinline block: suspend ValidatorScope<T>.() -> Unit
): Flow<ValidatorResult> = flatMapMerge(concurrency) { data ->
    flow { emit(validateSingle(data, block)) }
}

suspend inline fun <T> validateSingle(
    data: T,
    crossinline block: suspend ValidatorScope<T>.() -> Unit
): ValidatorResult {
    val validator = ValidatorScope(data)
    validator.block()
    return validator.result
}