package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import org.funktionale.either.Either

interface DevicesOnRouterProvider {
    fun getDevicesOnRouter(): Either<DeviceFetchingFailure, List<RouterDevice>>
}
