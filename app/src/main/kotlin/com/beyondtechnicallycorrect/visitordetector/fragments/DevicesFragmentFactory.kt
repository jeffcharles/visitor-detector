package com.beyondtechnicallycorrect.visitordetector.fragments

import de.greenrobot.event.EventBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class DevicesFragmentFactory @Inject constructor(val eventBus: EventBus) {
    public fun create(): DevicesFragment {
        return DevicesFragment(eventBus)
    }
}
