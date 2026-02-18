package nl.bikecheck.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object WeatherHelper {

    private const val API_KEY = "b2993d09a467a0172653bfecc4851b74"
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/forecast"

    suspend fun fetchWeather(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL?lat=$lat&lon=$lon&units=metric&appid=$API_KEY"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val list = json.getJSONArray("list")

            val current = list.getJSONObject(0)
            val inFourHours = list.getJSONObject(1)

            val currentTemp = Math.round(current.getJSONObject("main").getDouble("temp"))
            val tempIn4Hours = Math.round(inFourHours.getJSONObject("main").getDouble("temp"))
            val rainChance = Math.round(current.getDouble("pop") * 100)
            val weatherId = current.getJSONArray("weather").getJSONObject(0).getInt("id")

            val emoji = when {
                weatherId in 200..299 -> "⛈️"
                weatherId in 300..499 -> "🌧️"
                weatherId in 500..599 -> "🌧️"
                weatherId in 600..699 -> "🌨️"
                weatherId in 700..799 -> "🌫️"
                weatherId == 800      -> "☀️"
                weatherId > 800       -> "☁️"
                else                  -> "☀️"
            }

            buildString {
                append("$emoji ${currentTemp}°C    🕐 Over 4 uur: ${tempIn4Hours}°C")
                if (rainChance > 30) {
                    append("\n⚠️ ${rainChance}% kans op regen")
                }
            }
        } catch (e: Exception) {
            "Kon het weer niet ophalen"
        }
    }
}
