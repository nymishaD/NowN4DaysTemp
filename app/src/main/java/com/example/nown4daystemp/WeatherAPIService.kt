package com.example.nown4daystemp

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") appId: String
    ): Weather

    @GET("forecast")
    suspend fun getNextDaysForecast(
        @Query("q") cityName: String,
        @Query("appid") appId: String
    ): DayForecast
}
