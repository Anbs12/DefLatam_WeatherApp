package com.example.deflatam_weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.deflatam_weatherapp.cache.CacheManager
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    /** Instancia única del gestor de favoritos*/
    private val favoritesManager = CacheManager(application.applicationContext)

    /**Expone el flujo de favoritos para que la UI lo observe*/
    val favoriteCities: StateFlow<List<String>> = favoritesManager.favoritesFlow

    /** Funcion para guardar la ultima ciudad consultada*/
    fun saveLastCity(city: String, temp: String) {
        favoritesManager.saveLastCityAndTemp(city, temp)
    }

    /** Funcion para obtener la ultima ciudad consultada*/
    fun getLastCity(): String? {
        return favoritesManager.getLastCity()
    }

    fun getLastTemp(): String? {
        return favoritesManager.getLastTemp()
    }

    /** Funcion para añadir una ciudad a favoritos.*/
    fun addCityToFavorites(city: String) {
        favoritesManager.addFavorite(city)
    }

    /** Funcion para comprobar si una ciudad esta en favoritos.*/
    fun isCityFavorite(ciudad: String): Boolean {
        return favoritesManager.isFavorite(ciudad)
    }

    /** Funcion para eliminar una ciudad de favoritos*/
    fun removeCityFromFavorites(city: String) {
        favoritesManager.removeFavorite(city)
    }
}