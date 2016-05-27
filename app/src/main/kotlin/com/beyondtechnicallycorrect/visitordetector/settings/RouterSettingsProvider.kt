package com.beyondtechnicallycorrect.visitordetector.settings

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterSettingsProvider @Inject constructor(
    private val sharedPrefences: SharedPreferences
) : RouterSettingsGetter, RouterSettingsSetter {

    override fun areRouterSettingsSet(): Boolean {
        return sharedPrefences.contains(RouterSettingsKeys.homeWifiSsids)
            && sharedPrefences.contains(RouterSettingsKeys.routerIpAddress)
            && sharedPrefences.contains(RouterSettingsKeys.routerUsername)
            && sharedPrefences.contains(RouterSettingsKeys.routerPassword)
    }

    override fun getRouterSettings(): RouterSettings {
        val homeWifiSsids = sharedPrefences.getStringSet(RouterSettingsKeys.homeWifiSsids, setOf())
        val routerIpAddress = sharedPrefences.getString(RouterSettingsKeys.routerIpAddress, "")
        val routerUsername = sharedPrefences.getString(RouterSettingsKeys.routerUsername, "")
        val routerPassword = sharedPrefences.getString(RouterSettingsKeys.routerPassword, "")
        return RouterSettings(homeWifiSsids.toList(), routerIpAddress, routerUsername, routerPassword)
    }

    override fun setRouterSettings(routerSettings: RouterSettings): Boolean {
        val editableSharedPreferences = sharedPrefences.edit()
        editableSharedPreferences.putStringSet(RouterSettingsKeys.homeWifiSsids, routerSettings.homeNetworkSsids.toMutableSet())
        editableSharedPreferences.putString(RouterSettingsKeys.routerIpAddress, routerSettings.routerIpAddress)
        editableSharedPreferences.putString(RouterSettingsKeys.routerUsername, routerSettings.routerUsername)
        editableSharedPreferences.putString(RouterSettingsKeys.routerPassword, routerSettings.routerPassword)
        return editableSharedPreferences.commit()
    }
}
