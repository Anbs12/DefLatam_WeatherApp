package com.example.deflatam_weatherapp.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.deflatam_weatherapp.MainActivity
import com.example.deflatam_weatherapp.R
import com.example.deflatam_weatherapp.database.ClimaDao
import com.example.deflatam_weatherapp.database.ClimaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Clase AppWidgetProvider para nuestro widget de clima.
 * Esta clase es responsable de manejar las actualizaciones del widget.
 */
class WeatherAppWidgetProvider : AppWidgetProvider() {

    // Define un CoroutineScope para ejecutar operaciones asíncronas
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Se llama cuando el widget se actualiza.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Itera sobre todos los IDs de widgets que necesitan ser actualizados
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Funcion auxiliar para actualizar una instancia específica del widget.
     * Aquí es donde obtendrías tus datos de clima reales y actualizarías la UI del widget.
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Crea un objeto RemoteViews para el diseño del widget.
        // RemoteViews permite actualizar la UI del widget desde fuera de su proceso.
        val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)

        // Lanza una corrutina para realizar la operación de base de datos de forma asíncrona
        scope.launch {
            try {
                // Obtén la instancia de la base de datos y el DAO
                val climaDao: ClimaDao = ClimaDatabase.getDatabase(context).climaDao()

                // Obtén el clima más reciente
                val latestClima = climaDao.getAllClimas().firstOrNull() // Obtiene el primer clima (el más reciente por la query)

                val temperature: String
                val description: String
                val lastUpdated: String

                if (latestClima != null) {
                    temperature = "${latestClima.temperatura}°C"
                    description = latestClima.descripcion
                    lastUpdated = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(latestClima.fechaActualizacion))
                } else {
                    // Si no hay datos, muestra placeholders
                    temperature = "--°C"
                    description = "No hay datos"
                    lastUpdated = "Actualizado: --:--"
                }

                // Actualiza la UI del widget en el hilo principal
                views.setTextViewText(R.id.widget_temperature, temperature)
                views.setTextViewText(R.id.widget_description, description)
                views.setTextViewText(R.id.widget_last_updated, "Actualizado: $lastUpdated")

                // Pide al AppWidgetManager que actualice el widget con las nuevas vistas.
                appWidgetManager.updateAppWidget(appWidgetId, views)

            } catch (e: Exception) {
                // Manejo de errores, por ejemplo, loguear el error o mostrar un mensaje en el widget
                e.printStackTrace()
                // Opcional: Actualizar el widget con un mensaje de error
                views.setTextViewText(R.id.widget_temperature, "Error")
                views.setTextViewText(R.id.widget_description, "No se pudo cargar")
                views.setTextViewText(R.id.widget_last_updated, "Reintentar")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // --- Configurar un Intent para abrir la aplicación al hacer clic en el widget ---
        // Crea un Intent para abrir la actividad principal
        val intent = Intent(context, MainActivity::class.java)
        // Añade una bandera para que si la actividad ya está en ejecución
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // Crea un PendingIntent que se ejecutara cuando el usuario haga clic en el widget.
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId, // Usa el ID del widget como requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE es requerido en Android 12+
        )

        // Asigna el PendingIntent al layout raíz del widget para que responda a los clics
        views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)
    }
}