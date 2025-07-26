package com.example.deflatam_weatherapp.cache

import android.content.Context
import com.example.deflatam_weatherapp.model.DiaPronostico
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Gestiona el almacenamiento y la recuperación de datos en SharedPreferences. */
/** Gestiona la logica para guardar y recuperar ciudades favoritas desde la cache local. */
class CacheManager(context: Context) {

    /** Nombre del archivo de SharedPreferences donde se guardara la caché. */
    private val prefsName = "favorite_cities_cache"

    /** Clave para almacenar la lista de ciudades en formato JSON. */
    private val citiesKey = "favorite_cities_list"

    /** Instancia de SharedPreferences para acceder al almacenamiento local. */
    private val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /** Instancia de Gson para convertir la lista a/desde JSON. */
    private val gson = Gson()

    /** Flujo de estado mutable que contiene la lista actual de favoritos. */
    private val _favoritesFlow = MutableStateFlow<List<String>>(emptyList())

    /** Flujo de estado público e inmutable para que la UI observe los cambios en los favoritos. */
    val favoritesFlow: StateFlow<List<String>> = _favoritesFlow.asStateFlow()

    init {
        // Al iniciar, carga las ciudades guardadas desde la caché y actualiza el flujo.
        _favoritesFlow.value = loadFavoritesFromCache()
    }

    /** Añade una ciudad a la lista de favoritos si no existe. */
    fun addFavorite(city: String) {
        val currentFavorites = _favoritesFlow.value.toMutableList()
        if (!currentFavorites.contains(city)) {
            currentFavorites.add(city)
            saveFavoritesToCache(currentFavorites)
            _favoritesFlow.value = currentFavorites.toList() // Emite la lista actualizada
            println("Ciudad añadida: $city. Favoritos actuales: ${_favoritesFlow.value}")
        }
    }

    /** Elimina una ciudad de la lista de favoritos. */
    fun removeFavorite(city: String) {
        val currentFavorites = _favoritesFlow.value.toMutableList()
        if (currentFavorites.remove(city)) {
            saveFavoritesToCache(currentFavorites)
            _favoritesFlow.value = currentFavorites.toList() // Emite la lista actualizada
            println("Ciudad eliminada: $city. Favoritos actuales: ${_favoritesFlow.value}")
        }
    }

    /** Devuelve la lista actual de ciudades favoritas de forma síncrona. */
    fun getFavorites(): List<String> {
        return _favoritesFlow.value
    }

    /** Comprueba si una ciudad ya está en la lista de favoritos. */
    fun isFavorite(city: String): Boolean {
        return _favoritesFlow.value.contains(city)
    }

    /** Guarda la lista de favoritos en SharedPreferences como un string JSON. */
    private fun saveFavoritesToCache(cities: List<String>) {
        val jsonString = gson.toJson(cities)
        sharedPreferences.edit().putString(citiesKey, jsonString).apply()
    }

    /** Carga la lista de favoritos desde SharedPreferences. */
    private fun loadFavoritesFromCache(): List<String> {
        val jsonString = sharedPreferences.getString(citiesKey, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList() // Devuelve una lista vacía si no hay nada guardado.
        }
    }

    fun guardarListaPronosticos(listaPronosticos: List<DiaPronostico>) {
        val jsonString = gson.toJson(listaPronosticos)
        sharedPreferences.edit().putString("lista_pronosticos", jsonString).apply()
    }

    fun obtenerListaPronosticos(): List<DiaPronostico> {
        val jsonString = sharedPreferences.getString("lista_pronosticos", null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<DiaPronostico>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun saveLastCityAndTemp(city: String, temp: String) {
        sharedPreferences.edit().putString("last_city", city).apply()
        sharedPreferences.edit().putString("last_temp", temp).apply()
    }

    fun getLastCity(): String? {
        return sharedPreferences.getString("last_city", null)
    }

    fun getLastTemp(): String? {
        return sharedPreferences.getString("last_temp", null)
    }
}