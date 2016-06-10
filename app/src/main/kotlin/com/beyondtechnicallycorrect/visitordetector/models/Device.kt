package com.beyondtechnicallycorrect.visitordetector.models

data class Device(val macAddress: String, val hostName: String?, var description: String) {
    override fun toString(): String {
        return listOf(macAddress, hostName, description).filterNot { it.isNullOrBlank() }.joinToString( " - " )
    }
}
