package com.beyondtechnicallycorrect.visitordetector.preferences

import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.preference.EditTextPreference
import android.util.AttributeSet
import com.beyondtechnicallycorrect.visitordetector.R

class EditTextSetPreference(context: Context, val attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : EditTextPreference(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): this(context, attributeSet, defStyleAttr, 0)

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.editTextPreferenceStyle)

    constructor(context: Context): this(context, null)

    init {
        dialogLayoutResource = R.layout.view_preference_edittextset
    }

    override fun persistString(value: String?): Boolean {
        if (this.shouldPersist()) {
            if (value === this.getPersistedString(null as String?)) {
                return true
            } else {
                val splitValue = if (value != null) value.split("\n").toSet() else value
                PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(key, splitValue).apply()
                return true
            }
        } else {
            return false
        }
    }

    override fun getPersistedString(defaultReturnValue: String?): String? {
        return if (!this.shouldPersist()) defaultReturnValue else PreferenceManager.getDefaultSharedPreferences(context).getStringSet(key, setOf(defaultReturnValue)).joinToString("\n")
    }

    override fun setText(value: String) {
        super.setText(value)
        summary = value
    }
}
