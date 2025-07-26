package com.example.deflatam_weatherapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deflatam_weatherapp.R
import com.example.deflatam_weatherapp.model.DiaPronostico
import java.text.SimpleDateFormat
import java.util.Locale

/** Adaptador para el RecyclerView de pronóstico 5 días */

class PronosticoAdapter(private val pronosticos: List<DiaPronostico>) :
    RecyclerView.Adapter<PronosticoAdapter.PronosticoViewHolder>() {

    init {
        Log.d("PronosticoAdapter", "Adapter creado con ${pronosticos.size} pronósticos")
    }

    class PronosticoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcono: TextView = itemView.findViewById(R.id.ivIconoClima)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionPronostico)
        val tvTemperatura: TextView = itemView.findViewById(R.id.tvTemperaturaPronostico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PronosticoViewHolder {
        Log.d("PronosticoAdapter", "onCreateViewHolder llamado")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dia_pronostico, parent, false)
        return PronosticoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PronosticoViewHolder, position: Int) {
        val pronostico = pronosticos[position]
        Log.d("PronosticoAdapter", "Binding posición $position: ${pronostico.dt_txt}")

        try {
            // Fecha
            val fechaFormateada = formatearFecha(pronostico.dt_txt)
            holder.tvFecha.text = fechaFormateada

            // Descripción clima
            val descripcion = if (pronostico.weather.isNotEmpty()) {
                pronostico.weather[0].description.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            } else {
                "Sin descripción"
            }
            holder.tvDescripcion.text = descripcion

            // Temperatura
            holder.tvTemperatura.text = "${pronostico.main.temp.toInt()}°C"

            // Emoji del clima
            val main = if (pronostico.weather.isNotEmpty()) pronostico.weather[0].main else "Clear"
            val emoji = obtenerEmojiClima(main)
            holder.ivIcono.text = emoji

            Log.d("PronosticoAdapter", "Item $position configurado correctamente")
        } catch (e: Exception) {
            Log.e("PronosticoAdapter", "Error en onBindViewHolder posición $position: ${e.message}")
            holder.tvFecha.text = "Error"
            holder.tvDescripcion.text = "Error al cargar datos"
            holder.tvTemperatura.text = "--°C"
        }
    }

    override fun getItemCount(): Int = pronosticos.size

    private fun formatearFecha(fechaString: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaString)
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatoSalida.format(fecha!!)
        } catch (e: Exception) {
            fechaString.substring(0, 10)
        }
    }

    private fun obtenerEmojiClima(condicionClima: String): String {
        return when (condicionClima.lowercase()) {
            "clear" -> "☀️"
            "clouds" -> "☁️"
            "rain" -> "🌧️"
            "drizzle" -> "🌦️"
            "thunderstorm" -> "⛈️"
            "snow" -> "❄️"
            "mist", "fog" -> "🌫️"
            else -> "🌤️"
        }
    }
}