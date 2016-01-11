package com.beyondtechnicallycorrect.visitordetector.fragments

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.beyondtechnicallycorrect.visitordetector.R

class DevicesFragment : ListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_devices_list, container, false);
    }

    public fun updateDevices(devices: Array<String>) {
        this.listAdapter = ArrayAdapter(this.activity, R.layout.device_list_item, devices)
    }
}
