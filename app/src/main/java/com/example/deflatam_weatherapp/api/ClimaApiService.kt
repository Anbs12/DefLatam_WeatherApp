package com.example.deflatam_weatherapp.api

import com.example.deflatam_weatherapp.model.ClimaResponse
import com.example.deflatam_weatherapp.model.PronosticoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Interfaz para la API del clima para peticiones GET*/
interface ClimaApiService {

    @GET("weather")
    suspend fun getClimaPorCiudad(
        @Query("q") ciudad: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): Response<ClimaResponse>

    @GET("forecast")
    suspend fun getPronosticoPorCiudad(
        @Query("q") ciudad: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): Response<PronosticoResponse>

}