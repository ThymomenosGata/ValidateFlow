package org.wordy.validate.flow

/**
 * ValidatorResult is data class for receives validation state:
 *
 * @param errors A list of failures for the current item.
 * @param isValid An optional machine-readable string (e.g., `ERR_AUTH_01`).
 */
data class ValidatorResult(
    private val _errors: MutableList<ValidatorError> = mutableListOf(),
    var isValid: Boolean = true
) {
    val errors: List<ValidatorError>
        get() = _errors.toList()

    fun addError(message: String, code: String? = null) {
        _errors.add(
            ValidatorError(
                message = message,
                code = code
            )
        )
        isValid = false
    }
}
