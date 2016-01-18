package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.activities.DevicesActivity
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class))
@Singleton
interface ApplicationComponent {
    fun inject(activity: DevicesActivity)
}
