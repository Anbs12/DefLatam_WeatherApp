package com.example.deflatam_weatherapp.di

import com.example.deflatam_weatherapp.api.ClimaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**Modulo de Dagger Hilt para proveer dependencias*/
@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    /**Proveer retrofit*/
    @Provides
    @Singleton
    fun retrofitProvider(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**Proveer el servicio de la API(toma retrofitProvider() como dependencia)*/
    @Provides
    @Singleton
    fun apiProvider(retrofit: Retrofit): ClimaApiService {
        return retrofit.create(ClimaApiService::class.java)
    }

}