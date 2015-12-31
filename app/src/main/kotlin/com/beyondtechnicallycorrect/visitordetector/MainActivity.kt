package com.beyondtechnicallycorrect.visitordetector

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as VisitorDetectorApplication).getApplicationComponent().inject(this)

        setContentView(R.layout.activity_main)

        val deviceList = findViewById(R.id.devices) as ListView
        GetDevicesTask(this, devicesOnRouterProvider, deviceList).execute()
    }

    private class GetDevicesTask(val context: Context, val devicesOnRouterProvider: DevicesOnRouterProvider, val deviceList: ListView) : AsyncTask<Void, Void, Array<String>>() {
        override fun doInBackground(vararg params: Void?): Array<String>? {
            return devicesOnRouterProvider.getDevicesOnRouter()
        }

        override fun onPostExecute(devices: Array<String>?) {
            val deviceListAdapter = ArrayAdapter<String>(context, R.layout.device_list_item, devices)
            deviceList.adapter = deviceListAdapter
        }
    }
}
