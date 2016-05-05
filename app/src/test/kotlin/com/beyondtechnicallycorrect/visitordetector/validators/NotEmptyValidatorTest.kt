package com.beyondtechnicallycorrect.visitordetector.validators

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NotEmptyValidatorTest(val value: String, val result: NotEmptyValidator.Result) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<out Any>> {
            return listOf(
                arrayOf("", NotEmptyValidator.Result.EMPTY),
                arrayOf("a", NotEmptyValidator.Result.VALID)
            )
        }
    }

    @Test
    fun isValidShouldWork() {
        val notEmptyValidator = NotEmptyValidator()
        assertThat(notEmptyValidator.isValid(value)).isEqualTo(result)
    }
}
