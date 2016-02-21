package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations

import android.app.Notification
import android.content.Context

interface NotificationHelper {
    fun create(context: Context): Notification
}
