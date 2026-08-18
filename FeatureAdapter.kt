package com.nyxforge.gamebooster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nyxforge.gamebooster.databinding.ItemFeatureBinding

class FeatureAdapter(
    private val items: List<BoosterFeature>,
    private val onToggle: (BoosterFeature, Boolean) -> Unit
) : RecyclerView.Adapter<FeatureAdapter.VH>() {

    inner class VH(val binding: ItemFeatureBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.txtTitle.text = "${item.id}. ${item.title}"
        holder.binding.txtDesc.text = item.description
        holder.binding.switchFeature.setOnCheckedChangeListener(null)
        holder.binding.switchFeature.isChecked = item.enabled
        holder.binding.switchFeature.setOnCheckedChangeListener { _, isChecked ->
            onToggle(item, isChecked)
        }
    }

    override fun getItemCount() = items.size
}
