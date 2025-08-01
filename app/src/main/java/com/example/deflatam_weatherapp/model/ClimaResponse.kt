package com.example.deflatam_weatherapp.model

import com.google.gson.annotations.SerializedName

data class ClimaResponse(
    @SerializedName("name") val nombre: String,
    val weather: List<Weather>,
    val main: Main
)

data class  Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("temp_min") val temp_min: Double,
    @SerializedName("temp_max") val temp_max: Double

)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
