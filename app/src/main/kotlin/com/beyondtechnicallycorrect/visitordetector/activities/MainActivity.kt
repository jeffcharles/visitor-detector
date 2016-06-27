package com.beyondtechnicallycorrect.visitordetector.activities

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.MenuItem
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.fragments.*
import com.beyondtechnicallycorrect.visitordetector.models.Device
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsGetter
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), DevicesFragment.ArgumentProvider, DevicesTabsFragment.Callbacks, WelcomeFragment.Callbacks {

    @Inject lateinit var routerSettingsGetter: RouterSettingsGetter

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer: DrawerLayout

    private val devicesTabsFragmentTag = "devicesTabs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.activity_main)

        (this.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)

        this.setSupportActionBar(this.findViewById(R.id.toolbar) as Toolbar)
        drawer = this.findViewById(R.id.drawer) as DrawerLayout
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawer,
            R.string.main_activity_open_drawer,
            R.string.main_activity_close_drawer
        )
        drawer.setDrawerListener(drawerToggle)
        val navigationView = this.findViewById(R.id.navigation_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(fun(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.drawer_devices -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, DevicesTabsFragment(), devicesTabsFragmentTag)
                    .commit()
                R.id.drawer_settings -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()
                R.id.drawer_license_attributions -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, LicenseAttributionsFragment())
                    .commit()
                else -> throw IllegalArgumentException("Invalid menu item")
            }
            menuItem.isChecked = true
            drawer.closeDrawers()
            return true
        })

        if (savedInstanceState != null) {
            return
        }

        navigationView.menu.findItem(R.id.drawer_devices).isChecked = true
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (routerSettingsGetter.areRouterSettingsSet()) {
            fragmentTransaction.add(R.id.fragment_container, DevicesTabsFragment(), devicesTabsFragmentTag)
        } else {
            fragmentTransaction.add(R.id.fragment_container, WelcomeFragment())
        }
        fragmentTransaction.commit()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Timber.v("onPostCreate")
        drawerToggle.syncState();
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun disableNavigationDrawer() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun doneEnteringSettings() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, DevicesTabsFragment(), devicesTabsFragmentTag)
            .commit()
    }

    override fun enableNavigationDrawer() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
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
