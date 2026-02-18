package nl.bikecheck.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.bikecheck.R
import nl.bikecheck.model.ChecklistCategory
import nl.bikecheck.model.ChecklistItem

sealed class ListItem {
    data class Header(val name: String, val icon: String) : ListItem()
    data class Item(val item: ChecklistItem) : ListItem()
}

class ChecklistAdapter(
    private val categories: MutableList<ChecklistCategory>,
    private val onItemChanged: (total: Int, checked: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private val flatList: MutableList<ListItem> = mutableListOf()

    init {
        buildFlatList()
    }

    private fun buildFlatList() {
        flatList.clear()
        for (category in categories) {
            flatList.add(ListItem.Header(category.name, category.icon))
            for (item in category.items) {
                flatList.add(ListItem.Item(item))
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (flatList[position]) {
        is ListItem.Header -> VIEW_TYPE_HEADER
        is ListItem.Item   -> VIEW_TYPE_ITEM
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvCategoryIcon)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_category_header, parent, false)
            )
            else -> ItemViewHolder(
                inflater.inflate(R.layout.item_checklist, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val listItem = flatList[position]) {
            is ListItem.Header -> {
                (holder as HeaderViewHolder).apply {
                    tvIcon.text = listItem.icon
                    tvName.text = listItem.name
                }
            }
            is ListItem.Item -> {
                val item = listItem.item
                (holder as ItemViewHolder).apply {
                    // Remove listener before setting state to avoid triggering callback
                    checkBox.setOnCheckedChangeListener(null)
                    checkBox.isChecked = item.isChecked
                    tvDescription.text = item.description
                    applyStrikeThrough(tvDescription, item.isChecked)

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        item.isChecked = isChecked
                        applyStrikeThrough(tvDescription, isChecked)
                        notifyProgress()
                    }
                }
            }
        }
    }

    private fun applyStrikeThrough(textView: TextView, strike: Boolean) {
        textView.paintFlags = if (strike) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        textView.alpha = if (strike) 0.5f else 1.0f
    }

    override fun getItemCount(): Int = flatList.size

    fun getCategories(): MutableList<ChecklistCategory> = categories

    private fun notifyProgress() {
        val total = categories.sumOf { it.items.size }
        val checked = categories.sumOf { cat -> cat.items.count { it.isChecked } }
        onItemChanged(total, checked)
    }
}
