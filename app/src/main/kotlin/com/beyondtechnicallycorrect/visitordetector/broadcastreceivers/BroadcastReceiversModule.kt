package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations.ImplementationsModule
import dagger.Module

@Module(
    includes = arrayOf(ImplementationsModule::class)
)
class BroadcastReceiversModule
