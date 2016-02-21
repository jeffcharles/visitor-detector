package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations

import android.app.NotificationManager
import android.content.Context
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmReceiverImpl @Inject constructor(
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
