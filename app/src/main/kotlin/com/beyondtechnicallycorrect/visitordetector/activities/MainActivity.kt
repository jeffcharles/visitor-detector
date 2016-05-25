package com.beyondtechnicallycorrect.visitordetector.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesTabsFragment
import com.beyondtechnicallycorrect.visitordetector.fragments.WelcomeFragment
import com.beyondtechnicallycorrect.visitordetector.models.Device
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsGetter
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), DevicesFragment.ArgumentProvider, WelcomeFragment.Callbacks {

    @Inject lateinit var routerSettingsGetter: RouterSettingsGetter

    private val devicesTabsFragmentTag = "devicesTabs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.activity_main)

        (this.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)

        if (savedInstanceState != null) {
            return
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (routerSettingsGetter.areRouterSettingsSet()) {
            fragmentTransaction.add(R.id.fragment_container, DevicesTabsFragment(), devicesTabsFragmentTag)
        } else {
            fragmentTransaction.add(R.id.fragment_container, WelcomeFragment())
        }
        fragmentTransaction.commit()
    }

    override fun doneEnteringSettings() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, DevicesTabsFragment(), devicesTabsFragmentTag)
            .commit()
    }

    override fun getDeviceList(deviceType: Int): MutableList<Device> {
        return getDevicesTabsFragment().getDeviceList(deviceType)
    }

    override fun setActionMode(actionMode: ActionMode?) {
        getDevicesTabsFragment().setActionMode(actionMode)
    }

    override fun setFragmentForType(deviceType: Int, devicesFragment: DevicesFragment) {
        getDevicesTabsFragment().setFragmentForType(deviceType, devicesFragment)
    }

    private fun getDevicesTabsFragment(): DevicesTabsFragment {
        return supportFragmentManager.findFragmentByTag(devicesTabsFragmentTag) as DevicesTabsFragment
    }
}
