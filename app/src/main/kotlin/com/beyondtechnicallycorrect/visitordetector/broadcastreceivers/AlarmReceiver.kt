package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.NotificationCompat
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.activities.MainActivity
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceFetchingFailure
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import dagger.Module
import dagger.Provides
import org.funktionale.either.Either
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
    ) : AsyncTask<Void, Void, Either<DeviceFetchingFailure, List<RouterDevice>>>() {

        override fun doInBackground(
            vararg params: Void?
        ): Either<DeviceFetchingFailure, List<RouterDevice>> {
            return runner.inBackground()
        }

        override fun onPostExecute(
            connectedDevices: Either<DeviceFetchingFailure, List<RouterDevice>>
        ) {
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

        fun inBackground(): Either<DeviceFetchingFailure, List<RouterDevice>> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        fun withResults(
            connectedDevices: Either<DeviceFetchingFailure, List<RouterDevice>>, context: Context
        ) {
            if (connectedDevices.isLeft()) {
                val notification = notificationHelper.createError(context)
                val NOTIFICATION_ID = 1
                notificationManager.notify(NOTIFICATION_ID, notification)
                return
            }
            val savedDevices = devicePersistence.getSavedDevices()
            val nonHomeDevices : Set<String> =
                connectedDevices.right().get().map { it.macAddress }.toSet() - savedDevices.homeDevices.map { it.macAddress }.toSet()
            if (!nonHomeDevices.any()) {
                Timber.d("No non-home devices")
                return
            }
            Timber.d("At least one non-home device")
            val notification = notificationHelper.createVisitorDetected(context)
            val NOTIFICATION_ID = 1
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    interface NotificationHelper {
        fun createError(context: Context): Notification
        fun createVisitorDetected(context: Context): Notification
    }

    class NotificationHelperImpl @Inject constructor() : NotificationHelper {

        override fun createError(context: Context): Notification {
            return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_error_white_24dp)
                .setContentTitle(context.getString(R.string.error_fetching_devices_notification_title))
                .setContentText(context.getString(R.string.error_fetching_devices))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(createPendingIntent(context))
                .setAutoCancel(true)
                .build()
        }

        override fun createVisitorDetected(context: Context): Notification {
            return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_person_white_24dp)
                .setContentTitle(context.getString(R.string.visitor_detected_notification_title))
                .setContentText(context.getString(R.string.visitor_detected_notification_text))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(createPendingIntent(context))
                .setAutoCancel(true)
                .build()
        }

        private fun createPendingIntent(context: Context): PendingIntent {
            val requestCode = 0
            return PendingIntent.getActivity(
                context,
                requestCode,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    @Module
    class AlarmReceiverModule {
        @Provides @Singleton fun provideNotificationHelper(notificationHelperImpl: NotificationHelperImpl): NotificationHelper {
            return notificationHelperImpl
        }
    }
}
