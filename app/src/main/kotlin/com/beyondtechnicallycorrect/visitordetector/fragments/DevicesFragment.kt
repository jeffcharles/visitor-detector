package com.beyondtechnicallycorrect.visitordetector.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.app.FragmentManager
import android.support.v4.app.ListFragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import android.widget.*
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import com.beyondtechnicallycorrect.visitordetector.events.RefreshDeviceListEvent
import com.beyondtechnicallycorrect.visitordetector.models.Device
import de.greenrobot.event.EventBus
import timber.log.Timber
import javax.inject.Inject

class DevicesFragment() : ListFragment() {

    @Inject lateinit var eventBus: EventBus

    private lateinit var activity: ArgumentProvider
    private lateinit var deviceArrayAdapter: Adapter

    fun addDevices(devicesToAdd: Collection<Device>) {
        deviceArrayAdapter.addAll(devicesToAdd)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.v("onAttach")

        activity = context as ArgumentProvider
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        (this.context.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.v("onCreateView")
        return inflater!!.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("onViewCreated")

        val swipeRefreshLayout = view.findViewById(R.id.swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            eventBus.post(RefreshDeviceListEvent(context, swipeRefreshLayout))
        }

        this.listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val distanceBetweenTopOfParentViewAndTopOfListView =
                    if (listView.childCount == 0) 0 else listView.getChildAt(0).top
                swipeRefreshLayout.isEnabled =
                    firstVisibleItem == 0 && distanceBetweenTopOfParentViewAndTopOfListView >= 0
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            }
        })

        this.listView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.move_to_home -> moveDevicesToHomeList()
                    R.id.move_to_visitor -> moveDevicesToVisitorList()
                    else -> throw UnsupportedOperationException()
                }
                mode.finish()
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
                mode.menuInflater.inflate(R.menu.devices_menu, menu)
                activity.setActionMode(mode)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                Timber.v("onDestroyActionMode")
                activity.setActionMode(null)
                for(i in 0..(listView.childCount - 1)) {
                    setIsChecked(position = i, checked = false)
                }
            }

            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                when (checked) {
                    true -> deviceArrayAdapter.selectDevice(deviceArrayAdapter.getItem(position))
                    false -> deviceArrayAdapter.deselectDevice(deviceArrayAdapter.getItem(position))
                }
                setIsChecked(position - listView.firstVisiblePosition, checked)
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            private fun setIsChecked(position: Int, checked: Boolean) {
                (listView.getChildAt(position).findViewById(R.id.device_checkbox) as CheckBox).isChecked = checked
            }
        })
        this.listView.setOnItemClickListener { parent, view, position, id ->
            // does not fire on de-selection but seems to uncheck properly
            this.listView.setItemChecked(position, !this.listView.isItemChecked(position))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("onActivityCreated")

        val deviceType = this.arguments.getInt("deviceType")
        val devices = activity.getDeviceList(deviceType)
        activity.setFragmentForType(deviceType, this)

        deviceArrayAdapter = Adapter(this.context, devices, childFragmentManager)
        this.listAdapter = deviceArrayAdapter
    }

    fun setDevices(devices: List<Device>) {
        Timber.v("setDevices")
        deviceArrayAdapter.clear()
        deviceArrayAdapter.addAll(devices)
    }

    fun refreshListView() {
        Timber.v("refreshListView")
        deviceArrayAdapter.notifyDataSetChanged()
    }

    private fun moveDevicesToVisitorList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToVisitorList(devicesToMove)) }
    }

    private fun moveDevicesToHomeList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToHomeList(devicesToMove)) }
    }

    private fun moveDevicesToList(postEvent: (Collection<Device>) -> Unit) {
        val checkedItemPositions = this.listView.checkedItemPositions
        val checkedIndexes: MutableSet<Int> = hashSetOf()
        for (i in 0..(checkedItemPositions.size() - 1)) {
            if (checkedItemPositions.valueAt(i)) {
                checkedIndexes.add(checkedItemPositions.keyAt(i))
            }
        }
        val devicesToMove = checkedIndexes.mapNotNull { deviceArrayAdapter.getItem(it) }
        devicesToMove.forEach { deviceArrayAdapter.remove(it) }
        postEvent(devicesToMove)
    }

    interface ArgumentProvider {
        fun getDeviceList(deviceType: Int): MutableList<Device>
        fun setActionMode(actionMode: ActionMode?)
        fun setFragmentForType(deviceType: Int, devicesFragment: DevicesFragment)
    }

    private class Adapter(
        context: Context,
        @NonNull objects: MutableList<Device>,
        @NonNull val childFragmentManager: FragmentManager
    ) : ArrayAdapter<Device>(context, R.layout.device_list_item, R.id.device, objects) {

        private val selectedDevices: MutableSet<Device> = hashSetOf()

        fun selectDevice(device: Device) {
            selectedDevices.add(device)
        }

        fun deselectDevice(device: Device) {
            selectedDevices.remove(device)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = super.getView(position, convertView, parent)
            (view.findViewById(R.id.device_checkbox) as CheckBox).isChecked =
                selectedDevices.contains(this.getItem(position))
            val editDescriptionButton = view.findViewById(R.id.edit_description) as ImageButton
            editDescriptionButton.setOnClickListener {
                val device = this.getItem(position)
                val descriptionDialogFragment = DeviceDescriptionDialogFragment.newInstance(
                    deviceMacAddress = device.macAddress,
                    currentDescription = device.description
                )
                descriptionDialogFragment.show(childFragmentManager, "description")
            }
            return view
        }
    }

    companion object {
        fun newInstance(deviceType: Int): DevicesFragment {
            val fragment = DevicesFragment()
            val arguments = Bundle()
            arguments.putInt("deviceType", deviceType)
            fragment.arguments = arguments
            return fragment
        }
    }
}
