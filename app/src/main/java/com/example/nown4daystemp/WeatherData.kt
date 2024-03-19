package com.example.nown4daystemp

data class Weather(
    val main: Main,
    val name: String
)

data class DayForecast(
    val list: List<WeatherData>
)

data class WeatherData(
    val dt: Long,
    val main: Main
)

data class Main(
    val temp: Double
)