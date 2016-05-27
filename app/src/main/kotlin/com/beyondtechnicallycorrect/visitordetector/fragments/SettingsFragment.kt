package com.beyondtechnicallycorrect.visitordetector.fragments

import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsKeys
import com.beyondtechnicallycorrect.visitordetector.validators.NotEmptyValidator
import com.beyondtechnicallycorrect.visitordetector.validators.RouterIpAddressValidator
import timber.log.Timber
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var notEmptyValidator: NotEmptyValidator
    @Inject lateinit var routerIpAddressValidator: RouterIpAddressValidator

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("onActivityCreated")
        activity.title = this.getString(R.string.settings_fragment_title)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.v("onCreatePreferences")

        (context.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)

        this.addPreferencesFromResource(R.xml.settings)

        this.findPreference(RouterSettingsKeys.homeWifiSsids).onPreferenceChangeListener =
            NotEmptyPreferenceChangeListener(
                context,
                notEmptyValidator,
                R.string.settings_home_wifi_networks_cant_be_empty
            )
        this.findPreference(RouterSettingsKeys.routerIpAddress).onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val value = newValue as String
                when (routerIpAddressValidator.isValid(value)) {
                    RouterIpAddressValidator.Result.VALID -> true
                    RouterIpAddressValidator.Result.EMPTY -> {
                        showErrorAlert(R.string.settings_router_ip_address_cant_be_empty)
                        false
                    }
                    RouterIpAddressValidator.Result.NOT_AN_IP_ADDRESS -> {
                        showErrorAlert(R.string.settings_router_ip_address_must_be_ip_address)
                        false
                    }
                    RouterIpAddressValidator.Result.NOT_LOCAL_ADDRESS -> {
                        showErrorAlert(R.string.settings_router_ip_address_must_be_local_ip_address)
                        false
                    }
                }
            }
        this.findPreference(RouterSettingsKeys.routerUsername).onPreferenceChangeListener =
            NotEmptyPreferenceChangeListener(
                context,
                notEmptyValidator,
                R.string.settings_router_username_cant_be_empty
            )
        this.findPreference(RouterSettingsKeys.routerPassword).onPreferenceChangeListener =
            NotEmptyPreferenceChangeListener(
                context,
                notEmptyValidator,
                R.string.settings_router_password_cant_be_empty
            )
    }

    private fun showErrorAlert(@StringRes errorMessage: Int) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle(R.string.settings_invalid_value_dialog_title)
        alertBuilder.setMessage(errorMessage)
        alertBuilder.setPositiveButton(android.R.string.ok, null)
        alertBuilder.show()
    }

    private class NotEmptyPreferenceChangeListener(
        val context: Context,
        val notEmptyValidator: NotEmptyValidator,
        @StringRes val cantBeEmptyErrorString: Int
    ) : Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            val value = newValue as String
            when (notEmptyValidator.isValid(value)) {
                NotEmptyValidator.Result.VALID -> return true
                NotEmptyValidator.Result.EMPTY -> {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setTitle(R.string.settings_invalid_value_dialog_title)
                    alertBuilder.setMessage(cantBeEmptyErrorString)
                    alertBuilder.setPositiveButton(android.R.string.ok, null)
                    alertBuilder.show()
                    return false
                }
            }
        }
    }
}
