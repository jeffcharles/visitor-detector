package com.beyondtechnicallycorrect.visitordetector.fragments

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.beyondtechnicallycorrect.visitordetector.R

class DevicesFragment : ListFragment() {

    private val TAG = "DevicesFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.listView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.move_to_home -> throw UnsupportedOperationException()
                    R.id.move_to_visitor -> throw UnsupportedOperationException()
                    else -> return false
                }
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
                mode.menuInflater.inflate(R.menu.devices_menu, menu)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }

            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                Log.d(TAG, "onItemCheckedStateChanged started")
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
        })
        this.listView.setOnItemClickListener {
            parent, view, position, id -> this.listView.setItemChecked(position, !this.listView.isItemChecked(position))
        }
    }

    public fun updateDevices(devices: Array<String>) {
        this.listAdapter = ArrayAdapter(this.activity, R.layout.device_list_item, R.id.device, devices)
    }
}
