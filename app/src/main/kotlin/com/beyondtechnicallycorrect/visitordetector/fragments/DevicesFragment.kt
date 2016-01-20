package com.beyondtechnicallycorrect.visitordetector.fragments

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.*
import android.widget.*
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToHomeList
import com.beyondtechnicallycorrect.visitordetector.events.DevicesMovedToVisitorList
import de.greenrobot.event.EventBus

class DevicesFragment(val eventBus: EventBus) : ListFragment() {

    private var devices: List<String> = listOf()

    public fun addDevices(devicesToAdd: Set<String>) {
        devices = (devices.toHashSet() + devicesToAdd).toList()
        recreateArrayAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.listView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {

            private val listView = this@DevicesFragment.listView

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.move_to_home -> moveDevicesToHomeList()
                    R.id.move_to_visitor -> moveDevicesToVisitorList()
                    else -> throw UnsupportedOperationException()
                }
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

    public fun updateDevices(devices: List<String>) {
        this.devices = devices
        recreateArrayAdapter()
    }

    private fun recreateArrayAdapter() {
        // this is very resource intensive and not a good idea so a better approach should be used
        this.listAdapter = ArrayAdapter(this.context, R.layout.device_list_item, R.id.device, devices)
    }

    private fun moveDevicesToVisitorList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToVisitorList(devicesToMove)) }
    }

    private fun moveDevicesToHomeList() {
        moveDevicesToList { devicesToMove -> eventBus.post(DevicesMovedToHomeList(devicesToMove)) }
    }

    private fun moveDevicesToList(postEvent: (MutableSet<String>) -> Unit) {
        val allDevices: MutableSet<String> = devices.toHashSet()
        val devicesToMove: MutableSet<String> = hashSetOf()
        for (i in 0..(this.listView.childCount - 1)) {
            if (this.listView.isItemChecked(i)) {
                devicesToMove.add(this.devices[i])
            }
        }
        devices = (allDevices - devicesToMove).toList()
        recreateArrayAdapter()
        postEvent(devicesToMove)
    }
}
