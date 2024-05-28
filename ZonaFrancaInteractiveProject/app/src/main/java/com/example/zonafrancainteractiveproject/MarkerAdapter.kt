package com.example.zonafrancainteractiveproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class MarkerAdapter(private val context: Context, private var markerList: ArrayList<MarkerData>) :
    RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>(), android.widget.Filterable {

    private var markerListFull: ArrayList<MarkerData> = ArrayList(markerList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_marker, parent, false)
        return MarkerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val marker = markerList[position]
        holder.titleTextView.text = marker.title
        holder.descriptionTextView.text = marker.description
    }

    override fun getItemCount(): Int {
        return markerList.size
    }

    override fun getFilter(): android.widget.Filter {
        return markerFilter
    }

    private val markerFilter = object : android.widget.Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<MarkerData>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(markerListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (item in markerListFull) {
                    if (item.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        item.description.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            markerList.clear()
            markerList.addAll(results?.values as List<MarkerData>)
            notifyDataSetChanged()
        }
    }

    class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.markerTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.markerDescription)
    }
}