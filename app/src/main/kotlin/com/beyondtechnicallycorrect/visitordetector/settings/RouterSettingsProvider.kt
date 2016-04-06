package com.beyondtechnicallycorrect.visitordetector.settings

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterSettingsProvider @Inject constructor(
    private val sharedPrefences: SharedPreferences
) : RouterSettingsGetter, RouterSettingsSetter {

    private val homeWifiSsids = "home_wifi_ssids"
    private val routerIpAddress = "router_ip_address"
    private val routerUsername = "router_username"
    private val routerPassword = "router_password"

    override fun areRouterSettingsSet(): Boolean {
        return sharedPrefences.contains(homeWifiSsids)
            && sharedPrefences.contains(routerIpAddress)
            && sharedPrefences.contains(routerUsername)
            && sharedPrefences.contains(routerPassword)
    }

    override fun getRouterSettings(): RouterSettings {
        val homeWifiSsids = sharedPrefences.getStringSet(homeWifiSsids, setOf())
        val routerIpAddress = sharedPrefences.getString(routerIpAddress, "")
        val routerUsername = sharedPrefences.getString(routerUsername, "")
        val routerPassword = sharedPrefences.getString(routerPassword, "")
        return RouterSettings(homeWifiSsids.toList(), routerIpAddress, routerUsername, routerPassword)
    }

    override fun setRouterSettings(routerSettings: RouterSettings): Boolean {
        val editableSharedPreferences = sharedPrefences.edit()
        editableSharedPreferences.putStringSet(homeWifiSsids, routerSettings.homeNetworkSsids.toMutableSet())
        editableSharedPreferences.putString(routerIpAddress, routerSettings.routerIpAddress)
        editableSharedPreferences.putString(routerUsername, routerSettings.routerUsername)
        editableSharedPreferences.putString(routerPassword, routerSettings.routerPassword)
        return editableSharedPreferences.commit()
    }
}
