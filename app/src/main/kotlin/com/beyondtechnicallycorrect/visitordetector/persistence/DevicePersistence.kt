package com.beyondtechnicallycorrect.visitordetector.persistence

interface  DevicePersistence {
    fun getSavedDevices(): Devices
    fun saveDevices(devices: Devices)
}
