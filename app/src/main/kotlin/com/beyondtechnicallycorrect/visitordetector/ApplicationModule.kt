package com.beyondtechnicallycorrect.visitordetector

import android.content.Context
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceProvidersModule
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import de.greenrobot.event.EventBus
import javax.inject.Named
import javax.inject.Singleton

@Module(
    includes = arrayOf(DeviceProvidersModule::class)
)
class ApplicationModule(val applicationContext: Context) {
    @Provides @Singleton @Named("applicationContext") fun provideApplicationContext(): Context {
        return applicationContext
    }

    @Provides @Singleton fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }

    @Provides @Singleton fun provideGson(): Gson {
        return Gson()
    }
}
