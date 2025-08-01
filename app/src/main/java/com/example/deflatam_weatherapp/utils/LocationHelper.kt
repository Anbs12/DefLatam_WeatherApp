package com.example.deflatam_weatherapp.utils

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

/** Clase para obtener la ubicación del usuario */

object LocationHelper {

    fun obtenerUbicacion(context: Context, callback: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                callback(null)
                return
            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    callback(location)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    callback(null)
                }
            }

            // Intentar obtener la última ubicación conocida primero
            val lastKnownLocation = if (isGpsEnabled) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (lastKnownLocation != null) {
                callback(lastKnownLocation)
                return
            }

            // Si no hay última ubicación conocida, solicitar nueva ubicación
            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager.requestLocationUpdates(
                provider,
                1000L, // 1 segundo
                10f,   // 10 metros
                locationListener
            )

        } catch (e: SecurityException) {
            callback(null)
        }
    }
}