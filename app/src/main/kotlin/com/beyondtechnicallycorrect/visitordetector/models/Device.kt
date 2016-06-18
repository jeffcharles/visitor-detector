package com.beyondtechnicallycorrect.visitordetector.models

data class Device(val macAddress: String, val hostName: String?, var description: String) {
    override fun toString(): String {
        return listOf(description, hostName, macAddress).filterNot { it.isNullOrBlank() }.joinToString( "\n" )
    }
}
