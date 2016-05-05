package com.beyondtechnicallycorrect.visitordetector.validators

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RouterIpAddressValidatorTest(val routerIpAddress: String, val result: RouterIpAddressValidator.Result) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<out Any>> {
            return listOf(
                arrayOf("", RouterIpAddressValidator.Result.EMPTY),
                arrayOf("a", RouterIpAddressValidator.Result.NOT_AN_IP_ADDRESS),
                arrayOf("8.8.8.8", RouterIpAddressValidator.Result.NOT_LOCAL_ADDRESS),
                arrayOf("10.0.0.1", RouterIpAddressValidator.Result.VALID),
                arrayOf("172.16.0.1", RouterIpAddressValidator.Result.VALID),
                arrayOf("192.168.1.1", RouterIpAddressValidator.Result.VALID),
                arrayOf("a::1", RouterIpAddressValidator.Result.NOT_LOCAL_ADDRESS),
                arrayOf("fd00::1", RouterIpAddressValidator.Result.VALID)
            )
        }
    }

    @Test
    fun isValidShouldWork() {
        val routerIpAddressValidator = RouterIpAddressValidator()
        assertThat(routerIpAddressValidator.isValid(routerIpAddress)).isEqualTo(result)
    }
}
