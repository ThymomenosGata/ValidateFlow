package org.wordy.validate.flow

/**
 * ValidatorError is data class for description an error
 *
 * @param message `true` if all checks passed.
 * @param code An optional machine-readable string (e.g., `ERR_AUTH_01`).
 */
data class ValidatorError(
    val message: String,
    val code: String? = null
)
