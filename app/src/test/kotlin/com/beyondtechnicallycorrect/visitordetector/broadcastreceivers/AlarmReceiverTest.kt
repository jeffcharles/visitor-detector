package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceFetchingFailure
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import com.beyondtechnicallycorrect.visitordetector.persistence.Devices
import com.beyondtechnicallycorrect.visitordetector.persistence.SavedDevice
import org.funktionale.either.Either
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class AlarmReceiverTest {

    @Test
    fun start_shouldScheduleNextAlarm() {
        val alarmSchedulingHelper = mock(AlarmSchedulingHelper::class.java)
        val devicesOnRouterProvider = mock(DevicesOnRouterProvider::class.java)
        val devicePersistence = mock(DevicePersistence::class.java)
        val notificationManager = mock(NotificationManager::class.java)
        val notificationHelper = mock(AlarmReceiver.NotificationHelper::class.java)
        val runner = AlarmReceiver.Runner(
            alarmSchedulingHelper,
            devicesOnRouterProvider,
            devicePersistence,
            notificationManager,
            notificationHelper
        )

        runner.start()

        verify(alarmSchedulingHelper).setupAlarm()
    }

    @Test
    fun withResults_shouldNotifyIfError() {
        val notificationManager = mock(NotificationManager::class.java)
        val errorNotification = mock(Notification::class.java)
        val runner = createAlarmReceiverRunner(
            notificationManager,
            Devices(listOf(), listOf()),
            errorNotification = errorNotification
        )

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = Either.Left(DeviceFetchingFailure.Error),
            context = context
        )

        verify(notificationManager).notify(anyInt(), eq(errorNotification))
    }

    @Test
    fun withResults_shouldNotifyIfAnyUnclassifiedDevices() {
        val notificationManager = mock(NotificationManager::class.java)
        val detectedNotification = mock(Notification::class.java)
        val runner = createAlarmReceiverRunner(
            notificationManager,
            Devices(listOf(), listOf()),
            detectedNotification = detectedNotification
        )

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = Either.Right(listOf(RouterDevice(macAddress = "123456", hostName = ""))),
            context = context
        )

        verify(notificationManager).notify(anyInt(), eq(detectedNotification))
    }

    @Test
    fun withResults_shouldNotifyIfAnyVisitorDevices() {
        val notificationManager = mock(NotificationManager::class.java)
        val detectedNotification = mock(Notification::class.java)
        val runner = createAlarmReceiverRunner(
            notificationManager,
            Devices(
                homeDevices = listOf(),
                visitorDevices = listOf(SavedDevice(macAddress = "123456"))
            ),
            detectedNotification = detectedNotification
        )

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = Either.Right(listOf(RouterDevice(macAddress = "123456", hostName = ""))),
            context = context
        )

        verify(notificationManager).notify(anyInt(), eq(detectedNotification))
    }

    @Test
    fun withResults_shouldNotNotifyIfAllHomeDevices() {
        val notificationManager = mock(NotificationManager::class.java)
        val runner = createAlarmReceiverRunner(
            notificationManager,
            Devices(
                homeDevices = listOf(SavedDevice(macAddress = "123456")),
                visitorDevices = listOf()
            )
        )

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = Either.Right(listOf(RouterDevice(macAddress = "123456", hostName = ""))),
            context = context
        )

        verify(notificationManager, never()).notify(anyInt(), anyObject())
    }

    private fun createAlarmReceiverRunner(
        notificationManager: NotificationManager,
        savedDevices: Devices,
        errorNotification: Notification = mock(Notification::class.java),
        detectedNotification: Notification = mock(Notification::class.java)
    ): AlarmReceiver.Runner {
        val alarmSchedulingHelper = mock(AlarmSchedulingHelper::class.java)
        val devicesOnRouterProvider = mock(DevicesOnRouterProvider::class.java)
        val devicePersistence = mock(DevicePersistence::class.java)
        `when`(devicePersistence.getSavedDevices()).thenReturn(savedDevices)
        val notificationHelper = mock(AlarmReceiver.NotificationHelper::class.java)
        `when`(notificationHelper.createError(anyObject())).thenReturn(errorNotification)
        `when`(notificationHelper.createVisitorDetected(anyObject())).thenReturn(detectedNotification)
        return AlarmReceiver.Runner(
            alarmSchedulingHelper,
            devicesOnRouterProvider,
            devicePersistence,
            notificationManager,
            notificationHelper
        )
    }

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }
}
