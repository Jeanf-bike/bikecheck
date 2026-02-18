package nl.bikecheck

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import nl.bikecheck.adapter.ChecklistAdapter
import nl.bikecheck.databinding.ActivityMainBinding
import nl.bikecheck.model.ChecklistCategory
import nl.bikecheck.model.ChecklistItem
import nl.bikecheck.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChecklistAdapter
    private lateinit var prefsHelper: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        prefsHelper = PrefsHelper(this)
        setupRecyclerView()
        updateProgress()
        updateLastCheckDate()
    }

    private fun defaultCategories(): MutableList<ChecklistCategory> = mutableListOf(
        ChecklistCategory(
            name = "Veiligheid",
            icon = "🔒",
            items = mutableListOf(
                ChecklistItem("v1", "Voorrem werkt goed"),
                ChecklistItem("v2", "Achterrem werkt goed"),
                ChecklistItem("v3", "Voorlicht werkt"),
                ChecklistItem("v4", "Achterlicht werkt"),
                ChecklistItem("v5", "Reflectoren aanwezig en schoon"),
                ChecklistItem("v6", "Bel werkt"),
                ChecklistItem("v7", "Stuur zit stevig vast"),
                ChecklistItem("v8", "Zadel zit stevig vast en op hoogte")
            )
        ),
        ChecklistCategory(
            name = "Aandrijving",
            icon = "⚙️",
            items = mutableListOf(
                ChecklistItem("a1", "Ketting is gesmeerd"),
                ChecklistItem("a2", "Ketting is niet gesleten of roestig"),
                ChecklistItem("a3", "Versnellingen schakelen soepel"),
                ChecklistItem("a4", "Pedalen zitten goed vast")
            )
        ),
        ChecklistCategory(
            name = "Banden & Wielen",
            icon = "⬤",
            items = mutableListOf(
                ChecklistItem("b1", "Voorband voldoende gespannen"),
                ChecklistItem("b2", "Achterband voldoende gespannen"),
                ChecklistItem("b3", "Banden zijn niet gesleten of beschadigd"),
                ChecklistItem("b4", "Wielen draaien recht (niet scheef of wankel)")
            )
        ),
        ChecklistCategory(
            name = "Overig",
            icon = "🔧",
            items = mutableListOf(
                ChecklistItem("o1", "Spatborden zitten stevig vast"),
                ChecklistItem("o2", "Slot werkt goed"),
                ChecklistItem("o3", "Standaard werkt (indien aanwezig)"),
                ChecklistItem("o4", "Bagagedrager zit vast (indien aanwezig)"),
                ChecklistItem("o5", "Fiets is schoon en vrij van roest")
            )
        )
    )

    private fun setupRecyclerView() {
        val categories = prefsHelper.loadCategories()?.toMutableList() ?: defaultCategories()

        adapter = ChecklistAdapter(categories) { total, checked ->
            updateProgress(total, checked)
            prefsHelper.saveCategories(adapter.getCategories())
            updateLastCheckDate()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun updateProgress(total: Int = -1, checked: Int = -1) {
        val t: Int
        val c: Int

        if (total == -1) {
            val cats = adapter.getCategories()
            t = cats.sumOf { it.items.size }
            c = cats.sumOf { cat -> cat.items.count { it.isChecked } }
        } else {
            t = total
            c = checked
        }

        binding.progressBar.max = t
        binding.progressBar.progress = c
        binding.tvProgress.text = "$c van $t punten gecontroleerd"

        val percentage = if (t > 0) (c * 100 / t) else 0
        binding.tvPercentage.text = "$percentage%"

        // Change percentage color based on completion
        val color = when {
            percentage == 100 -> getColor(R.color.progressComplete)
            percentage >= 50  -> getColor(R.color.progressPartial)
            else              -> getColor(R.color.progressLow)
        }
        binding.tvPercentage.setTextColor(color)
    }

    private fun updateLastCheckDate() {
        binding.tvLastCheck.text = "Laatste controle: ${prefsHelper.getLastCheckDate()}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_reset -> {
            showResetDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Checklist resetten")
            .setMessage("Weet je zeker dat je alle vinkjes wilt verwijderen?")
            .setPositiveButton("Reset") { _, _ ->
                prefsHelper.clearSaved()
                setupRecyclerView()
                updateProgress()
                updateLastCheckDate()
            }
            .setNegativeButton("Annuleren", null)
            .show()
    }
}
