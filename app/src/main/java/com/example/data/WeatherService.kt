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
            // 1. Geocode search - increased count to 5 and will try to prioritize Indonesian results if multiple found
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(city, "UTF-8")}&count=5&language=id&format=json"
            val geoRequest = Request.Builder().url(geoUrl).build()
            val geoResponse = client.newCall(geoRequest).execute()
            if (!geoResponse.isSuccessful) return@withContext null
            
            val geoBody = geoResponse.body?.string() ?: return@withContext null
            val geoJson = JSONObject(geoBody)
            if (!geoJson.has("results")) return@withContext null
            
            val results = geoJson.getJSONArray("results")
            if (results.length() == 0) return@withContext null
            
            // Prioritize Indonesia (ID) if searching from Indonesia
            var firstResult = results.getJSONObject(0)
            for (i in 0 until results.length()) {
                val res = results.getJSONObject(i)
                if (res.optString("country_code", "").equals("ID", ignoreCase = true)) {
                    firstResult = res
                    break
                }
            }
            
            val name = firstResult.optString("name", city)
            val country = firstResult.optString("country", "")
            val lat = firstResult.getDouble("latitude")
            val lon = firstResult.getDouble("longitude")

            // 2. Fetch current weather using the modern 'current' parameter
            val forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"
            val forecastRequest = Request.Builder().url(forecastUrl).build()
            val forecastResponse = client.newCall(forecastRequest).execute()
            if (!forecastResponse.isSuccessful) return@withContext null
            
            val forecastBody = forecastResponse.body?.string() ?: return@withContext null
            val forecastJson = JSONObject(forecastBody)
            
            if (!forecastJson.has("current")) return@withContext null
            val current = forecastJson.getJSONObject("current")
            
            val temperature = current.getDouble("temperature_2m")
            val weatherCode = current.getInt("weather_code")
            val windspeed = current.optDouble("wind_speed_10m", 0.0)
            val humidity = current.optInt("relative_humidity_2m", 50)

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
