package com.beyondtechnicallycorrect.visitordetector.preferences

import android.content.Context
import android.support.v7.preference.EditTextPreference
import android.util.AttributeSet
import com.beyondtechnicallycorrect.visitordetector.R

class EditPasswordPreference(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : EditTextPreference(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): this(context, attributeSet, defStyleAttr, 0)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.editTextPreferenceStyle)

    constructor(context: Context): this(context, null)

    init {
        dialogLayoutResource = R.layout.view_preference_editpassword
    }

    override fun setText(value: String) {
        super.setText(value)
        val forSummary = StringBuilder(value.length)
        for (i in 0..value.length - 1) {
            forSummary.append('●')
        }
        summary = forSummary
    }
}
