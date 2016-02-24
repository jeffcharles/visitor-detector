package com.beyondtechnicallycorrect.visitordetector.activities

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.widget.Toast
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceFetchingFailure
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.models.Device
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import com.beyondtechnicallycorrect.visitordetector.persistence.Devices
import com.beyondtechnicallycorrect.visitordetector.persistence.SavedDevice
import de.greenrobot.event.EventBus
import org.funktionale.either.Either
import timber.log.Timber
import javax.inject.Inject

class DevicesActivity : FragmentActivity() {

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject lateinit var eventBus: EventBus
    @Inject lateinit var devicePersistence: DevicePersistence
    @Inject lateinit var alarmSchedulingHelper: AlarmSchedulingHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as VisitorDetectorApplication).getApplicationComponent().inject(this)

        alarmSchedulingHelper.setupAlarm()

        setContentView(R.layout.activity_devices)

        val pager = this.findViewById(R.id.pager) as ViewPager
        val savedDevices = devicePersistence.getSavedDevices()
        val adapter =
            PagerAdapter(this.supportFragmentManager, this, eventBus, devicePersistence, savedDevices)
        pager.adapter = adapter
        val tabLayout = this.findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        GetDevicesTask(this, devicesOnRouterProvider, adapter).execute()
    }

    private class GetDevicesTask(
        val context: Context,
        val devicesOnRouterProvider: DevicesOnRouterProvider,
        val adapter: PagerAdapter
    ) : AsyncTask<Void, Void, Either<DeviceFetchingFailure, List<RouterDevice>>>() {

        override fun doInBackground(
            vararg params: Void?
        ): Either<DeviceFetchingFailure, List<RouterDevice>> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(
            connectedDevices: Either<DeviceFetchingFailure, List<RouterDevice>>
        ) {
            Timber.v("Finished getting devices")
            when (connectedDevices) {
                is Either.Left -> Toast.makeText(context, R.string.error_fetching_devices, Toast.LENGTH_LONG).show()
                is Either.Right -> adapter.setConnectedDevices(connectedDevices.right().get())
            }
        }
    }

    private class PagerAdapter(
        fm: FragmentManager,
        val context: Context,
        val eventBus: EventBus,
        val devicePersistence: DevicePersistence,
        val savedDevices: Devices
    ) : FragmentPagerAdapter(fm) {

        private val visitorDevicesList: MutableList<Device>
        private val homeDevicesList: MutableList<Device>
        private val unclassifiedDevicesFragment: DevicesFragment
        private val visitorDevicesFragment: DevicesFragment
        private val homeDevicesFragment: DevicesFragment

        init {
            visitorDevicesList =
                savedDevices.visitorDevices.map { Device(macAddress = it.macAddress, hostName = null) }.toMutableList()
            homeDevicesList =
                savedDevices.homeDevices.map { Device(macAddress = it.macAddress, hostName = null) }.toMutableList()
            unclassifiedDevicesFragment = DevicesFragment(eventBus, mutableListOf())
            visitorDevicesFragment = DevicesFragment(eventBus, visitorDevicesList)
            homeDevicesFragment = DevicesFragment(eventBus, homeDevicesList)
            eventBus.register(this)
        }

        override fun getCount(): Int {
            return 3;
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> unclassifiedDevicesFragment
                1 -> visitorDevicesFragment
                2 -> homeDevicesFragment
                else -> throw IllegalArgumentException("Received invalid position argument")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> context.getString(R.string.unclassified_devices_tab_title)
                1 -> context.getString(R.string.visitor_devices_tab_title)
                2 -> context.getString(R.string.home_devices_tab_title)
                else -> throw IllegalArgumentException("Received invalid position argument")
            }
        }

        // used by EventBus
        fun onEvent(event: DevicesMovedToHomeList) {
            homeDevicesFragment.addDevices(event.devices)
            save()
        }

        // used by EventBus
        fun onEvent(event: DevicesMovedToVisitorList) {
            visitorDevicesFragment.addDevices(event.devices)
            save()
        }

        fun setConnectedDevices(connectedDevices: List<RouterDevice>) {
            val connectedDevicesByMacAddress = connectedDevices.associateBy { it.macAddress }
            val homeMacAddresses = homeDevicesList.map { it.macAddress }.toSet()
            val visitorMacAddresses = visitorDevicesList.map { it.macAddress }.toSet()
            val unclassifiedDevices =
                (connectedDevicesByMacAddress.keys - homeMacAddresses - visitorMacAddresses)
                    .map { connectedDevicesByMacAddress[it]!! }
                    .toMutableList()
            unclassifiedDevicesFragment.setDevices(unclassifiedDevices)
        }

        private fun save() {
            devicePersistence.saveDevices(
                Devices(
                    visitorDevices = visitorDevicesList.map { SavedDevice(macAddress = it.macAddress) },
                    homeDevices = homeDevicesList.map { SavedDevice(macAddress = it.macAddress) }
                )
            )
        }
    }
}
