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
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute())
            .thenReturn(
                Response.success(
                    JsonRpcResponse(arrayOf(arrayOf("foo", "bar"), arrayOf("baz", "corje")), null)
                )
            )
        val routerApiFactory = createRouterApiFactory(createSuccessfulLoginResponse(), sysResponse)

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
        val routerApiFactory = createRouterApiFactory(loginResponse, createSuccessfulSysResponse())

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
        val routerApiFactory = createRouterApiFactory(loginResponse, createSuccessfulSysResponse())

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
        val routerApiFactory = createRouterApiFactory(loginResponse, createSuccessfulSysResponse())

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
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute()).thenThrow(IOException())
        val routerApiFactory = createRouterApiFactory(createSuccessfulLoginResponse(), sysResponse)

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
    fun givenBadStatusCodeWhileGettingMacAddresses_ShouldReturnLeft() {
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute())
            .thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "")))
        val routerApiFactory = createRouterApiFactory(createSuccessfulLoginResponse(), sysResponse)

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
        loginResponse: Call<JsonRpcResponse<String>>,
        sysResponse: Call<JsonRpcResponse<Array<Array<String>>>>
    ): RouterApiFactory {
        val routerApi = mock(RouterApi::class.java)
        `when`(routerApi.login(anyObject())).thenReturn(loginResponse)
        `when`(routerApi.sys(anyObject(), anyString())).thenReturn(sysResponse)
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

    private fun createSuccessfulSysResponse(): Call<JsonRpcResponse<Array<Array<String>>>> {
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute())
            .thenReturn(
                Response.success(
                    JsonRpcResponse(arrayOf(arrayOf("foo", "bar"), arrayOf("baz", "corje")), null)
                )
            )
        return sysResponse
    }

    private inline fun <reified T: Any> myMock(): T = mock(T::class.java)

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    private fun <T> eq(v: T): T {
        return Mockito.eq(v)
    }
}
