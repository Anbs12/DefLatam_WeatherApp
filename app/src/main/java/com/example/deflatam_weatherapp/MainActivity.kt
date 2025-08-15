package com.example.deflatam_weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.deflatam_weatherapp.cache.RoomCacheManager
import com.example.deflatam_weatherapp.databinding.ActivityMainBinding
import com.example.deflatam_weatherapp.entities.ClimaEntity
import com.example.deflatam_weatherapp.model.ClimaResponse
import com.example.deflatam_weatherapp.repository.ClimaRepository
import com.example.deflatam_weatherapp.ui.MainActivityViewModel
import com.example.deflatam_weatherapp.utils.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class MainActivity() : AppCompatActivity() {

    @Inject
    lateinit var climaRepository: ClimaRepository

    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_LOCATION = 100
    private var ultimaCiudadConsultada = ""
    private var ciudadesFavoritas = mutableListOf<String>()

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var roomCacheManager: RoomCacheManager

    /** Inicializa vistas y carga datos al crear la actividad */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomCacheManager = RoomCacheManager(this)
        binding.progressBar.visibility = View.VISIBLE
        setupListener()
        observarCache()

        if (isInternetAvailable(this)) {
            binding.tvNoInternet.visibility = View.GONE
            solicitarPermisosUbicacion()
            lifecycleScope.launch { obtenerCiudadesFavoritas() }
        } else {
            binding.tvUltimaCiudadConsultada.visibility = View.VISIBLE
            binding.tvNoInternet.visibility = View.VISIBLE
            /*binding.tvCiudad.text = viewModel.getLastCity()
            binding.tvTemperatura.text = viewModel.getLastTemp()*/
            viewModel.cargarClimaCache(viewModel.getLastCity() ?: "")
            lifecycleScope.launch { obtenerCiudadesFavoritas() }
            ultimaCiudadConsultada = viewModel.getLastCity() ?: ""

        }
    }

    /** Configura listeners de botones e interacción */
    private fun setupListener() {

        binding.btnBuscar.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val ciudad = binding.etCiudad.text.toString()
            if (ciudad.isNotBlank()) {
                obtenerClima(ciudad)
                binding.etCiudad.setText("")
            } else Toast.makeText(this, "Ingrese Ciudad", Toast.LENGTH_SHORT).show()


        }

        binding.btnUbicacion.setOnClickListener { solicitarPermisosUbicacion() }

        binding.layoutClima.setOnClickListener {
            if (ultimaCiudadConsultada.isNotEmpty()) {
                abrirPronostico(ultimaCiudadConsultada)
            } else {
                Toast.makeText(
                    this,
                    "No hay pronostico disponible o no tiene internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.ivFav.setOnClickListener {
            if (viewModel.isCityFavorite(ultimaCiudadConsultada)) {
                viewModel.removeCityFromFavorites(ultimaCiudadConsultada)
                Toast.makeText(this, "Ciudad eliminada de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addCityToFavorites(ultimaCiudadConsultada)
                Toast.makeText(this, "Ciudad agregada a favoritos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Observa datos de clima almacenados en cache */
    @SuppressLint("SetTextI18n")
    private fun observarCache() {
        viewModel.climaCache.observe(this, Observer { entity ->
            entity?.let {
                binding.tvCiudad.text = it.ciudad
                binding.tvTemperatura.text = "${it.temperatura.toInt()}°C"
                binding.tvDescripcion.text = it.descripcion
                ultimaCiudadConsultada = it.ciudad
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    /** Solicita y muestra datos de clima desde API y cache */
    private fun obtenerClima(ciudad: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val climaResponse = climaRepository.obtenerClima(ciudad)
                mostrarClima(climaResponse)
                viewModel.saveLastCity(
                    ultimaCiudadConsultada,
                    binding.tvTemperatura.text.toString()
                )
                viewModel.guardarClimaCache(
                    ClimaEntity(
                        ciudad = climaResponse.nombre,
                        temperatura = climaResponse.main.temp,
                        descripcion = climaResponse.weather[0].description,
                        iconoClima = climaResponse.weather[0].icon,
                        condicionPrincipal = climaResponse.weather[0].main,
                        presion = climaResponse.main.pressure,
                        humedad = climaResponse.main.humidity,
                        temMax = climaResponse.main.temp_max,
                        temMin = climaResponse.main.temp_min,
                        weather = climaResponse.weather
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al obtener el clima: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    /** Actualiza UI con los datos de clima recibidos */
    @SuppressLint("SetTextI18n")
    private fun mostrarClima(climaResponse: ClimaResponse) {
        binding.tvCiudad.text = climaResponse.nombre
        binding.tvDescripcion.text = climaResponse.weather[0].description
        binding.tvTemperatura.text = "${climaResponse.main.temp.toInt()}°C"
        ultimaCiudadConsultada = climaResponse.nombre
        binding.progressBar.visibility = View.GONE
    }

    /** Abre actividad de pronóstico para la ciudad dada */
    private fun abrirPronostico(ciudad: String) {
        startActivity(Intent(this, PronosticoActivity::class.java).apply {
            putExtra("CIUDAD_NOMBRE", ciudad)
        })
    }

    /** Solicita permisos de ubicación al usuario */
    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        } else obtenerUbicacion()
    }

    /** Maneja el resultado de solicitud de permisos */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) obtenerUbicacion()
        else Toast.makeText(
            this,
            "Permiso de ubicacion requerido para obtener clima actual",
            Toast.LENGTH_SHORT
        ).show()
    }

    /** Obtiene ubicacion del dispositivo y delega obtención de clima */
    private fun obtenerUbicacion() {
        LocationHelper.obtenerUbicacion(this) { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val ciudad =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                            .firstOrNull()?.locality ?: "Ciudad no encontrada"
                    if (ciudad != "Ciudad no encontrada") {
                        obtenerClima(ciudad)
                        binding.etCiudad.setText(ciudad)
                    } else mostrarErrorUbicacion("Ciudad no encontrada")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error al obtener ciudad", e)
                    mostrarErrorUbicacion("Error de geolocalizacion")
                }
            } else mostrarErrorUbicacion("Sin Ubicacion")
        }
    }

    /** Muestra mensaje de error relacionado con ubicación */

    private fun mostrarErrorUbicacion(mensaje: String) {
        try {
            binding.progressBar.visibility = View.GONE
            binding.tvCiudad.text = "❌ $mensaje"
            binding.tvTemperatura.text = " --°C"
            binding.tvDescripcion.text = "No se puede obtener ubicación"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

    }

    /** Obtiene lista de ciudades favoritas desde flujo */
    private suspend fun obtenerCiudadesFavoritas() {
        viewModel.favoriteCities.collect { favoriteCities ->
            ciudadesFavoritas.clear()
            ciudadesFavoritas.addAll(favoriteCities)
            var ciudadFavtxt: String
            if (ciudadesFavoritas.isNotEmpty()) {
                ciudadFavtxt = "Ciudades favoritas: ${ciudadesFavoritas.joinToString(", ")}"
            } else {
                ciudadFavtxt = "No has añadido ciudades favoritas aun..."
            }
            binding.tvCiudadesFavorita.text = ciudadFavtxt
        }
    }

    /** Verifica si hay conexión a internet */
    @SuppressLint("ObsoleteSdkInt")
    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                ) || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            } == true
        } else {
            @Suppress("DEPRECATION") cm.activeNetworkInfo?.isConnected == true
        }
    }
}

