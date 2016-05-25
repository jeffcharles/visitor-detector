package com.beyondtechnicallycorrect.visitordetector

import com.beyondtechnicallycorrect.visitordetector.activities.MainActivity
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.AlarmReceiver
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.BootReceiver
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesTabsFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.WelcomeFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class))
@Singleton
interface ApplicationComponent {
    fun inject(activity: MainActivity)
    fun inject(receiver: AlarmReceiver)
    fun inject(receiver: BootReceiver)
    fun inject(fragment: DevicesFragment)
    fun inject(fragment: DevicesTabsFragment)
    fun inject(fragment: WelcomeFragment)
}
