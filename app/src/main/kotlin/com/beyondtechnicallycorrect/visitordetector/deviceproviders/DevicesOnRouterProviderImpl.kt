package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import android.net.wifi.WifiManager
import com.beyondtechnicallycorrect.visitordetector.BuildConfig
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class DevicesOnRouterProviderImpl @Inject constructor(
    val routerApi: RouterApi,
    val wifiManager: WifiManager
) : DevicesOnRouterProvider {
    override fun getDevicesOnRouter(): List<RouterDevice> {
        val onHomeWifi =
            BuildConfig.DEBUG ||
            BuildConfig.WIFI_SSIDS
                .split(',')
                .map { "\"$it\"" }
                .contains(wifiManager.connectionInfo.ssid)
        if (!onHomeWifi) {
            Timber.d("Not on home network")
            return listOf()
        }

        val loginCall =
            routerApi.login(
                loginBody = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "login",
                    params = arrayOf(BuildConfig.ROUTER_USERNAME, BuildConfig.ROUTER_PASSWORD)
                )
            )
        val loginResponse = loginCall.execute()
        val loginResponseBody = loginResponse.body()
        val auth = loginResponseBody.result
        if (auth == null) {
            throw RuntimeException("Got a null auth result")
        }

        val macAddressCall =
            routerApi.sys(
                body = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "net.mac_hints",
                    params = arrayOf<String>()
                ),
                auth = auth
            )
        val macAddressResponse = macAddressCall.execute()
        val macAddressBody = macAddressResponse.body()
        val macAddressHints = macAddressBody.result
        if (macAddressHints == null) {
            throw RuntimeException("Got a null result for mac_hints")
        }

        return macAddressHints.map { RouterDevice(macAddress = it[0], hostName = it[1]) }
    }
}
