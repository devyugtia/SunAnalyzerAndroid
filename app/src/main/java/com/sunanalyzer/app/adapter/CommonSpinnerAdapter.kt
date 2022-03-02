package com.sunanalyzer.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sunanalyzer.app.R
import com.sunanalyzer.app.clickListener.RecyclerRowClick
import com.sunanalyzer.app.databinding.RowCommonSpinnerBinding
import com.sunanalyzer.app.model.SpinnerOptionModel
import java.util.*


class CommonSpinnerAdapter(var items: ArrayList<SpinnerOptionModel>, val click: RecyclerRowClick) :
    RecyclerView.Adapter<CommonSpinnerAdapter.ViewHolder>() {

    var originalitemslist: ArrayList<SpinnerOptionModel> = items
    var selectedItem = SpinnerOptionModel()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: RowCommonSpinnerBinding =
            DataBindingUtil.inflate(inflater, R.layout.row_common_spinner, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.binding.llMainImage.setOnClickListener {
            selectedItem = items[position]
            for (pos in items.indices) {
                items[pos].isSelected = pos == position
            }
            notifyDataSetChanged()
            click.rowClick(position, 1)
        }
    }

    @JvmName("getSelectedItem1")
    fun getSelectedItem(): SpinnerOptionModel {
        return selectedItem
    }

    class ViewHolder(val binding: RowCommonSpinnerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SpinnerOptionModel) {
            binding.model = data
            binding.executePendingBindings()
        }
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                items = filterResults.values as ArrayList<SpinnerOptionModel>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()
                val filterResults = Filter.FilterResults()
                if (queryString.isNullOrEmpty()) {
                    items.clear()
                    items.addAll(originalitemslist)
                    filterResults.values = items
                } else {
                    if (items.isEmpty()) items = originalitemslist
                    filterResults.values = items.filter {
                        it.name.lowercase(Locale.getDefault())
                            .contains(queryString) || it.value.lowercase(Locale.getDefault())
                            .contains(queryString)
                    }
                }
                return filterResults
            }
        }
    }
}