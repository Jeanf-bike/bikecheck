package nl.bikecheck

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.bikecheck.adapter.ChecklistAdapter
import nl.bikecheck.databinding.ActivityChecklistBinding
import nl.bikecheck.util.PrefsHelper
import nl.bikecheck.util.WeatherHelper

class ChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistBinding
    private lateinit var adapter: ChecklistAdapter
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var bikeName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bikeName = intent.getStringExtra(MainActivity.EXTRA_BIKE_NAME) ?: run { finish(); return }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = bikeName
        }

        prefsHelper = PrefsHelper(this)
        setupChecklist()
        fetchWeatherAndUpdateItems()
    }

    private fun setupChecklist() {
        val items = prefsHelper.loadChecklist(bikeName) ?: BikeData.defaultItemsFor(bikeName)

        adapter = ChecklistAdapter(items) {
            prefsHelper.saveChecklist(bikeName, adapter.getItems())
            updateProgress()
        }

        binding.rvChecklist.layoutManager = LinearLayoutManager(this)
        binding.rvChecklist.adapter = adapter
        updateProgress()
    }

    private fun fetchWeatherAndUpdateItems() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener { location ->
                if (location == null) return@addOnSuccessListener
                CoroutineScope(Dispatchers.Main).launch {
                    val weather = WeatherHelper.fetchWeather(location.latitude, location.longitude)

                    // Weer tonen
                    binding.tvWeather.text = weather.displayText

                    // Conditionele items bijwerken op basis van temperatuur en UV
                    val conditional = BikeData.conditionalItems(weather.temperatureCelsius, weather.uvIndex)
                    adapter.updateConditionalItems(conditional)
                    prefsHelper.saveChecklist(bikeName, adapter.getItems())
                    updateProgress()
                }
            }
    }

    private fun updateProgress() {
        val items = adapter.getItems()
        val total = items.size
        val checked = items.count { it.isChecked }
        binding.progressBar.max = total
        binding.progressBar.progress = checked
        binding.tvProgress.text = "$checked / $total"
        val pct = if (total > 0) checked * 100 / total else 0
        binding.tvPercentage.text = "$pct%"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.checklist_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_reset -> {
            AlertDialog.Builder(this)
                .setTitle("Checklist resetten")
                .setMessage("Weet je zeker dat je alle vinkjes wilt verwijderen?")
                .setPositiveButton("Reset") { _, _ ->
                    prefsHelper.clearChecklist(bikeName)
                    setupChecklist()
                    fetchWeatherAndUpdateItems()
                }
                .setNegativeButton("Annuleren", null)
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
