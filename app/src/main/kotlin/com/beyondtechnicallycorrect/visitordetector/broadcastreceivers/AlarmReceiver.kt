package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations.AlarmReceiverImpl
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.RouterDevice
import timber.log.Timber
import javax.inject.Inject

class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmReceiverImpl: AlarmReceiverImpl

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Starting onReceive")
        (context.applicationContext as VisitorDetectorApplication).getApplicationComponent().inject(this)

        alarmReceiverImpl.start()
        GetConnectedDevicesTask(context, alarmReceiverImpl).execute()
    }

    private class GetConnectedDevicesTask(
        val context: Context,
        val alarmReceiverImpl: AlarmReceiverImpl
    ) : AsyncTask<Void, Void, List<RouterDevice>>() {

        override fun doInBackground(vararg params: Void?): List<RouterDevice> {
            return alarmReceiverImpl.inBackground()
        }

        override fun onPostExecute(connectedDevices: List<RouterDevice>) {
            alarmReceiverImpl.withResults(connectedDevices, context)
        }
    }
}
