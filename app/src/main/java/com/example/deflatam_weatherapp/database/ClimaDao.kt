// ClimaDao.kt
package com.example.deflatam_weatherapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.deflatam_weatherapp.entities.ClimaEntity
import com.example.deflatam_weatherapp.entities.PronosticoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimaDao {

    // Clima Actual
    @Query("SELECT * FROM clima_actual WHERE ciudad = :ciudad")
    suspend fun obtenerClimaActual(ciudad: String): ClimaEntity?

    @Query("SELECT * FROM clima_actual ORDER BY fechaActualizacion DESC")
    fun getAllClimasFlow(): Flow<List<ClimaEntity>>

    @Query("SELECT * FROM clima_actual ORDER BY fechaActualizacion DESC")
    suspend fun getAllClimas(): List<ClimaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClima(clima: ClimaEntity)

    @Delete
    suspend fun eliminarClima(clima: ClimaEntity)

    // Pronóstico
    @Query("SELECT * FROM pronostico WHERE ciudad = :ciudad")
    suspend fun getPronosticoByCiudad(ciudad: String): List<PronosticoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPronostico(pronostico: PronosticoEntity)

    @Query("DELETE FROM pronostico WHERE ciudad = :ciudad")
    suspend fun eliminarPronostico(ciudad: String)

    // Utilidades
    @Query("SELECT COUNT(*) FROM clima_actual")
    suspend fun contarClimas(): Int

    @Query("SELECT COUNT(*) FROM pronostico")
    suspend fun contarPronosticos(): Int
}
