package nl.bikecheck.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WeatherData(
    val displayText: String,
    val temperatureCelsius: Double,
    val uvIndex: Double,
    val rainChance: Int
)

object WeatherHelper {

    private const val API_KEY = "b2993d09a467a0172653bfecc4851b74"
    private const val FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast"
    private const val UVI_URL = "https://api.openweathermap.org/data/2.5/uvi"

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherData = withContext(Dispatchers.IO) {
        coroutineScope {
            val forecastDeferred = async { fetchForecast(lat, lon) }
            val uviDeferred = async { fetchUvi(lat, lon) }

            val forecast = forecastDeferred.await()
            val uvi = uviDeferred.await()

            forecast.copy(
                uvIndex = uvi,
                displayText = buildDisplayText(forecast, uvi)
            )
        }
    }

    private fun fetchForecast(lat: Double, lon: Double): WeatherData {
        return try {
            val url = "$FORECAST_URL?lat=$lat&lon=$lon&units=metric&appid=$API_KEY"
            val json = JSONObject(URL(url).readText())
            val list = json.getJSONArray("list")

            val current = list.getJSONObject(0)
            val inFourHours = list.getJSONObject(1)

            val temp = current.getJSONObject("main").getDouble("temp")
            val tempIn4h = Math.round(inFourHours.getJSONObject("main").getDouble("temp"))
            val rain = Math.round(current.getDouble("pop") * 100)
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

            val text = buildString {
                append("$emoji ${Math.round(temp)}°C    🕐 Over 4 uur: ${tempIn4h}°C")
                if (rain > 30) append("\n⚠️ ${rain}% kans op regen")
            }

            WeatherData(displayText = text, temperatureCelsius = temp, uvIndex = 0.0, rainChance = rain.toInt())
        } catch (e: Exception) {
            WeatherData("Kon het weer niet ophalen", 15.0, 0.0, 0)
        }
    }

    private fun fetchUvi(lat: Double, lon: Double): Double {
        return try {
            val url = "$UVI_URL?lat=$lat&lon=$lon&appid=$API_KEY"
            val json = JSONObject(URL(url).readText())
            json.getDouble("value")
        } catch (e: Exception) {
            0.0
        }
    }

    private fun buildDisplayText(data: WeatherData, uvi: Double): String = buildString {
        append(data.displayText)
        if (uvi >= 3) {
            val level = when {
                uvi >= 8 -> "zeer hoog"
                uvi >= 6 -> "hoog"
                else     -> "matig"
            }
            append("\n🔆 UV-index ${uvi.toInt()} ($level)")
        }
    }
}
