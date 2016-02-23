package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.app.NotificationManager
import android.content.Context
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import com.beyondtechnicallycorrect.visitordetector.persistence.Devices
import com.beyondtechnicallycorrect.visitordetector.persistence.SavedDevice
import org.junit.Test
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
    fun withResults_shouldNotifyIfAnyUnclassifiedDevices() {
        val notificationManager = mock(NotificationManager::class.java)
        val runner = createAlarmReceiverRunner(notificationManager, Devices(listOf(), listOf()))

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = listOf(RouterDevice(macAddress = "123456", hostName = "")),
            context = context
        )

        verify(notificationManager).notify(anyInt(), anyObject())
    }

    @Test
    fun withResults_shouldNotifyIfAnyVisitorDevices() {
        val notificationManager = mock(NotificationManager::class.java)
        val runner = createAlarmReceiverRunner(
            notificationManager,
            Devices(
                homeDevices = listOf(),
                visitorDevices = listOf(SavedDevice(macAddress = "123456"))
            )
        )

        val context = mock(Context::class.java)
        runner.withResults(
            connectedDevices = listOf(RouterDevice(macAddress = "123456", hostName = "")),
            context = context
        )

        verify(notificationManager).notify(anyInt(), anyObject())
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
            connectedDevices = listOf(RouterDevice(macAddress = "123456", hostName = "")),
            context = context
        )

        verify(notificationManager, never()).notify(anyInt(), anyObject())
    }

    private fun createAlarmReceiverRunner(
        notificationManager: NotificationManager,
        savedDevices: Devices
    ): AlarmReceiver.Runner {
        val alarmSchedulingHelper = mock(AlarmSchedulingHelper::class.java)
        val devicesOnRouterProvider = mock(DevicesOnRouterProvider::class.java)
        val devicePersistence = mock(DevicePersistence::class.java)
        `when`(devicePersistence.getSavedDevices()).thenReturn(savedDevices)
        val notificationHelper = mock(AlarmReceiver.NotificationHelper::class.java)
        return AlarmReceiver.Runner(
            alarmSchedulingHelper,
            devicesOnRouterProvider,
            devicePersistence,
            notificationManager,
            notificationHelper
        )
    }
}
