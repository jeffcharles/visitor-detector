package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import retrofit.Call
import retrofit.http.Body
import retrofit.http.POST
import retrofit.http.Query

interface RouterApi {
    @POST("/cgi-bin/luci/rpc/auth")
    fun login(@Body loginBody: JsonRpcRequest): Call<JsonRpcResponse<String>>

    @POST("/cgi-bin/luci/rpc/sys")
    fun sys(@Body body: JsonRpcRequest, @Query("auth") auth: String): Call<JsonRpcResponse<Array<Array<String>>>>
}
