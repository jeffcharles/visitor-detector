package com.beyondtechnicallycorrect.visitordetector.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesTabsFragment
import com.beyondtechnicallycorrect.visitordetector.models.Device
import timber.log.Timber

class DevicesActivity : AppCompatActivity(), DevicesFragment.ArgumentProvider {

    val devicesTabsFragmentTag = "devicesTabs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.activity_devices)

        if (savedInstanceState != null) {
            return
        }
        val devicesTabsFragment = DevicesTabsFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, devicesTabsFragment, devicesTabsFragmentTag)
            .commit()
    }

    override fun getDeviceList(deviceType: Int): MutableList<Device> {
        return getDevicesTabsFragment().getDeviceList(deviceType)
    }

    override fun setFragmentForType(deviceType: Int, devicesFragment: DevicesFragment) {
        getDevicesTabsFragment().setFragmentForType(deviceType, devicesFragment)
    }

    private fun getDevicesTabsFragment(): DevicesTabsFragment {
        return supportFragmentManager.findFragmentByTag(devicesTabsFragmentTag) as DevicesTabsFragment
    }
}
