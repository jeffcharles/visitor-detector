package com.beyondtechnicallycorrect.visitordetector

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.AlarmReceiver
import org.joda.time.DateTime
import org.joda.time.LocalTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class AlarmSchedulingHelperImpl @Inject constructor(
    val alarmManager: AlarmManager,
    @Named("applicationContext") val applicationContext: Context
) : AlarmSchedulingHelper {
    override fun setupAlarm() {
        Timber.v("Setting up alarm")
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val now = DateTime()
        val timeToExecute = LocalTime(23, 0)
        val isCloseToOrAfterTimeToExecute = now.isAfter(now.withTime(timeToExecute).minusSeconds(15))
        val nextTimeToExecute =
            if (isCloseToOrAfterTimeToExecute)
                now.plusDays(1).withTime(timeToExecute)
            else
                now.withTime(timeToExecute)
        Timber.d("Setting alarm for %s", nextTimeToExecute)
        val nextTimeToExecuteMillis = nextTimeToExecute.millis
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTimeToExecuteMillis,
            pendingIntent
        )
    }
}
