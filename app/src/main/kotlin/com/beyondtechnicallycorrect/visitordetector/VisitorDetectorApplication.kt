package com.beyondtechnicallycorrect.visitordetector

import android.app.Application

class VisitorDetectorApplication : Application() {

    private lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.create()
    }

    public fun getApplicationComponent(): ApplicationComponent {
        return applicationComponent
    }
}
