package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import com.google.gson.annotations.SerializedName

class ArpTableEntry(
    @SerializedName("HW address") val hwAddress: String,
    @SerializedName("IP address") val ipAddress: String
)
