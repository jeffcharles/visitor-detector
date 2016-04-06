package com.beyondtechnicallycorrect.visitordetector.validators

import com.google.common.net.InetAddresses
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterIpAddressValidator @Inject constructor() {
    fun isValid(routerIpAddress: CharSequence): Result {
        if (routerIpAddress.length < 1) {
            return Result.EMPTY
        }
        val address: InetAddress
        try {
            address = InetAddresses.forString(routerIpAddress.toString())
        } catch (e: IllegalArgumentException) {
            return Result.NOT_AN_IP_ADDRESS
        }
        val isSiteLocal = address.isSiteLocalAddress
        if (isSiteLocal) {
            return Result.VALID
        }
        val isLocallyAssignedUniqueLocalAddress = address.address[0] == 0xfd.toByte()
        return if (isLocallyAssignedUniqueLocalAddress) Result.VALID else Result.NOT_LOCAL_ADDRESS
    }

    enum class Result {
        VALID,
        EMPTY,
        NOT_AN_IP_ADDRESS,
        NOT_LOCAL_ADDRESS
    }
}
