package nl.bikecheck.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.bikecheck.model.ChecklistCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrefsHelper(context: Context) {

    private val prefs = context.getSharedPreferences("bikecheck_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCategories(categories: List<ChecklistCategory>) {
        prefs.edit()
            .putString("categories", gson.toJson(categories))
            .putLong("last_check", System.currentTimeMillis())
            .apply()
    }

    fun loadCategories(): List<ChecklistCategory>? {
        val json = prefs.getString("categories", null) ?: return null
        return try {
            val type = object : TypeToken<List<ChecklistCategory>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastCheckDate(): String {
        val time = prefs.getLong("last_check", 0)
        if (time == 0L) return "Nog niet gecontroleerd"
        val sdf = SimpleDateFormat("dd MMMM yyyy 'om' HH:mm", Locale("nl", "NL"))
        return sdf.format(Date(time))
    }

    fun clearSaved() {
        prefs.edit().clear().apply()
    }
}
