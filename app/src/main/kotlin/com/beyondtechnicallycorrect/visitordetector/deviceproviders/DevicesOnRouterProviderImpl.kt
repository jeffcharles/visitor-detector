package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import android.net.wifi.WifiManager
import com.beyondtechnicallycorrect.visitordetector.BuildConfig
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettings
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsGetter
import dagger.Module
import dagger.Provides
import org.funktionale.either.Either
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

class DevicesOnRouterProviderImpl @Inject constructor(
    private val routerSettingsGetter: RouterSettingsGetter,
    private val routerApiFactory: RouterApiFactory,
    private val onHomeWifi: DevicesOnRouterProvider.OnHomeWifi
) : DevicesOnRouterProvider {

    override fun getDevicesOnRouter(): Either<DeviceFetchingFailure, List<RouterDevice>> {
        return getDevicesOnRouter(routerSettingsGetter.getRouterSettings())
    }

    override fun getDevicesOnRouter(
        routerSettings: RouterSettings
    ): Either<DeviceFetchingFailure, List<RouterDevice>> {
        if (!onHomeWifi.isOnHomeWifi(routerSettings.homeNetworkSsids)) {
            Timber.d("Not on home network")
            return Either.Right(listOf<RouterDevice>())
        }

        val routerApi = routerApiFactory.createRouterApi(routerSettings.routerIpAddress)

        val auth = login(routerApi, routerSettings.routerUsername, routerSettings.routerPassword)
        if (auth.isLeft()) {
            return Either.Left(auth.left().get())
        }

        val macAddressHints = getMacAddresses(routerApi, auth.right().get())
        if (macAddressHints.isLeft()) {
            return Either.Left(macAddressHints.left().get())
        }

        return Either.Right(
            macAddressHints.right().get().map { RouterDevice(macAddress = it[0], hostName = it[1]) }
        )
    }

    private fun login(
        routerApi: RouterApi,
        routerUsername: String,
        routerPassword: String
    ): Either<DeviceFetchingFailure, String> {
        val loginCall =
            routerApi.login(
                loginBody = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "login",
                    params = arrayOf(routerUsername, routerPassword)
                )
            )
        val loginResponse: Response<JsonRpcResponse<String>>
        try {
            loginResponse = loginCall.execute()
        } catch (e: IOException) {
            Timber.w(e, "IOException while logging in")
            return Either.Left(DeviceFetchingFailure.Error)
        }
        if (!loginResponse.isSuccessful) {
            Timber.w("Got status code of %d while logging in", loginResponse.code())
            return Either.Left(DeviceFetchingFailure.Error)
        }
        val loginResponseBody = loginResponse.body()
        val auth = loginResponseBody.result
        if (auth == null) {
            Timber.w("Authentication call didn't return result")
            return Either.Left(DeviceFetchingFailure.Error)
        }
        return Either.Right(auth)
    }

    private fun getMacAddresses(
        routerApi: RouterApi,
        auth: String
    ): Either<DeviceFetchingFailure, Array<Array<String>>> {
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
        if (!macAddressResponse.isSuccessful) {
            Timber.w("Got status code of %d while getting mac addresses", macAddressResponse.code())
            return Either.Left(DeviceFetchingFailure.Error)
        }
        val macAddressBody = macAddressResponse.body()
        val macAddressHints = macAddressBody.result
        if (macAddressHints == null) {
            Timber.w("mac_hints didn't return result")
            return Either.Left(DeviceFetchingFailure.Error)
        }
        return Either.Right(macAddressHints)
    }

    class OnHomeWifiImpl @Inject constructor(
        private val wifiManager: WifiManager
    ) : DevicesOnRouterProvider.OnHomeWifi {
        override fun isOnHomeWifi(homeSsids: List<String>): Boolean {
            return BuildConfig.DEBUG ||
                homeSsids
                    .map { "\"$it\"" }
                    .contains(wifiManager.connectionInfo.ssid)
        }
    }

    @Module
    class DevicesOnRouterProviderImplModule() {
        @Provides @Singleton fun provideOnHomeWifi(
            onHomeWifiImpl: OnHomeWifiImpl
        ): DevicesOnRouterProvider.OnHomeWifi {
            return onHomeWifiImpl
        }
    }
}
