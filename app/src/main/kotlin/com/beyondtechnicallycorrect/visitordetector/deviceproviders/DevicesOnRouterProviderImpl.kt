package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import android.net.wifi.WifiManager
import com.beyondtechnicallycorrect.visitordetector.BuildConfig
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettings
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsGetter
import com.google.common.net.InetAddresses
import dagger.Module
import dagger.Provides
import org.funktionale.either.Either
import retrofit2.Call
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

        val auth = processRequest(
            routerApi.login(
                loginBody = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "login",
                    params = arrayOf(routerSettings.routerUsername, routerSettings.routerPassword)
                )
            ),
            "login"
        )
        if (auth.isLeft()) {
            return Either.Left(auth.left().get())
        }

        val arpTable = processRequest(
            routerApi.arp(
                body = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "net.arptable",
                    params = arrayOf<String>()
                ),
                auth = auth.right().get()
            ),
            "arptable"
        )
        if (arpTable.isLeft()) {
            return Either.Left(arpTable.left().get())
        }

        val activeConnections = processRequest(
            routerApi.conntrack(
                body = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "net.conntrack",
                    params = arrayOf<String>()
                ),
                auth = auth.right().get()
            ),
            "conntrack"
        )
        if (activeConnections.isLeft()) {
            return Either.Left(activeConnections.left().get())
        }
        val activeIps = activeConnections.right().get().map { it.src }

        val machints = processRequest(
            routerApi.macHints(
                body = JsonRpcRequest(
                    jsonrpc = "2.0",
                    id = UUID.randomUUID().toString(),
                    method = "net.mac_hints",
                    params = arrayOf<String>()
                ),
                auth = auth.right().get()
            ),
            "mac_hints"
        )
        if (machints.isLeft()) {
            return Either.Left(machints.left().get())
        }
        val macToHostName = machints.right().get().associate { Pair(it[0].toLowerCase(), it[1]) }

        return Either.Right(
            arpTable.right().get().filter {
                activeIps.contains(it.ipAddress)
            }.map {
                RouterDevice(macAddress = it.hwAddress, hostName = macToHostName.getOrElse(it.hwAddress, { it.ipAddress }))
            }
        )
    }

    private fun <T> processRequest(request: Call<JsonRpcResponse<T>>, descriptionForLog: String): Either<DeviceFetchingFailure, T> {
        val response: Response<JsonRpcResponse<T>>
        try {
            response = request.execute()
        } catch (e: IOException) {
            Timber.w(e, "IO exception during %s", descriptionForLog)
            return Either.Left(DeviceFetchingFailure.Error)
        }
        if (!response.isSuccessful) {
            Timber.w("Got status code of %d during %s", descriptionForLog)
            return Either.Left(DeviceFetchingFailure.Error)
        }
        val result = response.body().result
        if (result == null) {
            Timber.w("%s didn't return result", descriptionForLog)
            return Either.Left(DeviceFetchingFailure.Error)
        }
        return Either.Right(result)
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
