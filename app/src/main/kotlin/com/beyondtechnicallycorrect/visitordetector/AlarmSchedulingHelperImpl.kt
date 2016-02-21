package com.beyondtechnicallycorrect.visitordetector

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.AlarmReceiver
import org.joda.time.DateTime
import org.joda.time.LocalTime
import javax.inject.Inject
import javax.inject.Named

class AlarmSchedulingHelperImpl @Inject constructor(
    val alarmManager: AlarmManager,
    @Named("applicationContext") val applicationContext: Context
) : AlarmSchedulingHelper {
    override fun setupAlarm() {
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val now = DateTime()
        val timeToExecute = LocalTime(23, 0)
        val isCloseToOrAfterTimeToExecute = now.isAfter(now.withTime(timeToExecute).minusMinutes(1))
        val nextTimeToExecute =
            if (isCloseToOrAfterTimeToExecute)
                now.plusDays(1).withTime(timeToExecute)
            else
                now.withTime(timeToExecute)
        val nextTimeToExecuteMillis = nextTimeToExecute.millis
        if (Build.VERSION.SDK_INT < 23) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextTimeToExecuteMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTimeToExecuteMillis,
                pendingIntent
            )
        }
    }
}
