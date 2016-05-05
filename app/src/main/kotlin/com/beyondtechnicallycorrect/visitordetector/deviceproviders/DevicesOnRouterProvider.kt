package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettings
import org.funktionale.either.Either

interface DevicesOnRouterProvider {
    fun getDevicesOnRouter(): Either<DeviceFetchingFailure, List<RouterDevice>>

    fun getDevicesOnRouter(
        routerSettings: RouterSettings
    ): Either<DeviceFetchingFailure, List<RouterDevice>>

    interface OnHomeWifi {
        fun isOnHomeWifi(homeSsids: List<String>): Boolean
    }
}
