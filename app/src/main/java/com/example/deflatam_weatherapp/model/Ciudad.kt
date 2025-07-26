package com.example.deflatam_weatherapp.model

/** data class para representar una ciudad con sus coordenadas y la última consulta */
data class Ciudad (
    val nombre: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val ultimaConsulta: Long = System.currentTimeMillis()
)
