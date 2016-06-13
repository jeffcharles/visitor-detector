package com.beyondtechnicallycorrect.visitordetector.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.SpannableStringBuilder
import android.widget.EditText
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.events.DeviceDescriptionSetEvent
import de.greenrobot.event.EventBus
import timber.log.Timber
import javax.inject.Inject

class DeviceDescriptionDialogFragment : DialogFragment() {

    @Inject lateinit var eventBus: EventBus

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.v("onCreateDialog")
        (context.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)
        val view =
            activity.layoutInflater.inflate(R.layout.dialog_edit_description, null)
        val deviceDescriptionEditText = view.findViewById(R.id.device_description) as EditText
        deviceDescriptionEditText.text =
            SpannableStringBuilder(arguments.getString("currentDescription"))
        return AlertDialog.Builder(activity)
            .setTitle(R.string.device_edit_description)
            .setPositiveButton(android.R.string.ok, { dialog, id ->
                eventBus.post(
                    DeviceDescriptionSetEvent(
                        deviceDescriptionEditText.text.toString(),
                        arguments.getString("deviceMacAddress")
                    )
                )
            })
            .setNegativeButton(android.R.string.cancel, { dialog, id -> })
            .setView(view)
            .create()
    }

    companion object {
        fun newInstance(deviceMacAddress: String, currentDescription: String): DeviceDescriptionDialogFragment {
            val fragment = DeviceDescriptionDialogFragment()
            val arguments = Bundle()
            arguments.putString("deviceMacAddress", deviceMacAddress)
            arguments.putString("currentDescription", currentDescription)
            fragment.arguments = arguments
            return fragment
        }
    }
}
