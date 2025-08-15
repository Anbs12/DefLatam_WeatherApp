package com.example.deflatam_weatherapp.repository

import com.example.deflatam_weatherapp.BuildConfig
import com.example.deflatam_weatherapp.api.ClimaApiService
import com.example.deflatam_weatherapp.model.ClimaResponse
import com.example.deflatam_weatherapp.model.PronosticoResponse
import javax.inject.Inject
import javax.inject.Singleton


/** Repositorio para obtener datos del clima */
@Singleton
class ClimaRepository @Inject constructor(
    private val api: ClimaApiService
) {

    private val ykpia = BuildConfig.API_KEY

    /**Obtiene el clima para una ciudad dada*/
    suspend fun obtenerClima(ciudad: String): ClimaResponse {
        val response = api.getClimaPorCiudad(ciudad, ykpia)

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            throw Exception("Error en la API: ${response.code()} - ${response.message()}")
        }
    }

    /** Obtiene pronóstico para una ciudad dada */
    suspend fun obtenerPronostico(ciudad: String): PronosticoResponse {
        val response = api.getPronosticoPorCiudad(ciudad, ykpia)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía del servidor")
        } else {
            throw Exception("Error en la API: ${response.code()} - ${response.message()}")
        }
    }

}