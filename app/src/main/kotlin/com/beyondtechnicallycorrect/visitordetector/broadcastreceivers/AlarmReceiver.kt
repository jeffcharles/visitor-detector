package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import timber.log.Timber
import javax.inject.Inject

public class AlarmReceiver : BroadcastReceiver() {

    @Inject public lateinit var alarmSchedulingHelper: AlarmSchedulingHelper

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Starting onReceive")
        (context.applicationContext as VisitorDetectorApplication).getApplicationComponent().inject(this)

        alarmSchedulingHelper.setupAlarm()
    }
}
