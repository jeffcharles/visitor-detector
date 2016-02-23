package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import dagger.Module

@Module(
    includes = arrayOf(AlarmReceiver.AlarmReceiverModule::class)
)
class BroadcastReceiversModule
