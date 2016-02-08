package com.beyondtechnicallycorrect.visitordetector.models

data class Device(val macAddress: String, val hostName: String?) {
    override fun toString(): String {
        val hostnamePart = if (hostName != null) "$hostName - " else ""
        return hostnamePart + macAddress
    }
}
