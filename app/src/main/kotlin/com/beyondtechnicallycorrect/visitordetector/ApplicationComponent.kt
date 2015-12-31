package com.beyondtechnicallycorrect.visitordetector

import dagger.Component

@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    fun inject(activity: MainActivity)
}
