package com.example.nown4daystemp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val appId: String = "9b8cb8c7f11c077f8c4e217974d9ee40"

    private val _currentWeather = MutableLiveData<Map<String, Int>>()
    val currentWeather: LiveData<Map<String, Int>>
        get() = _currentWeather

    private val _errorOccurred = MutableLiveData<Boolean>()
    val errorOccurred: LiveData<Boolean>
        get() = _errorOccurred

    private val _nextDayForecast = MutableLiveData<Map<String, Int>>()
    val averageTemperatures: LiveData<Map<String, Int>>
        get() = _nextDayForecast


    fun fetchWeatherResults(cityName: String) {
        fetchWeather(cityName)
        fetchNextDaysForecast(cityName)
    }

    private fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            try {
                val currentWeather = repository.getCurrentWeather(cityName, appId)
                val temperatureInCelsius = (currentWeather.main.temp - 273.15).toInt()
                _currentWeather.value = mapOf(
                    cityName to temperatureInCelsius
                )
            } catch (e: Exception) {
                _errorOccurred.value = true
            }
        }
    }

    private fun fetchNextDaysForecast(cityName: String) {
        viewModelScope.launch {
            try {
                val nextDaysForecast = repository.getNextDaysForecast(cityName, appId)
                _nextDayForecast.value = calculateAverageTemperatures(nextDaysForecast)
            } catch (e: Exception) {
                _errorOccurred.value = true
            }
        }
    }

    private fun calculateAverageTemperatures(dayForecast: DayForecast): Map<String, Int> {
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val groupedByDate = dayForecast.list.groupBy { (it.dt / 86400) * 86400 }

        return groupedByDate.mapValues { (_, weatherDataList) ->
            val temperatures = weatherDataList.map { it.main.temp - 273.15 }
            val averageTemp = temperatures.sum() / temperatures.size.toDouble()
            averageTemp.toInt()
        }.mapKeys { (timestamp, _) ->
            val date = Date(timestamp * 1000L)
            dayFormat.format(date)
        }
    }
}
