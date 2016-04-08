package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RouterApi {
    @POST("/cgi-bin/luci/rpc/auth")
    fun login(@Body loginBody: JsonRpcRequest): Call<JsonRpcResponse<String>>

    @POST("/cgi-bin/luci/rpc/sys")
    fun sys(@Body body: JsonRpcRequest, @Query("auth") auth: String): Call<JsonRpcResponse<Array<Array<String>>>>
}
