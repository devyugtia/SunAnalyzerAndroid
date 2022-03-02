package com.sunanalyzer.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sunanalyzer.app.R
import com.sunanalyzer.app.clickListener.RecyclerRowClick
import com.sunanalyzer.app.databinding.RowMapOptionItemBinding
import com.sunanalyzer.app.model.SpinnerOptionModel


class MapOptionAdapter(
    private val items: ArrayList<SpinnerOptionModel>,
    private val click: RecyclerRowClick
) : RecyclerView.Adapter<MapOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: RowMapOptionItemBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.row_map_option_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.binding.root.setOnClickListener {
            click.rowClick(position, items[position].id)
        }
    }

    class ViewHolder(val binding: RowMapOptionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SpinnerOptionModel) {
            binding.model = data
            binding.executePendingBindings()
        }
    }
}