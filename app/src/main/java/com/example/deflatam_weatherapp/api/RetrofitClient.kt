package com.example.deflatam_weatherapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Retrofit client para la API del clima */
object RetrofitClient {
    private const val BASE_URL ="https://api.openweathermap.org/data/2.5/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}