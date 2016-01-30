package com.beyondtechnicallycorrect.visitordetector.activities

import android.app.ActionBar
import android.app.ListActivity
import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import de.greenrobot.event.EventBus
import timber.log.Timber
import javax.inject.Inject

class DevicesActivity : FragmentActivity() {

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject lateinit var eventBus: EventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as VisitorDetectorApplication).getApplicationComponent().inject(this)

        setContentView(R.layout.activity_devices)

        val pager = this.findViewById(R.id.pager) as ViewPager
        val adapter = PagerAdapter(this.supportFragmentManager, this, eventBus)
        pager.adapter = adapter
        val tabLayout = this.findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        GetDevicesTask(devicesOnRouterProvider, adapter).execute()
    }

    private class GetDevicesTask(val devicesOnRouterProvider: DevicesOnRouterProvider, val adapter: PagerAdapter) : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(devices: List<String>) {
            Timber.v("Finished getting devices timber")
            adapter.setUnclassifiedDevices(devices)
        }
    }

    private class PagerAdapter(fm: FragmentManager, val context: Context, val eventBus: EventBus) : FragmentPagerAdapter(fm) {

        private val visitorDevicesList: ObservableList<String>
        private val homeDevicesList: ObservableList<String>
        private val unclassifiedDevicesFragment: DevicesFragment
        private val visitorDevicesFragment: DevicesFragment
        private val homeDevicesFragment: DevicesFragment

        init {
            val devicesListChangedCallback = DevicesListChangedCallback()
            visitorDevicesList = ObservableArrayList<String>()
            visitorDevicesList.addOnListChangedCallback(devicesListChangedCallback)
            homeDevicesList = ObservableArrayList<String>()
            homeDevicesList.addOnListChangedCallback(devicesListChangedCallback)
            unclassifiedDevicesFragment = DevicesFragment(eventBus, arrayListOf())
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
        }

        // used by EventBus
        public fun onEvent(event: DevicesMovedToVisitorList) {
            visitorDevicesFragment.addDevices(event.devices)
        }

        public fun setUnclassifiedDevices(devices: List<String>) {
            unclassifiedDevicesFragment.setDevices(devices)
        }
    }

    private class DevicesListChangedCallback : ObservableList.OnListChangedCallback<ObservableList<String>>() {

        override fun onItemRangeMoved(sender: ObservableList<String>, fromPosition: Int, toPosition: Int, itemCount: Int) {
            Timber.v("onItemRangeMoved")
        }

        override fun onChanged(sender: ObservableList<String>) {
            Timber.v("onChanged")
        }

        override fun onItemRangeInserted(sender: ObservableList<String>, positionStart: Int, itemCount: Int) {
            Timber.v("onItemRangeInserted")
        }

        override fun onItemRangeRemoved(sender: ObservableList<String>, positionStart: Int, itemCount: Int) {
            Timber.v("onItemRangeRemoved")
        }

        override fun onItemRangeChanged(sender: ObservableList<String>, positionStart: Int, itemCount: Int) {
            Timber.v("onItemRangeChanged")
        }

    }
}
