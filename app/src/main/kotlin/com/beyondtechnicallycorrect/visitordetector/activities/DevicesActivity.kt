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
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import com.beyondtechnicallycorrect.visitordetector.persistence.Devices
import de.greenrobot.event.EventBus
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

        GetDevicesTask(devicesOnRouterProvider, adapter).execute()
    }

    private class GetDevicesTask(val devicesOnRouterProvider: DevicesOnRouterProvider, val adapter: PagerAdapter) : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(connectedDevices: List<String>) {
            Timber.v("Finished getting devices")
            adapter.setConnectedDevices(connectedDevices)
        }
    }

    private class PagerAdapter(
        fm: FragmentManager,
        val context: Context,
        val eventBus: EventBus,
        val devicePersistence: DevicePersistence,
        val savedDevices: Devices
    ) : FragmentPagerAdapter(fm) {

        private val visitorDevicesList: MutableList<String>
        private val homeDevicesList: MutableList<String>
        private val unclassifiedDevicesFragment: DevicesFragment
        private val visitorDevicesFragment: DevicesFragment
        private val homeDevicesFragment: DevicesFragment

        init {
            visitorDevicesList = savedDevices.visitorDevices.toMutableList()
            homeDevicesList = savedDevices.homeDevices.toMutableList()
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
        public fun onEvent(event: DevicesMovedToHomeList) {
            homeDevicesFragment.addDevices(event.devices)
            save()
        }

        // used by EventBus
        public fun onEvent(event: DevicesMovedToVisitorList) {
            visitorDevicesFragment.addDevices(event.devices)
            save()
        }

        public fun setConnectedDevices(connectedDevices: List<String>) {
            val unclassifiedDevices =
                (connectedDevices.toSet() - homeDevicesList.toSet() - visitorDevicesList.toSet()).toMutableList()
            unclassifiedDevicesFragment.setDevices(unclassifiedDevices)
        }

        private fun save() {
            devicePersistence.saveDevices(
                Devices(visitorDevices = visitorDevicesList, homeDevices = homeDevicesList)
            )
        }
    }
}
