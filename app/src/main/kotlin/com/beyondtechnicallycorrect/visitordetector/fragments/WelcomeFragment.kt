package com.beyondtechnicallycorrect.visitordetector.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.beyondtechnicallycorrect.visitordetector.R
import com.beyondtechnicallycorrect.visitordetector.VisitorDetectorApplication
import com.beyondtechnicallycorrect.visitordetector.deviceproviders.DevicesOnRouterProvider
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettings
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsSetter
import com.beyondtechnicallycorrect.visitordetector.validators.NotEmptyValidator
import com.beyondtechnicallycorrect.visitordetector.validators.RouterIpAddressValidator
import org.funktionale.either.Either
import timber.log.Timber
import javax.inject.Inject

class WelcomeFragment() : Fragment() {

    @Inject lateinit var notEmptyValidator: NotEmptyValidator
    @Inject lateinit var routerIpAddressValidator: RouterIpAddressValidator
    @Inject lateinit var onHomeWifi: DevicesOnRouterProvider.OnHomeWifi
    @Inject lateinit var devicesOnRouterProvider: DevicesOnRouterProvider
    @Inject lateinit var routerSettingsSetter: RouterSettingsSetter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        (this.context.applicationContext as VisitorDetectorApplication)
            .getApplicationComponent()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        Timber.v("onCreateView")
        return inflater!!.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("onViewCreated")

        val testSettingsButton = view.findViewById(R.id.test_settings) as Button
        testSettingsButton.isEnabled = false
        val nextButton = view.findViewById(R.id.next_button) as Button
        nextButton.isEnabled = false
        val validationState = ValidationState { allValid ->
            run {
                testSettingsButton.isEnabled = allValid
                nextButton.isEnabled = allValid
            }
        }
        val homeWifiNetworksEdit = view.findViewById(R.id.home_wifi_networks) as EditText
        val homeWifiNetworksLabel = view.findViewById(R.id.home_wifi_networks_label) as TextInputLayout
        addNotEmptyValidation(
            homeWifiNetworksEdit,
            homeWifiNetworksLabel,
            R.string.welcome_home_wifi_networks_cant_be_empty,
            { valid -> validationState.validNetworkSsids = valid }
        )
        val routerIpAddressEdit = view.findViewById(R.id.router_ip_address) as EditText
        val routerIpAddressLabel = view.findViewById(R.id.router_ip_address_label) as TextInputLayout
        routerIpAddressEdit.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                validateRouterIpAddress(
                    routerIpAddressEdit,
                    routerIpAddressLabel,
                    { valid -> validationState.validRouterIpAddress = valid }
                )
            }
        }
        routerIpAddressEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validateRouterIpAddress(
                    routerIpAddressEdit,
                    routerIpAddressLabel,
                    { valid -> validationState.validRouterIpAddress = valid }
                )
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, Before: Int, count: Int) {
            }
        })
        val routerUsernameEdit = view.findViewById(R.id.router_username) as EditText
        val routerUsernameLabel = view.findViewById(R.id.router_username_label) as TextInputLayout
        addNotEmptyValidation(
            routerUsernameEdit,
            routerUsernameLabel,
            R.string.welcome_router_username_cant_be_empty,
            { valid -> validationState.validRouterUsername = valid }
        )
        val routerPasswordEdit = view.findViewById(R.id.router_password) as EditText
        val routerPasswordLabel = view.findViewById(R.id.router_password_label) as TextInputLayout
        addNotEmptyValidation(
            routerPasswordEdit,
            routerPasswordLabel,
            R.string.welcome_router_password_cant_be_empty,
            { valid -> validationState.validRouterPassword = valid }
        )
        testSettingsButton.setOnClickListener {
            if (!onHomeWifi.isOnHomeWifi(listOf(homeWifiNetworksEdit.text.toString()))) {
                Toast.makeText(
                    this.context,
                    R.string.welcome_test_settings_not_on_network,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                RetrieveDevicesAsyncTask(
                    this.context,
                    devicesOnRouterProvider,
                    listOf(homeWifiNetworksEdit.text.toString()),
                    routerIpAddressEdit.text.toString(),
                    routerUsernameEdit.text.toString(),
                    routerPasswordEdit.text.toString()
                ).execute()
            }
        }
        nextButton.setOnClickListener {
            val setSettings = routerSettingsSetter.setRouterSettings(
                RouterSettings(
                    listOf(homeWifiNetworksEdit.text.toString()),
                    routerIpAddressEdit.text.toString(),
                    routerUsernameEdit.text.toString(),
                    routerPasswordEdit.text.toString()
                )
            )
            if (!setSettings) {
                Toast
                    .makeText(
                        this.context,
                        R.string.welcome_next_failed_to_set_settings,
                        Toast.LENGTH_LONG
                    )
                    .show()
            } else {
                (activity as Callbacks).doneEnteringSettings()
            }
        }
    }

    private fun addNotEmptyValidation(
        editText: EditText,
        label: TextInputLayout,
        @StringRes emptyMessage: Int,
        validationStateFunc: (Boolean) -> Unit
    ) {
        editText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                validateNotEmptyEdit(editText, label, emptyMessage, validationStateFunc)
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                validateNotEmptyEdit(editText, label, emptyMessage, validationStateFunc)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, Before: Int, count: Int) {
            }
        })
    }

    private fun validateNotEmptyEdit(
        edit: EditText,
        label: TextInputLayout,
        @StringRes emptyMessage: Int,
        validationStateFunc: (Boolean) -> Unit
    ) {
        when (notEmptyValidator.isValid(edit.text)) {
            NotEmptyValidator.Result.EMPTY -> {
                label.error = getString(emptyMessage)
                validationStateFunc(false)
            }
            NotEmptyValidator.Result.VALID -> {
                label.error = null
                validationStateFunc(true)
            }
        }
    }

    private fun validateRouterIpAddress(
        routerIpAddressEdit: EditText,
        routerIpAddressLabel: TextInputLayout,
        validationStateFunc: (Boolean) -> Unit
    ) {
        val result = routerIpAddressValidator.isValid(routerIpAddressEdit.text)
        when (result) {
            RouterIpAddressValidator.Result.VALID -> routerIpAddressLabel.error = null
            RouterIpAddressValidator.Result.EMPTY -> routerIpAddressLabel.error =
                getString(R.string.welcome_router_ip_address_cant_be_empty)
            RouterIpAddressValidator.Result.NOT_AN_IP_ADDRESS -> routerIpAddressLabel.error =
                getString(R.string.welcome_router_ip_address_must_be_ip_address)
            RouterIpAddressValidator.Result.NOT_LOCAL_ADDRESS -> routerIpAddressLabel.error =
                getString(R.string.welcome_router_ip_address_must_be_local_ip_address)
        }
        validationStateFunc(result == RouterIpAddressValidator.Result.VALID)
    }

    interface Callbacks {
        fun doneEnteringSettings()
    }

    private class ValidationState(val onValidationChange: (Boolean) -> Unit) {
        var validNetworkSsids = false
            set(value) {
                field = value
                onValidationChange(allValid())
            }

        var validRouterIpAddress = false
            set(value) {
                field = value
                onValidationChange(allValid())
            }

        var validRouterUsername = false
            set(value) {
                field = value
                onValidationChange(allValid())
            }

        var validRouterPassword = false
            set(value) {
                field = value
                onValidationChange(allValid())
            }

        private fun allValid(): Boolean {
            return validNetworkSsids && validRouterIpAddress && validRouterUsername && validRouterPassword
        }
    }

    private class RetrieveDevicesAsyncTask(
        private val context: Context,
        private val devicesOnRouterProvider: DevicesOnRouterProvider,
        private val networkSsids: List<String>,
        private val routerIpAddress: String,
        private val routerUsername: String,
        private val routerPassword: String
    ) : AsyncTask<Void, Void, Either<*, *>>() {

        override fun doInBackground(vararg params: Void): Either<*, *> {
            return devicesOnRouterProvider.getDevicesOnRouter(
                RouterSettings(
                    networkSsids,
                    routerIpAddress,
                    routerUsername,
                    routerPassword
                )
            )
        }

        override fun onPostExecute(result: Either<*, *>) {
            when (result) {
                is Either.Left -> Toast.makeText(
                    context,
                    R.string.error_fetching_devices,
                    Toast.LENGTH_SHORT
                ).show()
                is Either.Right -> Toast.makeText(
                    context,
                    R.string.welcome_test_settings_successful,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
