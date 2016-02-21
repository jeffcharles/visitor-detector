package com.beyondtechnicallycorrect.visitordetector.broadcastreceivers.implementations

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ImplementationsModule {
    @Provides @Singleton fun provideNotificationHelper(notificationHelperImpl: NotificationHelperImpl): NotificationHelper {
        return notificationHelperImpl
    }
}
