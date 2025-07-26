package com.example.deflatam_weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.deflatam_weatherapp.databinding.ActivityMainBinding
import com.example.deflatam_weatherapp.repository.ClimaRepository
import com.example.deflatam_weatherapp.utils.LocationHelper
import com.example.deflatam_weatherapp.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val climaRepository = ClimaRepository(this)
    private val PERMISSIONS_REQUEST_LOCATION = 100
    private var ultimaCiudadConsultada = ""
    private var ciudadesFavoritas = mutableListOf<String>()

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.VISIBLE
        setupListener()


        if (isInternetAvailable(this)) {
            binding.tvNoInternet.visibility = View.GONE

            //Geolocalizacion automatica
            solicitarPermisosUbicacion()
            lifecycleScope.launch {
                obtenerCiudadesFavoritas()
            }
        } else {
            binding.tvUltimaCiudadConsultada.visibility = View.VISIBLE
            binding.tvNoInternet.visibility = View.VISIBLE
            binding.tvCiudad.text = viewModel.getLastCity()
            binding.tvTemperatura.text = viewModel.getLastTemp()
            lifecycleScope.launch {
                obtenerCiudadesFavoritas()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        //viewModel.saveLastCity(ultimaCiudadConsultada, binding.tvTemperatura.text.toString())
    }

    private fun setupListener() {

        binding.btnBuscar.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val ciudad = binding.etCiudad.text.toString()
            if (ciudad.isNotBlank()) {
                obtenerClima(ciudad)
            } else {
                Toast.makeText(this, "Ingrese Ciudad", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUbicacion.setOnClickListener {
            solicitarPermisosUbicacion()
        }

        binding.layoutClima.setOnClickListener {
            if (ultimaCiudadConsultada.isNotEmpty()) {
                abrirPronostico(ultimaCiudadConsultada)
            } else {
                abrirPronostico(ultimaCiudadConsultada)
                Toast.makeText(this, "No hay pronostico disponible o no tiene internet", Toast.LENGTH_SHORT).show()
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

    private fun obtenerClima(ciudad: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val climaResponse = climaRepository.obtenerClima(ciudad)
                binding.tvCiudad.text = climaResponse.nombre
                binding.tvDescripcion.text = climaResponse.weather[0].description
                val temperatura = "${climaResponse.main.temp.toInt()}°C"
                binding.tvTemperatura.text = temperatura
                ultimaCiudadConsultada = climaResponse.nombre
                binding.progressBar.visibility = View.GONE
                viewModel.saveLastCity(ultimaCiudadConsultada, temperatura)
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

    private fun abrirPronostico(ciudad: String) {
        val intent = Intent(this, PronosticoActivity::class.java)
        intent.putExtra("CIUDAD_NOMBRE", ciudad)
        startActivity(intent)
    }

    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            obtenerUbicacion()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion()
        } else {
            Toast.makeText(
                this,
                "Permiso de ubicacion requerido para obtener clima actual",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerUbicacion() {

        LocationHelper.obtenerUbicacion(this) { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val direcciones =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val ciudad = direcciones?.firstOrNull()?.locality ?: "Ciudad no encontrada"
                    if (ciudad != "Ciudad no encontrada") {
                        obtenerClima(ciudad)
                        binding.etCiudad.setText(ciudad)
                        binding.progressBar.visibility = View.GONE
                    } else {
                        binding.tvCiudad.text = "❌ Ciudad no encontrada"
                        binding.tvTemperatura.text = " --°C"
                        binding.tvDescripcion.text = "Busca una ciudad para ver el clima"
                        Toast.makeText(
                            this,
                            "Error al obtener nombre de la ciudad",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressBar.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvCiudad.text = "❌ Error de geolocalizacion"
                    binding.tvTemperatura.text = " --°C"
                    binding.tvDescripcion.text = "Error al obtener la ciudad donde te encuentras"
                    Toast.makeText(this, "Error al obtener nombre de la ciudad", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                binding.progressBar.visibility = View.GONE
                binding.tvCiudad.text = " Sin Ubicacion"
                binding.tvTemperatura.text = " --°C"
                binding.tvDescripcion.text = "No se puede obtener ubicación"
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun obtenerCiudadesFavoritas() {
        viewModel.favoriteCities.collect { favoriteCities ->
            ciudadesFavoritas.clear()
            ciudadesFavoritas.addAll(favoriteCities)
            binding.tvCiudadesFavorita.text = ciudadesFavoritas.joinToString(", ")
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // para otras redes como Ethernet, VPN, etc.
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } else {
            // Métodos depreciados para API < 23 (Marshmallow)
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}