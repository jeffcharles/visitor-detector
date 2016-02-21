package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations

import android.app.Notification
import android.content.Context
import android.support.v7.app.NotificationCompat
import com.beyondtechnicallycorrect.visitordetector.R
import javax.inject.Inject

class NotificationHelperImpl @Inject constructor() : NotificationHelper {
    override fun create(context: Context): Notification {
        return NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_person_white_24dp)
            .setContentTitle(context.getString(R.string.visitor_detected_notification_title))
            .setContentText(context.getString(R.string.visitor_detected_notification_text))
            .build()
    }
}
