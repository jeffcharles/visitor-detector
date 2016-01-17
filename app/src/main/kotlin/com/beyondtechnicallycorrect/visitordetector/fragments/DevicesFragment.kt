package com.beyondtechnicallycorrect.visitordetector.fragments

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.*
import android.widget.*
import com.beyondtechnicallycorrect.visitordetector.R

class DevicesFragment : ListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.listView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {

            private val listView = this@DevicesFragment.listView

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

    public fun updateDevices(devices: Array<String>) {
        this.listAdapter = ArrayAdapter(this.activity, R.layout.device_list_item, R.id.device, devices)
    }
}
