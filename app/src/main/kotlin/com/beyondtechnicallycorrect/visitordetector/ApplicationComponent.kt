package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.activities.DevicesActivity
import com.beyondtechnicallycorrect.visitordetector.activities.WelcomeActivity
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.AlarmReceiver
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.BootReceiver
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesTabsFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class))
@Singleton
interface ApplicationComponent {
    fun inject(activity: DevicesActivity)
    fun inject(activity: WelcomeActivity)
    fun inject(receiver: AlarmReceiver)
    fun inject(receiver: BootReceiver)
    fun inject(fragment: DevicesFragment)
    fun inject(fragment: DevicesTabsFragment)
}
