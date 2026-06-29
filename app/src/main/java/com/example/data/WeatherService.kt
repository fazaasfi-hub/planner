package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

data class WeatherInfo(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val weatherCode: Int,
    val humidity: Int,
    val windspeed: Double
)

class WeatherService {
    private val client = OkHttpClient()

    suspend fun fetchWeather(city: String): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            // 1. Geocode search
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(city, "UTF-8")}&count=1&language=id&format=json"
            val geoRequest = Request.Builder().url(geoUrl).build()
            val geoResponse = client.newCall(geoRequest).execute()
            if (!geoResponse.isSuccessful) return@withContext null
            
            val geoBody = geoResponse.body?.string() ?: return@withContext null
            val geoJson = JSONObject(geoBody)
            if (!geoJson.has("results")) return@withContext null
            
            val results = geoJson.getJSONArray("results")
            if (results.length() == 0) return@withContext null
            
            val firstResult = results.getJSONObject(0)
            val name = firstResult.optString("name", city)
            val country = firstResult.optString("country", "")
            val lat = firstResult.getDouble("latitude")
            val lon = firstResult.getDouble("longitude")

            // 2. Fetch forecast
            val forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&hourly=relativehumidity_2m,weathercode&timezone=auto&forecast_days=1"
            val forecastRequest = Request.Builder().url(forecastUrl).build()
            val forecastResponse = client.newCall(forecastRequest).execute()
            if (!forecastResponse.isSuccessful) return@withContext null
            
            val forecastBody = forecastResponse.body?.string() ?: return@withContext null
            val forecastJson = JSONObject(forecastBody)
            
            val currentWeather = forecastJson.getJSONObject("current_weather")
            val temperature = currentWeather.getDouble("temperature")
            val weatherCode = currentWeather.getInt("weathercode")
            val windspeed = currentWeather.optDouble("windspeed", 0.0)

            // Parse humidity from hourly
            var humidity = 50
            if (forecastJson.has("hourly")) {
                val hourly = forecastJson.getJSONObject("hourly")
                if (hourly.has("relativehumidity_2m")) {
                    val humidities = hourly.getJSONArray("relativehumidity_2m")
                    if (humidities.length() > 0) {
                        humidity = humidities.getInt(0) // Default to first hour
                    }
                }
            }

            WeatherInfo(
                cityName = name,
                country = country,
                temperature = temperature,
                weatherCode = weatherCode,
                humidity = humidity,
                windspeed = windspeed
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
