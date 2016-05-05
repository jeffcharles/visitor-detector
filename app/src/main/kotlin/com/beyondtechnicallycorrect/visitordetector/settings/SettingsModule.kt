package com.beyondtechnicallycorrect.visitordetector.settings

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SettingsModule {
    @Singleton @Provides fun provideRouterSettingsGetter(
        routerSettingsProvider: RouterSettingsProvider
    ): RouterSettingsGetter {
        return routerSettingsProvider
    }

    @Singleton @Provides fun provideRouterSettingsSetter(
        routerSettingsProvider: RouterSettingsProvider
    ): RouterSettingsSetter {
        return routerSettingsProvider
    }
}
