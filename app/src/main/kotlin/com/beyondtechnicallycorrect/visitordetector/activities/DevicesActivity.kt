package com.beyondtechnicallycorrect.visitordetector.activities

import android.app.ActionBar
import android.app.ListActivity
import android.content.Context
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
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import javax.inject.Inject

class DevicesActivity : FragmentActivity() {

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as VisitorDetectorApplication).getApplicationComponent().inject(this)

        setContentView(R.layout.activity_devices)

        val pager = this.findViewById(R.id.pager) as ViewPager
        val adapter = PagerAdapter(this.supportFragmentManager, this)
        pager.adapter = adapter
        val tabLayout = this.findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        GetDevicesTask(devicesOnRouterProvider, adapter).execute()
    }

    private class GetDevicesTask(val devicesOnRouterProvider: DevicesOnRouterProvider, val adapter: PagerAdapter) : AsyncTask<Void, Void, Array<String>>() {
        override fun doInBackground(vararg params: Void?): Array<String>? {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(devices: Array<String>) {
            adapter.updateDevices(devices)
        }
    }

    private class PagerAdapter(fm: FragmentManager, val context: Context) : FragmentPagerAdapter(fm) {

        private val unclassifiedDevicesFragment: DevicesFragment
        private val visitorDevicesFragment: DevicesFragment
        private val homeDevicesFragment: DevicesFragment

        init {
            unclassifiedDevicesFragment = DevicesFragment()
            visitorDevicesFragment = DevicesFragment()
            homeDevicesFragment = DevicesFragment()
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

        public fun updateDevices(devices: Array<String>) {
            unclassifiedDevicesFragment.updateDevices(devices)
        }
    }
}
