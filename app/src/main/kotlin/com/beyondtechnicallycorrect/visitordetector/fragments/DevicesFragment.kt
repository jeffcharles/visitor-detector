package com.beyondtechnicallycorrect.visitordetector.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.*
import android.widget.*
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import de.greenrobot.event.EventBus
import timber.log.Timber

class DevicesFragment(val eventBus: EventBus, val devices: MutableList<String>) : ListFragment() {

    private var deviceArrayAdapter: ArrayAdapter<String>? = null

    public fun addDevices(devicesToAdd: Collection<String>) {
        if (deviceArrayAdapter != null) {
            deviceArrayAdapter?.addAll(devicesToAdd)
        } else {
            devices.addAll(devicesToAdd)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.v("Attached Timber")
        deviceArrayAdapter =
            ArrayAdapter(this.context, R.layout.device_list_item, R.id.device, devices)
        this.listAdapter = deviceArrayAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                for(i in 0..(listView.childCount - 1)) {
                    setIsChecked(position = i, checked = false)
                }
            }

            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                setIsChecked(position, checked)
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

    public fun setDevices(devices: List<String>) {
        if (deviceArrayAdapter != null) {
            deviceArrayAdapter?.clear()
            deviceArrayAdapter?.addAll(devices)
        } else {
            this.devices.clear()
            this.devices.addAll(devices)
        }
    }

    private fun moveDevicesToVisitorList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToVisitorList(devicesToMove)) }
    }

    private fun moveDevicesToHomeList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToHomeList(devicesToMove)) }
    }

    private fun moveDevicesToList(postEvent: (Collection<String>) -> Unit) {
        val checkedItemPositions = this.listView.checkedItemPositions
        val checkedIndexes: MutableSet<Int> = hashSetOf()
        for (i in 0..(checkedItemPositions.size() - 1)) {
            if (checkedItemPositions.valueAt(i)) {
                checkedIndexes.add(checkedItemPositions.keyAt(i))
            }
        }
        val devicesToMove = checkedIndexes.mapNotNull { deviceArrayAdapter?.getItem(it) }
        devicesToMove.forEach { deviceArrayAdapter?.remove(it) }
        postEvent(devicesToMove)
    }
}
