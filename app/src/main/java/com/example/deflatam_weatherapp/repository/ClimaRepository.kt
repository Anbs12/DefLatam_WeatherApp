package com.example.deflatam_weatherapp.repository

import android.content.Context
import android.util.Log
import com.example.deflatam_weatherapp.api.ClimaApiService
import com.example.deflatam_weatherapp.api.RetrofitClient
import com.example.deflatam_weatherapp.model.ClimaResponse
import com.example.deflatam_weatherapp.model.PronosticoResponse
import com.example.deflatam_weatherapp.utils.NetworkManager


/** Repositorio para obtener datos del clima */
class ClimaRepository(private val context: Context) {

    private val api: ClimaApiService = RetrofitClient.instance.create(ClimaApiService::class.java)
    private val apiKey = "cc5f5c259c2348ba4fc544ae184ad7c8"
    //private val cacheManager = CacheManager(context)
    private val appContext = context

    companion object {
        private const val TAG = "ClimaRepository"
    }

    suspend fun obtenerClima(ciudad: String): ClimaResponse {
        val response = api.getClimaPorCiudad(ciudad, apiKey)

        //validacion de internet
        val isOnline = NetworkManager.isNetworkAvailable(appContext)
        val networkType = NetworkManager.getNetworkType(appContext)

        Log.d(TAG, "Estado de red: Online=$isOnline, Tipo de red: $networkType")


        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            throw Exception("Error en la API: ${response.code()} - ${response.message()}")
        }
    }

    suspend fun obtenerPronostico(ciudad: String): PronosticoResponse {
        val response = api.getPronosticoPorCiudad(ciudad, apiKey)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            throw Exception("Error en la API: ${response.code()} - ${response.message()}")
        }
    }
}