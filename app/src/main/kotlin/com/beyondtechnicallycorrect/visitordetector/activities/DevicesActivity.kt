package com.beyondtechnicallycorrect.visitordetector.activities

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.ApplicationComponent
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

class DevicesActivity : AppCompatActivity(), DevicesFragment.ArgumentProvider {

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject lateinit var eventBus: EventBus
    @Inject lateinit var devicePersistence: DevicePersistence
    @Inject lateinit var alarmSchedulingHelper: AlarmSchedulingHelper

    private lateinit var applicationComponent: ApplicationComponent

    private val visitorDevicesList: MutableList<Device> = mutableListOf()
    private val homeDevicesList: MutableList<Device> = mutableListOf()
    private val unclassifiedDevicesFragment: DevicesFragment = DevicesFragment()
    private val visitorDevicesFragment: DevicesFragment = DevicesFragment()
    private val homeDevicesFragment: DevicesFragment = DevicesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationComponent = (this.application as VisitorDetectorApplication).getApplicationComponent()
        applicationComponent.inject(this)

        alarmSchedulingHelper.setupAlarm()

        setContentView(R.layout.activity_devices)

        val pager = this.findViewById(R.id.pager) as ViewPager
        val savedDevices = devicePersistence.getSavedDevices()

        visitorDevicesList.addAll(
            savedDevices.visitorDevices.map { Device(macAddress = it.macAddress, hostName = null) }
        )
        homeDevicesList.addAll(
            savedDevices.homeDevices.map { Device(macAddress = it.macAddress, hostName = null) }
        )

        val adapter = PagerAdapter(
            this.supportFragmentManager,
            eventBus,
            this,
            devicePersistence,
            homeDevicesList,
            visitorDevicesList,
            unclassifiedDevicesFragment,
            visitorDevicesFragment,
            homeDevicesFragment
        )
        pager.adapter = adapter
        val tabLayout = this.findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        GetDevicesTask(this, devicesOnRouterProvider, adapter).execute()
    }

    override fun getComponent(): ApplicationComponent {
        return applicationComponent
    }

    override fun getDeviceList(fragmentHashCode: Int): MutableList<Device> {
        return when (fragmentHashCode) {
            unclassifiedDevicesFragment.hashCode() -> mutableListOf()
            visitorDevicesFragment.hashCode() -> visitorDevicesList
            homeDevicesFragment.hashCode() -> homeDevicesList
            else -> throw IllegalArgumentException("Should not have gotten here")
        }
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
        eventBus: EventBus,
        val context: Context,
        val devicePersistence: DevicePersistence,
        val homeDevicesList: MutableList<Device>,
        val visitorDevicesList: MutableList<Device>,
        val unclassifiedDevicesFragment: DevicesFragment,
        val visitorDevicesFragment: DevicesFragment,
        val homeDevicesFragment: DevicesFragment
    ) : FragmentPagerAdapter(fm) {

        init {
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
            if (homeDevicesFragment.isAdded) {
                homeDevicesFragment.addDevices(event.devices)
            } else {
                homeDevicesList.addAll(event.devices)
            }
            save()
        }

        // used by EventBus
        fun onEvent(event: DevicesMovedToVisitorList) {
            if (visitorDevicesFragment.isAdded) {
                visitorDevicesFragment.addDevices(event.devices)
            } else {
                visitorDevicesList.addAll(event.devices)
            }
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
