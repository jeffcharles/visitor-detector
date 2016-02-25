package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import android.net.wifi.WifiManager
import com.beyondtechnicallycorrect.visitordetector.BuildConfig
import org.funktionale.either.Either
import retrofit.Response
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

class DevicesOnRouterProviderImpl @Inject constructor(
    val routerApi: RouterApi,
    val wifiManager: WifiManager
) : DevicesOnRouterProvider {
    override fun getDevicesOnRouter(): Either<DeviceFetchingFailure, List<RouterDevice>> {
        val onHomeWifi =
            BuildConfig.DEBUG ||
            BuildConfig.WIFI_SSIDS
                .split(',')
                .map { "\"$it\"" }
                .contains(wifiManager.connectionInfo.ssid)
        if (!onHomeWifi) {
            Timber.d("Not on home network")
            return Either.Right(listOf<RouterDevice>())
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
        val loginResponse: Response<JsonRpcResponse<String>>
        try {
            loginResponse = loginCall.execute()
        } catch (e: IOException) {
            Timber.w(e, "IOException while logging in")
            return Either.Left(DeviceFetchingFailure.Error)
        }
        if (!loginResponse.isSuccess) {
            Timber.w("Got status code of %d while logging in", loginResponse.code())
            return Either.Left(DeviceFetchingFailure.Error)
        }
        val loginResponseBody = loginResponse.body()
        val auth = loginResponseBody.result
        if (auth == null) {
            Timber.w("Authentication call didn't return result")
            return Either.Left(DeviceFetchingFailure.Error)
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
        val macAddressResponse: Response<JsonRpcResponse<Array<Array<String>>>>
        try {
            macAddressResponse = macAddressCall.execute()
        } catch (e: IOException) {
            Timber.w(e, "IOException while getting mac addresses")
            return Either.Left(DeviceFetchingFailure.Error)
        }
        if (!macAddressResponse.isSuccess) {
            Timber.w("Got status code of %d while getting mac addresses", macAddressResponse.code())
            return Either.Left(DeviceFetchingFailure.Error)
        }
        val macAddressBody = macAddressResponse.body()
        val macAddressHints = macAddressBody.result
        if (macAddressHints == null) {
            Timber.w("mac_hints didn't return result")
            return Either.Left(DeviceFetchingFailure.Error)
        }

        return Either.Right(
            macAddressHints.map { RouterDevice(macAddress = it[0], hostName = it[1]) }
        )
    }
}
