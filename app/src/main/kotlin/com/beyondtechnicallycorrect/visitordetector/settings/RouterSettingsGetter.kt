package com.beyondtechnicallycorrect.visitordetector.settings

interface RouterSettingsGetter {
    fun areRouterSettingsSet(): Boolean
    fun getRouterSettings(): RouterSettings
}
