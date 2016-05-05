package com.beyondtechnicallycorrect.visitordetector

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.BroadcastReceiversModule
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceProvidersModule
import com.beyondtechnicallycorrect.visitordetector.persistence.PersistenceModule
import com.beyondtechnicallycorrect.visitordetector.settings.SettingsModule
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import de.greenrobot.event.EventBus
import javax.inject.Named
import javax.inject.Singleton

@Module(
    includes = arrayOf(
        BroadcastReceiversModule::class,
        DeviceProvidersModule::class,
        PersistenceModule::class,
        SettingsModule::class
    )
)
class ApplicationModule(val applicationContext: Context) {
    @Provides @Singleton fun provideAlarmManager(): AlarmManager {
        return applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides @Singleton fun provideAlarmSchedulingHelper(alarmSchedulingHelper: AlarmSchedulingHelperImpl): AlarmSchedulingHelper {
        return alarmSchedulingHelper
    }

    @Provides @Singleton @Named("applicationContext") fun provideApplicationContext(): Context {
        return applicationContext
    }

    @Provides @Singleton fun provideEventBus(): EventBus {
        return EventBus.getDefault()
    }

    @Provides @Singleton fun provideGson(): Gson {
        return Gson()
    }

    @Provides @Singleton fun provideNotificationManager(): NotificationManager {
        return applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides @Singleton fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    @Provides @Singleton fun provideWifiManager(): WifiManager {
        return applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
}
