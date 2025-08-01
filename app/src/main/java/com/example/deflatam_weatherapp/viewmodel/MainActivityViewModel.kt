package com.example.deflatam_weatherapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.deflatam_weatherapp.cache.CacheManager
import com.example.deflatam_weatherapp.cache.RoomCacheManager 
import com.example.deflatam_weatherapp.entities.ClimaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    /** Instancia única del gestor de favoritos*/
    private val favoritesManager = CacheManager(application.applicationContext)

    /**Expone el flujo de favoritos para que la UI lo observe*/
    val favoriteCities: StateFlow<List<String>> = favoritesManager.favoritesFlow

    /** Room Cache Manager */
    private val roomCacheManager = RoomCacheManager(application.applicationContext) 

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

    // Funciones con room database

    /** Obtener clima cache */
    private val _climaCache = MutableLiveData<ClimaEntity?>()
    val climaCache: LiveData<ClimaEntity?> = _climaCache

    fun cargarClimaCache(ciudad: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = roomCacheManager.obtenerClimaCache(ciudad)
            _climaCache.postValue(entity)
        }
    }

    /** Guardar clima en cache */
    fun guardarClimaCache(entity: ClimaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            roomCacheManager.guardarClimaCache(entity)
        }
    }
}
