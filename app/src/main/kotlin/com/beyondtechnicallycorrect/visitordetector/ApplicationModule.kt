package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceProvidersModule
import dagger.Module
import dagger.Provides
import de.greenrobot.event.EventBus
import javax.inject.Singleton

@Module(
    includes = arrayOf(DeviceProvidersModule::class)
)
class ApplicationModule {
    @Provides @Singleton fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }
}
