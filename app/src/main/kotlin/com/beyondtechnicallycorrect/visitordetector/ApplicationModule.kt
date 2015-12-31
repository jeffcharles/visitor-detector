package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceProvidersModule
import dagger.Module

@Module(
    includes = arrayOf(DeviceProvidersModule::class)
)
class ApplicationModule
