package com.beyondtechnicallycorrect.visitordetector.deviceproviders

interface RouterApiFactory {
    fun createRouterApi(routerIpAddress: String): RouterApi
}
