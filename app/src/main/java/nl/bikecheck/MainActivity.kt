package nl.bikecheck

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.bikecheck.adapter.BikeAdapter
import nl.bikecheck.databinding.ActivityMainBinding
import nl.bikecheck.model.Bike
import nl.bikecheck.util.WeatherHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val EXTRA_BIKE_NAME = "bike_name"
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    private val bikes = listOf(
        Bike("Ultimate", "Race fiets",   "",         R.drawable.bike_bg_ultimate),
        Bike("Atalaya",  "Gravel fiets", "",         R.drawable.bike_bg_atalaya),
        Bike("Neuron",   "Mountain bike","Fully",    R.drawable.bike_bg_neuron),
        Bike("Exceed",   "Mountain bike","Hardtail", R.drawable.bike_bg_exceed)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.rvBikes.layoutManager = GridLayoutManager(this, 2)
        binding.rvBikes.adapter = BikeAdapter(bikes) { bike ->
            startActivity(Intent(this, ChecklistActivity::class.java).apply {
                putExtra(EXTRA_BIKE_NAME, bike.name)
            })
        }

        requestWeather()
    }

    private fun requestWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fetchWeather()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun fetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val weather = WeatherHelper.fetchWeather(location.latitude, location.longitude)
                        binding.tvWeather.text = weather.displayText
                    }
                } else {
                    binding.tvWeather.text = "Locatie niet beschikbaar"
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeather()
        } else {
            binding.tvWeather.text = "Locatietoestemming geweigerd"
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fetchWeather()
        }
    }
}
