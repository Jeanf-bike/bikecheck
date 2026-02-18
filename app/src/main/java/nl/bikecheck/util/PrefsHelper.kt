package nl.bikecheck.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.bikecheck.model.ChecklistItem

class PrefsHelper(context: Context) {

    private val prefs = context.getSharedPreferences("bikecheck_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveChecklist(bikeName: String, items: List<ChecklistItem>) {
        prefs.edit().putString("checklist_$bikeName", gson.toJson(items)).apply()
    }

    fun loadChecklist(bikeName: String): MutableList<ChecklistItem>? {
        val json = prefs.getString("checklist_$bikeName", null) ?: return null
        return try {
            val type = object : TypeToken<MutableList<ChecklistItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun clearChecklist(bikeName: String) {
        prefs.edit().remove("checklist_$bikeName").apply()
    }
}
