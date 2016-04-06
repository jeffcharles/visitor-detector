package com.beyondtechnicallycorrect.visitordetector.settings

data class RouterSettings(
    val homeNetworkSsids: List<String>,
    val routerIpAddress: String,
    val routerUsername: String,
    val routerPassword: String
)
