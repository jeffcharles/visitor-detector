package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import com.beyondtechnicallycorrect.visitordetector.BuildConfig
import dagger.Module
import dagger.Provides
import retrofit.GsonConverterFactory
import retrofit.Retrofit

@Module(includes = arrayOf(DevicesOnRouterProviderImpl.DevicesOnRouterProviderImplModule::class))
class DeviceProvidersModule {
    @Provides fun provideDevicesOnRouterProvider(provider: DevicesOnRouterProviderImpl): DevicesOnRouterProvider {
        return provider;
    }

    @Provides fun provideRouterApi(): RouterApi {
        return Retrofit.Builder()
            .baseUrl("http://${BuildConfig.ROUTER_IP_ADDRESS}")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouterApi::class.java)
    }
}
