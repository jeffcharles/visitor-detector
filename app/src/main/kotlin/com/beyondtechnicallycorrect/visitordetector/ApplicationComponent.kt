package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.activities.DevicesActivity
import dagger.Component

@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    fun inject(activity: DevicesActivity)
}
