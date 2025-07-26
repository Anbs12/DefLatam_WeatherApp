package com.example.deflatam_weatherapp.model

/** data class para obtener la listado de ciudades */
data class PronosticoResponse(
    val list: List<DiaPronostico>
)

/** data class para obtener el pronóstico por día */
data class DiaPronostico(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>
)
