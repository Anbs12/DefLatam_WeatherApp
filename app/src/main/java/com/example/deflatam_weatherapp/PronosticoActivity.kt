package com.example.deflatam_weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deflatam_weatherapp.adapter.PronosticoAdapter
import com.example.deflatam_weatherapp.cache.RoomCacheManager
import com.example.deflatam_weatherapp.entities.PronosticoEntity
import com.example.deflatam_weatherapp.model.DiaPronostico
import com.example.deflatam_weatherapp.model.PronosticoResponse
import com.example.deflatam_weatherapp.repository.ClimaRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class PronosticoActivity() : AppCompatActivity() {

    @Inject
    lateinit var climaRepository: ClimaRepository

    private lateinit var tvTituloPronostico: TextView
    private lateinit var rvPronostico: RecyclerView
    private lateinit var roomCacheManager: RoomCacheManager // NUEVA
    private var ciudadNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pronostico)

        initViews()
        setupRecyclerView()
        obtenerDatosIntent()
        cargarPronostico()
    }

    private fun initViews() {
        tvTituloPronostico = findViewById(R.id.tvTituloPronostico)
        rvPronostico = findViewById(R.id.rvPronostico)
        roomCacheManager = RoomCacheManager(this) // NUEVA

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pronóstico 5 días"
    }

    private fun setupRecyclerView() {
        rvPronostico.layoutManager = LinearLayoutManager(this)
    }


    private fun obtenerDatosIntent() {
        ciudadNombre = intent.getStringExtra("CIUDAD_NOMBRE") ?: "Ciudad no encontrada"
        tvTituloPronostico.text = "Pronóstico para los próximos 5 días en $ciudadNombre"
    }

    @SuppressLint("SetTextI18n")
    private fun cargarPronostico() {
        if (ciudadNombre.isEmpty() || ciudadNombre == "Ciudad no encontrada") {
            Toast.makeText(this, "Ciudad no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                tvTituloPronostico.text = "Cargando pronóstico para $ciudadNombre..."
                Log.d("PronosticoActivity", "Obteniendo info de la API")
                val texttvTituloPronosticoNoInternet = "Sin internet desde cache."

                val response: PronosticoResponse
                var pronosticosFiltrados: List<DiaPronostico>
                val listaCache = mutableListOf<PronosticoEntity>()

                if (isInternetAvailable(this@PronosticoActivity)) {
                    response = climaRepository.obtenerPronostico(ciudadNombre)
                    pronosticosFiltrados = filtrarPronosticosPorDia(response.list)
                    Log.d(
                        "PronosticoActivity",
                        "datos filtrados: ${pronosticosFiltrados.size} por día"
                    )

                    if (pronosticosFiltrados.isEmpty()) {
                        tvTituloPronostico.text = "No se encontraron pronósticos para el día actual"
                        return@launch
                    }

                    pronosticosFiltrados.forEach { dia ->
                        val entity = PronosticoEntity(
                            ciudad = ciudadNombre,
                            fechaHora = dia.dt_txt,
                            temperatura = dia.main.temp,
                            iconoClima = dia.weather[0].icon,
                            condicionPrincipal = dia.weather[0].main,
                            presion = dia.main.pressure,
                            humedad = dia.main.humidity,
                            temMax = dia.main.temp_max,
                            temMin = dia.main.temp_min,
                            weather = dia.weather
                        )
                        roomCacheManager.guardarPronosticoCache(entity)
                        listaCache.add(entity)
                        tvTituloPronostico.text =
                            "Pronóstico de ${listaCache.size} días para $ciudadNombre"
                    }
                } else {
                    listaCache.addAll(roomCacheManager.obtenerPronosticoCache(ciudadNombre))
                    tvTituloPronostico.text =
                        "Pronóstico de ${listaCache.size} días para $ciudadNombre $texttvTituloPronosticoNoInternet"
                }

                // Mostrar en RecyclerView
                val adapter = PronosticoAdapter(listaCache)
                rvPronostico.adapter = adapter

                Toast.makeText(this@PronosticoActivity, "Pronóstico cargado", Toast.LENGTH_SHORT)
                    .show()

            } catch (e: Exception) {
                tvTituloPronostico.text = "Error al cargar pronóstico"
                Toast.makeText(this@PronosticoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("PronosticoActivity", "Error al cargar pronóstico ${e.message}", e)
            }
        }
    }

    private fun filtrarPronosticosPorDia(pronosticos: List<DiaPronostico>): List<DiaPronostico> {
        val pronosticosFiltrados = mutableMapOf<String, DiaPronostico>()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        pronosticos.forEach { pronostico ->
            val parts = pronostico.dt_txt.split(" ")
            val fecha = parts[0]
            val hora = parts.getOrNull(1) ?: ""
            if (!pronosticosFiltrados.containsKey(fecha)) {
                pronosticosFiltrados[fecha] = pronostico
            } else {
                try {
                    val currentTime = timeFormat.parse(hora)
                    val diffCurrent = abs(currentTime!!.hours - 12)
                    val existing = pronosticosFiltrados[fecha]!!
                    val existingTime = timeFormat.parse(existing.dt_txt.split(" ")[1])
                    val diffExisting = abs(existingTime!!.hours - 12)
                    if (diffCurrent < diffExisting) pronosticosFiltrados[fecha] = pronostico
                } catch (_: Exception) {
                }
            }
        }
        return pronosticosFiltrados.values.toList()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

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
