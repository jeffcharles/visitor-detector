package com.beyondtechnicallycorrect.visitordetector.validators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotEmptyValidator @Inject constructor() {
    fun isValid(value: CharSequence): Result {
        return if (value.length > 0) Result.VALID else Result.EMPTY
    }

    enum class Result {
        VALID,
        EMPTY
    }
}
