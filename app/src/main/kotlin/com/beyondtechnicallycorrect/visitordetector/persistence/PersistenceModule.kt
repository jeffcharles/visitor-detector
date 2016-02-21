package com.beyondtechnicallycorrect.visitordetector.persistence

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PersistenceModule {
    @Provides @Singleton fun provideDevicePersistence(devicePersistenceImpl: DevicePersistenceImpl): DevicePersistence {
        return devicePersistenceImpl
    }
}
