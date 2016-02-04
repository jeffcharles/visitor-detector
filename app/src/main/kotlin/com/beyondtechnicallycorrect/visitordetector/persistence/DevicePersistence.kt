package com.beyondtechnicallycorrect.visitordetector.persistence

import android.content.Context
import com.google.gson.Gson
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
public class DevicePersistence @Inject constructor(
    @Named("applicationContext") val applicationContext: Context,
    val gson: Gson
) {
    fun getSavedDevices(): Devices {
        if (!applicationContext.getFileStreamPath("devices.json").exists()) {
            return Devices(visitorDevices = listOf(), homeDevices = listOf())
        }
        var devicesAsJson: String = ""
        BufferedReader(InputStreamReader(applicationContext.openFileInput("devices.json"), Charsets.UTF_8)).use {
            reader -> devicesAsJson = reader.readText()
        }
        Timber.v("Retrieved devices.json as %s", devicesAsJson)
        return gson.fromJson(devicesAsJson, Devices::class.java)
    }

    fun saveDevices(devices: Devices) {
        val devicesAsJson = gson.toJson(devices)
        Timber.v("Saving devices.json as %s", devicesAsJson)
        BufferedWriter(OutputStreamWriter(applicationContext.openFileOutput("devices.json", Context.MODE_PRIVATE), Charsets.UTF_8)).use {
            writer -> writer.write(devicesAsJson)
        }
    }
}
