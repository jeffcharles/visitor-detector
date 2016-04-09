package com.beyondtechnicallycorrect.visitordetector.deviceproviders

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
        val routerApi = createRouterApi(createSuccessfulLoginResponse(), sysResponse)

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().right().get())
            .containsExactly(RouterDevice("foo", "bar"), RouterDevice("baz", "corje"))
    }

    @Test
    fun givenNotOnHomeWifi_ShouldReturnNoConnectedDevices() {
        val onHomeWifi = mock(DevicesOnRouterProviderImpl.OnHomeWifi::class.java)
        `when`(onHomeWifi.isOnHomeWifi()).thenReturn(false);

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(
            mock(RouterApi::class.java),
            onHomeWifi
        )
        val connectedDevices = devicesOnRouterProvider.getDevicesOnRouter()
        assertThat(connectedDevices.right().get()).isEmpty()
    }

    @Test
    fun givenConnectionFailureDuringLogin_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        `when`(loginResponse.execute()).thenThrow(IOException())
        val routerApi = createRouterApi(loginResponse, createSuccessfulSysResponse())

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().isLeft()).isTrue()
    }

    @Test
    fun givenBadStatusCodeDuringLogin_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        `when`(loginResponse.execute())
            .thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "")))
        val routerApi = createRouterApi(loginResponse, createSuccessfulSysResponse())

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().isLeft()).isTrue()
    }

    @Test
    fun givenIncorrectLoginCredentials_ShouldReturnLeft() {
        val loginResponse: Call<JsonRpcResponse<String>> = myMock()
        // incorrect login credentials results in a 200 status code with a null result and null error
        `when`(loginResponse.execute())
            .thenReturn(Response.success(JsonRpcResponse<String>(null, null)))
        val routerApi = createRouterApi(loginResponse, createSuccessfulSysResponse())

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().isLeft()).isTrue()
    }

    @Test
    fun givenConnectionFailureWhileGettingMacAddresses_ShouldReturnLeft() {
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute()).thenThrow(IOException())
        val routerApi = createRouterApi(createSuccessfulLoginResponse(), sysResponse)

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().isLeft()).isTrue()
    }

    @Test
    fun givenBadStatusCodeWhileGettingMacAddresses_ShouldReturnLeft() {
        val sysResponse: Call<JsonRpcResponse<Array<Array<String>>>> = myMock()
        `when`(sysResponse.execute())
            .thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "")))
        val routerApi = createRouterApi(createSuccessfulLoginResponse(), sysResponse)

        val devicesOnRouterProvider = DevicesOnRouterProviderImpl(routerApi, createOnHomeWifi())

        assertThat(devicesOnRouterProvider.getDevicesOnRouter().isLeft()).isTrue()
    }

    private fun createOnHomeWifi(): DevicesOnRouterProviderImpl.OnHomeWifi {
        val onHomeWifi = mock(DevicesOnRouterProviderImpl.OnHomeWifi::class.java)
        `when`(onHomeWifi.isOnHomeWifi()).thenReturn(true)
        return onHomeWifi
    }

    private fun createRouterApi(
        loginResponse: Call<JsonRpcResponse<String>>,
        sysResponse: Call<JsonRpcResponse<Array<Array<String>>>>
    ): RouterApi {
        val routerApi = mock(RouterApi::class.java)
        `when`(routerApi.login(anyObject())).thenReturn(loginResponse)
        `when`(routerApi.sys(anyObject(), anyString())).thenReturn(sysResponse)
        return routerApi
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
