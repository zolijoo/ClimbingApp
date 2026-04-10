package com.example.climbingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.climbingapp.R
import com.example.climbingapp.models.RecordItem

class RecordAdapter (private val items: MutableList<RecordItem>) :
    RecyclerView.Adapter<RecordAdapter.RowViewHolder>() {

    inner class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val difficulty: TextView = itemView.findViewById(R.id.difficulty)
        val value: TextView = itemView.findViewById(R.id.value)
        val attempts: TextView = itemView.findViewById(R.id.attempts)
        val sent: TextView = itemView.findViewById(R.id.sent)
        val time: TextView = itemView.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_activity, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val item = items[position]
        holder.difficulty.text = item.difficulty
        holder.value.text = item.value.toString()
        holder.attempts.text = item.attempts.toString()
        holder.sent.text = item.sent.toString()
        holder.time.text = item.time
    }

    override fun getItemCount(): Int = items.size

    fun addRecord(record: RecordItem) {
        items.add(record)
        notifyItemInserted(items.size - 1)
    }
}
