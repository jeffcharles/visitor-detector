package com.beyondtechnicallycorrect.visitordetector.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.beyondtechnicallycorrect.visitordetector.AlarmSchedulingHelper
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DeviceFetchingFailure
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import com.beyondtechnicallycorrect.visitordetector.events.DeviceDescriptionSetEvent
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import com.beyondtechnicallycorrect.visitordetector.fragments.DevicesFragment.ArgumentProvider
import com.beyondtechnicallycorrect.visitordetector.models.Device
import com.beyondtechnicallycorrect.visitordetector.persistence.DevicePersistence
import com.beyondtechnicallycorrect.visitordetector.persistence.Devices
import com.beyondtechnicallycorrect.visitordetector.persistence.SavedDevice
import de.greenrobot.event.EventBus
import org.funktionale.either.Either
import timber.log.Timber
import javax.inject.Inject

class DevicesTabsFragment() : Fragment(), ArgumentProvider {

    companion object {
        val unclassifiedDevicesType = 1
        val visitorDevicesType = 2
        val homeDevicesType = 3
    }

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject lateinit var eventBus: EventBus
    @Inject lateinit var devicePersistence: DevicePersistence
    @Inject lateinit var alarmSchedulingHelper: AlarmSchedulingHelper

    private val unclassifiedDevicesList: MutableList<Device> = mutableListOf()
    private val visitorDevicesList: MutableList<Device> = mutableListOf()
    private val homeDevicesList: MutableList<Device> = mutableListOf()

    private lateinit var adapter: PagerAdapter

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        (this.context.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)

        alarmSchedulingHelper.setupAlarm()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.v("onCreateView")
        return inflater!!.inflate(R.layout.fragment_devices_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("onViewCreated")

        val pager = view.findViewById(R.id.pager) as ViewPager
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                actionMode?.finish()
            }
        })
        val savedDevices = devicePersistence.getSavedDevices()

        visitorDevicesList.addAll(
            savedDevices.visitorDevices.map { Device(macAddress = it.macAddress, hostName = null, description = it.description) }
        )
        homeDevicesList.addAll(
            savedDevices.homeDevices.map { Device(macAddress = it.macAddress, hostName = null, description = it.description) }
        )

        adapter = PagerAdapter(
            childFragmentManager,
            eventBus,
            this.context,
            devicePersistence,
            unclassifiedDevicesList,
            homeDevicesList,
            visitorDevicesList
        )
        pager.adapter = adapter
        val tabLayout = view.findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        GetDevicesTask(this.context, devicesOnRouterProvider, adapter).execute()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("onActivityCreated")
        (activity as Callbacks).enableNavigationDrawer()
        activity.title = this.getString(R.string.device_tabs_fragment_title)
    }

    override fun getDeviceList(deviceType: Int): MutableList<Device> {
        Timber.v("getDeviceList(deviceType = %d)", deviceType)
        return when (deviceType) {
            unclassifiedDevicesType -> unclassifiedDevicesList
            visitorDevicesType -> visitorDevicesList
            homeDevicesType -> homeDevicesList
            else -> throw IllegalArgumentException("Should not have gotten here")
        }
    }

    override fun setActionMode(actionMode: ActionMode?) {
        this.actionMode = actionMode
    }

    override fun setFragmentForType(deviceType: Int, devicesFragment: DevicesFragment) {
        adapter.setFragmentForType(deviceType, devicesFragment)
    }

    interface Callbacks {
        fun enableNavigationDrawer()
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
        val unclassifiedDevicesList: MutableList<Device>,
        val homeDevicesList: MutableList<Device>,
        val visitorDevicesList: MutableList<Device>
    ) : FragmentPagerAdapter(fm) {

        private var unclassifiedDevicesFragment: DevicesFragment? = null
        private var visitorDevicesFragment: DevicesFragment? = null
        private var homeDevicesFragment: DevicesFragment? = null

        init {
            eventBus.register(this)
        }

        override fun getCount(): Int {
            return 3;
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DevicesFragment.newInstance(unclassifiedDevicesType)
                1 -> DevicesFragment.newInstance(visitorDevicesType)
                2 -> DevicesFragment.newInstance(homeDevicesType)
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
            if (homeDevicesFragment != null && homeDevicesFragment!!.isAdded) {
                homeDevicesFragment!!.addDevices(event.devices)
            } else {
                homeDevicesList.addAll(event.devices)
            }
            save()
        }

        // used by EventBus
        fun onEvent(event: DevicesMovedToVisitorList) {
            if (visitorDevicesFragment != null && visitorDevicesFragment!!.isAdded) {
                visitorDevicesFragment!!.addDevices(event.devices)
            } else {
                visitorDevicesList.addAll(event.devices)
            }
            save()
        }

        // used by EventBus
        fun onEvent(deviceDescriptionSetEvent: DeviceDescriptionSetEvent) {
            Timber.v("onEvent(deviceDescriptionSetEvent = %s)", deviceDescriptionSetEvent)
            var shouldSave = false
            var refreshUnclassified: Boolean
            var refreshHome = false
            var refreshVisitor = false
            var possibleDevice =
                unclassifiedDevicesList.firstOrNull { it.macAddress == deviceDescriptionSetEvent.macAddress }
            refreshUnclassified = true
            if (possibleDevice == null) {
                refreshUnclassified = false
                shouldSave = true
                possibleDevice = homeDevicesList.firstOrNull { it.macAddress == deviceDescriptionSetEvent.macAddress }
                refreshHome = true
            }
            if (possibleDevice == null) {
                refreshHome = false
                shouldSave = true
                possibleDevice = visitorDevicesList.firstOrNull { it.macAddress == deviceDescriptionSetEvent.macAddress }
                refreshVisitor = true
            }
            if (possibleDevice == null) {
                throw UnsupportedOperationException("At least one list should contain item with mac address")
            }
            possibleDevice.description = deviceDescriptionSetEvent.description
            if (shouldSave) {
                devicePersistence.saveDevices(
                    Devices(
                        visitorDevices = visitorDevicesList.map { SavedDevice(macAddress = it.macAddress, description = it.description) },
                        homeDevices = homeDevicesList.map { SavedDevice(macAddress = it.macAddress, description = it.description) }
                    )
                )
            }
            if (refreshUnclassified) {
                unclassifiedDevicesFragment!!.refreshListView()
            }
            if (refreshHome) {
                homeDevicesFragment!!.refreshListView()
            }
            if (refreshVisitor) {
                visitorDevicesFragment!!.refreshListView()
            }
        }

        fun setFragmentForType(deviceType: Int, devicesFragment: DevicesFragment) {
            when (deviceType) {
                unclassifiedDevicesType -> unclassifiedDevicesFragment = devicesFragment
                visitorDevicesType -> visitorDevicesFragment = devicesFragment
                homeDevicesType -> homeDevicesFragment = devicesFragment
                else -> throw IllegalArgumentException("Unknown device type of $deviceType")
            }
        }

        fun setConnectedDevices(connectedDevices: List<RouterDevice>) {
            Timber.v("setConnectedDevices")
            val connectedDevicesByMacAddress = connectedDevices.associateBy { it.macAddress }
            val homeMacAddresses = homeDevicesList.map { it.macAddress }.toSet()
            val visitorMacAddresses = visitorDevicesList.map { it.macAddress }.toSet()
            val unclassifiedDevices =
                (connectedDevicesByMacAddress.keys - homeMacAddresses - visitorMacAddresses)
                    .map { connectedDevicesByMacAddress[it]!! }
                    .map { Device(macAddress = it.macAddress, hostName = it.hostName, description = "") }
                    .toMutableList()
            if (unclassifiedDevicesFragment != null && unclassifiedDevicesFragment!!.isAdded) {
                unclassifiedDevicesFragment!!.setDevices(unclassifiedDevices)
            } else {
                unclassifiedDevicesList.clear()
                unclassifiedDevicesList.addAll(unclassifiedDevices)
            }
        }

        private fun save() {
            devicePersistence.saveDevices(
                Devices(
                    visitorDevices = visitorDevicesList.map { SavedDevice(macAddress = it.macAddress, description = it.description) },
                    homeDevices = homeDevicesList.map { SavedDevice(macAddress = it.macAddress, description = it.description) }
                )
            )
        }
    }
}
