package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettings
import com.beyondtechnicallycorrect.visitordetector.settings.RouterSettingsGetter
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

class DevicesOnRouterProviderImplTest {

    @Test
    fun givenNoIssues_ShouldReturnConnectedDevices() {
        val routerApiFactory = createRouterApiFactory()

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.right().get())
            .containsExactly(RouterDevice("foo", "bar"), RouterDevice("baz", "corje"))
    }

    @Test
    fun givenArpTableWithSomeNotConnectedDevices_ShouldOnlyReturnConnectedDevices() {
        val arpTableResponse: Call<JsonRpcResponse<Array<ArpTableEntry>>> = myMock()
        `when`(arpTableResponse.execute()).thenReturn(
            Response.success(JsonRpcResponse(
                arrayOf(ArpTableEntry("mac1", "ip1"), ArpTableEntry("mac2", "ip2")),
                null
            ))
        )
        val conntrackResponse: Call<JsonRpcResponse<Array<ConntrackEntry>>> = myMock()
        `when`(conntrackResponse.execute()).thenReturn(
            Response.success(JsonRpcResponse(
                arrayOf(ConntrackEntry("ip2")),
                null
            ))
        )
        val macHintsResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(macHintsResponse.execute()).thenReturn(
            Response.success(JsonRpcResponse(
                arrayOf(),
                null
            ))
        )
        val routerApiFactory = createRouterApiFactory(
            arpTableResponse = arpTableResponse,
            conntrackResponse = conntrackResponse,
            macHintsResponse = macHintsResponse
        )

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.right().get())
            .containsExactly(RouterDevice("mac2", "ip2"))
    }

    @Test
    fun givenNotOnHomeWifi_ShouldReturnNoConnectedDevices() {
        val onHomeWifi = mock(DevicesOnRouterProvider.OnHomeWifi::class.java)
        `when`(onHomeWifi.isOnHomeWifi(anyObject())).thenReturn(false);

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(
            createRouterSettingsGetter(),
            mock(RouterApiFactory::class.java),
            onHomeWifi
        )
        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.right().get()).isEmpty()
    }

    @Test
    fun givenConnectionFailureDuringLogin_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        `when`(loginResponse.execute()).thenThrow(IOException())
        val routerApiFactory = createRouterApiFactory(loginResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    @Test
    fun givenBadStatusCodeDuringLogin_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        `when`(loginResponse.execute())
            .thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "")))
        val routerApiFactory = createRouterApiFactory(loginResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    @Test
    fun givenIncorrectLoginCredentials_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        // incorrect login credentials results in a 200 status code with a null result and null error
        `when`(loginResponse.execute())
            .thenReturn(Response.success(JsonRpcResponse<String>(null, null)))
        val routerApiFactory = createRouterApiFactory(loginResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    @Test
    fun givenConnectionFailureWhileGettingArpTable_ShouldReturnLeft() {
        val arpTableResponse: Call<JsonRpcResponse<Array<ArpTableEntry>>> = myMock()
        `when`(arpTableResponse.execute()).thenThrow(IOException())
        val routerApiFactory = createRouterApiFactory(arpTableResponse = arpTableResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    @Test
    fun givenConnectionFailureWhileGettingTrackedConnections_ShouldReturnLeft() {
        val conntrackResponse: Call<JsonRpcResponse<Array<ConntrackEntry>>> = myMock()
        `when`(conntrackResponse.execute()).thenThrow(IOException())
        val routerApiFactory = createRouterApiFactory(conntrackResponse = conntrackResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    @Test
    fun givenConnectionFailureWhileGettingMacAddresses_ShouldReturnLeft() {
        val macHintsResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(macHintsResponse.execute()).thenThrow(IOException())
        val routerApiFactory = createRouterApiFactory(macHintsResponse = macHintsResponse)

        val devicesOnRouterProvider =
            DevicesOnRouterProviderImpl(
                createRouterSettingsGetter(),
                routerApiFactory,
                createOnHomeWifi()
            )

        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.isLeft()).isTrue()
    }

    private fun createRouterSettingsGetter(): RouterSettingsGetter {
        val routerSettingsGetter = mock(RouterSettingsGetter::class.java)
        `when`(routerSettingsGetter.getRouterSettings())
            .thenReturn(RouterSettings(listOf(), "", "", ""))
        return routerSettingsGetter
    }

    private fun createOnHomeWifi(): DevicesOnRouterProvider.OnHomeWifi {
        val onHomeWifi = mock(DevicesOnRouterProvider.OnHomeWifi::class.java)
        `when`(onHomeWifi.isOnHomeWifi(anyObject())).thenReturn(true)
        return onHomeWifi
    }

    private fun createRouterApiFactory(
        loginResponse: Call<JsonRpcResponse<String>> = createSuccessfulLoginResponse(),
        arpTableResponse: Call<JsonRpcResponse<Array<ArpTableEntry>>> = createSuccessfulArpTableResponse(),
        conntrackResponse: Call<JsonRpcResponse<Array<ConntrackEntry>>> = createSuccessfulConntrackResponse(),
        macHintsResponse: Call<JsonRpcResponse<Array<Array<String>>>> = createSuccessfulMacHintsResponse()
    ): RouterApiFactory {
        val routerApi = mock(RouterApi::class.java)
        `when`(routerApi.login(anyObject())).thenReturn(loginResponse)
        `when`(routerApi.arp(anyObject(), anyString())).thenReturn(arpTableResponse)
        `when`(routerApi.conntrack(anyObject(), anyString())).thenReturn(conntrackResponse)
        `when`(routerApi.macHints(anyObject(), anyString())).thenReturn(macHintsResponse)
        val routerApiFactory = mock(RouterApiFactory::class.java)
        `when`(routerApiFactory.createRouterApi(anyString())).thenReturn(routerApi)
        return routerApiFactory
    }

    private fun createSuccessfulLoginResponse(): Call<JsonRpcResponse<String>> {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        `when`(loginResponse.execute())
            .thenReturn(Response.success(JsonRpcResponse("auth_token", null)))
        return loginResponse
    }

    private fun createSuccessfulArpTableResponse(): Call<JsonRpcResponse<Array<ArpTableEntry>>> {
        val arpTableResponse: Call<JsonRpcResponse<Array<ArpTableEntry>>> = myMock()
        `when`(arpTableResponse.execute())
            .thenReturn(
                Response.success(
                    JsonRpcResponse(arrayOf(ArpTableEntry("foo", "192.168.1.2"), ArpTableEntry("baz", "192.168.1.3")), null)
                )
            )
        return arpTableResponse
    }

    private fun createSuccessfulConntrackResponse(): Call<JsonRpcResponse<Array<ConntrackEntry>>> {
        val conntrackResponse: Call<JsonRpcResponse<Array<ConntrackEntry>>> = myMock()
        `when`(conntrackResponse.execute())
            .thenReturn(
                Response.success(
                    JsonRpcResponse(arrayOf(ConntrackEntry("192.168.1.2"), ConntrackEntry("192.168.1.3")), null)
                )
            )
        return conntrackResponse
    }

    private fun createSuccessfulMacHintsResponse(): Call<JsonRpcResponse<Array<Array<String>>>> {
        val macHintsResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(macHintsResponse.execute())
            .thenReturn(
                Response.success(
                    JsonRpcResponse(arrayOf(arrayOf("foo", "bar"), arrayOf("baz", "corje")), null)
                )
            )
        return macHintsResponse
    }

    private inline fun <reified T: Any> myMock(): T = mock(T::class.java)

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    private fun <T> eq(v: T): T {
        return Mockito.eq(v)
    }
}
