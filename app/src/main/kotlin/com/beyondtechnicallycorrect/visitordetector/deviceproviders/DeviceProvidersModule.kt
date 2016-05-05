package com.beyondtechnicallycorrect.visitordetector.deviceproviders

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = arrayOf(DevicesOnRouterProviderImpl.DevicesOnRouterProviderImplModule::class))
class DeviceProvidersModule {
    @Provides fun provideDevicesOnRouterProvider(provider: DevicesOnRouterProviderImpl): DevicesOnRouterProvider {
        return provider;
    }

    @Provides @Singleton fun provideRouterApiFactory(factory: RouterApiFactoryImpl): RouterApiFactory {
        return factory;
    }
}
