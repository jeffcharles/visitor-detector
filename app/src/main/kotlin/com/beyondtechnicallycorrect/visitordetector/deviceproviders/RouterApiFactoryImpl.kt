package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class RouterApiFactoryImpl @Inject constructor() : RouterApiFactory {
    override fun createRouterApi(routerIpAddress: String): RouterApi {
        return Retrofit.Builder()
            .baseUrl("http://$routerIpAddress")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouterApi::class.java)
    }
}
