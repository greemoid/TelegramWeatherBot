package data.repository

import data.api.WeatherApi
import data.models.WeatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val weatherApi: WeatherApi) {

    suspend fun getCurrentWeather(cityName: String, apiKey: String): WeatherModel {
        return withContext(Dispatchers.IO) {
            weatherApi.getWeather(cityName, apiKey)
        }
    }
}