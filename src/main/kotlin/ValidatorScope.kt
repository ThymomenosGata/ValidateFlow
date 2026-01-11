package org.wordy.validate.flow

@ValidatorDsl
class ValidatorScope<T>(val rootObject: T) {

    val result = ValidatorResult()

    /**
     * Switches the validation context to a specific field of the [rootObject] object.
     *
     * This function allows you to isolate checks for a single property (for example, [User.name]).
     * By using inline, calling this function does not create additional objects
     * in memory and does not reduce performance.
     *
     * The [FieldScope.check] and [FieldScope.checkAsync] methods become available inside the [block] block,
     * working directly with the value of the selected field.
     *
     * ### Example:
     * ```kotlin
     * validateField({name}) {
     *     check("Empty name", "ERR_EMPTY_NAME") {
     *         it.isNotEmpty()
     *     }
     * }
     * ```
     * @param R Is the type of field that will be validated.
     * @param value is a Lambda extractor that returns the field value from the [T] object.
     * @param block The suspended verification configuration block for this field.
     */
    suspend inline fun <R> validateField(
        value: T.() -> R,
        crossinline block: suspend FieldScope<R>.() -> Unit
    ) {
        val rValue = rootObject.value()
        FieldScope(rValue).block()
    }

    @ValidatorDsl
    inner class FieldScope<R>(val fieldValue: R) {
        /**
         * Performs synchronous field validation.
         * * It is used for checks that require calling not-suspend functions.
         * If the check returns `false`, an error is added to the final result.
         *
         * @param message A human-readable error description.
         * @param code An optional machine-readable string (e.g., `ERR_AUTH_01`).
         * @param block synchronous lambda that returns `true` if the data is valid
         */
        inline fun check(
            message: String,
            code: String,
            block: (R) -> Boolean
        ) {
            if (!block(fieldValue)) {
                this@ValidatorScope.result.addError(message, code)
            }
        }

        /**
         * Performs asynchronous field validation.
         * * It is used for checks that require calling suspend functions.
         * If the check returns `false`, an error is added to the final result.
         *
         * @param message A human-readable error description.
         * @param code An optional machine-readable string (e.g., `ERR_AUTH_01`).
         * @param block asynchronous lambda that returns `true` if the data is valid
         */
        suspend fun checkAsync(
            message: String,
            code: String,
            block: suspend (R) -> Boolean
        ) {
            if (!block(fieldValue)) {
                this@ValidatorScope.result.addError(message, code)
            }
        }
    }
}