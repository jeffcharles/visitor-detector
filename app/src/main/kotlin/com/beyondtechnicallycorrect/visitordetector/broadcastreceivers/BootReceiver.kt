package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import timber.log.Timber
import javax.inject.Inject

class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmSchedulingHelper: AlarmSchedulingHelper

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Starting onReceive")
        if (!intent.action.equals("android.intent.action.BOOT_COMPLETED")) {
            Timber.w("Expecting %s, got %s", "android.intent.action.BOOT_COMPLETED", intent.action)
            return
        }
        (context.applicationContext as VisitorDetectorApplication).getApplicationComponent().inject(this)

        alarmSchedulingHelper.setupAlarm()
    }
}
