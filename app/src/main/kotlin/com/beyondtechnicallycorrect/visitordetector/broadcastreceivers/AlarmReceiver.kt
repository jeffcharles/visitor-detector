package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.NotificationCompat
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import timber.log.Timber
import javax.inject.Inject

public class AlarmReceiver : BroadcastReceiver() {

    @Inject public lateinit var alarmSchedulingHelper: AlarmSchedulingHelper
    @Inject public lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject public lateinit var devicePersistence: DevicePersistence

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Starting onReceive")
        (context.applicationContext as VisitorDetectorApplication).getApplicationComponent().inject(this)

        alarmSchedulingHelper.setupAlarm()

        GetConnectedDevicesTask(context, devicesOnRouterProvider, devicePersistence).execute()
    }

    private class GetConnectedDevicesTask(
        val context: Context,
        val devicesOnRouterProvider: DevicesOnRouterProvider,
        val devicePersistence: DevicePersistence
    ) : AsyncTask<Void, Void, List<RouterDevice>>() {
        override fun doInBackground(vararg params: Void?): List<RouterDevice> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(connectedDevices: List<RouterDevice>) {
            val savedDevices = devicePersistence.getSavedDevices()
            val nonHomeDevices =
                connectedDevices.map { it.macAddress }.toSet() - savedDevices.homeDevices.toSet()
            if (nonHomeDevices.any()) {
                Timber.d("At least one non-home device")
                val notification = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_person_white_24dp)
                    .setContentTitle(context.getString(R.string.visitor_detected_notification_title))
                    .setContentText(context.getString(R.string.visitor_detected_notification_text))
                    .build()
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val NOTIFICATION_ID = 1
                notificationManager.notify(NOTIFICATION_ID, notification)
                return;
            }
            Timber.d("No non-home devices")
        }
    }
}
