package com.example.nown4daystemp

import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService
) {

    suspend fun getCurrentWeather(cityName: String, appId: String): Weather {
        return apiService.getCurrentWeather(cityName, appId)
    }

    suspend fun getNextDaysForecast(cityName: String, appId: String): DayForecast {
        return apiService.getNextDaysForecast(cityName, appId)
    }
}
