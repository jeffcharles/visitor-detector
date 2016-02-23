package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.app.Notification
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
import dagger.Module
import dagger.Provides
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var runner: Runner

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Starting onReceive")
        (context.applicationContext as VisitorDetectorApplication).getApplicationComponent().inject(this)

        runner.start()
        GetConnectedDevicesTask(context, runner).execute()
    }

    private class GetConnectedDevicesTask(
        val context: Context,
        val runner: Runner
    ) : AsyncTask<Void, Void, List<RouterDevice>>() {

        override fun doInBackground(vararg params: Void?): List<RouterDevice> {
            return runner.inBackground()
        }

        override fun onPostExecute(connectedDevices: List<RouterDevice>) {
            runner.withResults(connectedDevices, context)
        }
    }

    class Runner @Inject constructor(
        val alarmSchedulingHelper: AlarmSchedulingHelper,
        val devicesOnRouterProvider: DevicesOnRouterProvider,
        val devicePersistence: DevicePersistence,
        val notificationManager: NotificationManager,
        val notificationHelper: NotificationHelper
    ) {
        fun start() {
            alarmSchedulingHelper.setupAlarm()
        }

        fun inBackground(): List<RouterDevice> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        fun withResults(connectedDevices: List<RouterDevice>, context: Context) {
            val savedDevices = devicePersistence.getSavedDevices()
            val nonHomeDevices : Set<String> =
                connectedDevices.map { it.macAddress }.toSet() - savedDevices.homeDevices.map { it.macAddress }.toSet()
            if (nonHomeDevices.any()) {
                Timber.d("At least one non-home device")
                val notification = notificationHelper.create(context)
                val NOTIFICATION_ID = 1
                notificationManager.notify(NOTIFICATION_ID, notification)
                return;
            }
            Timber.d("No non-home devices")
        }
    }

    interface NotificationHelper {
        fun create(context: Context): Notification
    }

    class NotificationHelperImpl @Inject constructor() : NotificationHelper {
        override fun create(context: Context): Notification {
            return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_person_white_24dp)
                .setContentTitle(context.getString(R.string.visitor_detected_notification_title))
                .setContentText(context.getString(R.string.visitor_detected_notification_text))
                .build()
        }
    }

    @Module
    class AlarmReceiverModule {
        @Provides @Singleton fun provideNotificationHelper(notificationHelperImpl: NotificationHelperImpl): NotificationHelper {
            return notificationHelperImpl
        }
    }
}
