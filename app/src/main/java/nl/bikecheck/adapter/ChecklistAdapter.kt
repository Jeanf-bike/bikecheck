package nl.bikecheck.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.bikecheck.databinding.ItemChecklistBinding
import nl.bikecheck.model.ChecklistItem

class ChecklistAdapter(
    private val items: MutableList<ChecklistItem>,
    private val onItemChanged: () -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChecklistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isChecked
            tvDescription.text = item.description
            applyStrikeThrough(item.isChecked)

            // Weerbadge (☀️ UV of 🥶 Koud)
            if (item.weatherTag.isNotEmpty()) {
                tvWeatherTag.text = item.weatherTag
                tvWeatherTag.visibility = View.VISIBLE
            } else {
                tvWeatherTag.visibility = View.GONE
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                applyStrikeThrough(isChecked)
                onItemChanged()
            }
        }
    }

    private fun ItemChecklistBinding.applyStrikeThrough(strike: Boolean) {
        tvDescription.paintFlags = if (strike) {
            tvDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        tvDescription.alpha = if (strike) 0.45f else 1.0f
    }

    override fun getItemCount(): Int = items.size

    fun getItems(): MutableList<ChecklistItem> = items

    /**
     * Vervangt alle weather_* items door de nieuwe set op basis van actueel weer.
     * Onthoudt de checked-status van items die blijven.
     */
    fun updateConditionalItems(newConditional: List<ChecklistItem>) {
        val oldConditional = items.filter { it.id.startsWith("weather_") }
        items.removeAll { it.id.startsWith("weather_") }

        val merged = newConditional.map { new ->
            val prev = oldConditional.find { it.id == new.id }
            if (prev != null) new.copy(isChecked = prev.isChecked) else new
        }

        items.addAll(merged)
        notifyDataSetChanged()
    }
}
