package com.example.deflatam_weatherapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.deflatam_weatherapp.entities.ClimaEntity
import com.example.deflatam_weatherapp.entities.Converters
import com.example.deflatam_weatherapp.entities.PronosticoEntity


@Database(
    entities = [ClimaEntity::class, PronosticoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClimaDatabase : RoomDatabase() {
    abstract fun climaDao(): ClimaDao

    companion object{
        @Volatile
        private var INSTANCE: ClimaDatabase? = null

        fun getDatabase(context: Context): ClimaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClimaDatabase::class.java,
                    "clima_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}