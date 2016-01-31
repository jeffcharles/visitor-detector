package com.beyondtechnicallycorrect.visitordetector

import android.app.Application
import timber.log.Timber

class VisitorDetectorApplication : Application() {

    private lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent =
            DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    public fun getApplicationComponent(): ApplicationComponent {
        return applicationComponent
    }
}
