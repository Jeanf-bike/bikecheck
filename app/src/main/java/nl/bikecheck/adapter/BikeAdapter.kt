package nl.bikecheck.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.bikecheck.databinding.ItemBikeCardBinding
import nl.bikecheck.model.Bike

class BikeAdapter(
    private val bikes: List<Bike>,
    private val onClick: (Bike) -> Unit
) : RecyclerView.Adapter<BikeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBikeCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBikeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bike = bikes[position]
        holder.binding.apply {
            tvBikeName.text = bike.name
            tvBikeSubtitle.text = bike.subtitle
            if (bike.subtype.isNotEmpty()) {
                tvBikeSubtype.text = bike.subtype
                tvBikeSubtype.visibility = android.view.View.VISIBLE
            } else {
                tvBikeSubtype.visibility = android.view.View.GONE
            }
            ivBikePlaceholder.setBackgroundResource(bike.imageRes)
            root.setOnClickListener { onClick(bike) }
        }
    }

    override fun getItemCount(): Int = bikes.size
}
