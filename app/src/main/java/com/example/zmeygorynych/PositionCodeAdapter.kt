package com.example.zmeygorynych

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PositionCodeAdapter(
    private val onEditClick: (PositionCode) -> Unit,
    private val onDeleteClick: (PositionCode) -> Unit
) : ListAdapter<PositionCode, PositionCodeAdapter.PositionCodeViewHolder>(PositionCodeDiffCallback()) {

    inner class PositionCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPositionCode: TextView = itemView.findViewById(R.id.tvPositionCode)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditPositionCode)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeletePositionCode)

        fun bind(positionCode: PositionCode) {
            tvPositionCode.text = positionCode.displayName

            btnEdit.setOnClickListener { onEditClick(positionCode) }
            btnDelete.setOnClickListener { onDeleteClick(positionCode) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionCodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_position_code, parent, false)
        return PositionCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PositionCodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PositionCodeDiffCallback : DiffUtil.ItemCallback<PositionCode>() {
    override fun areItemsTheSame(oldItem: PositionCode, newItem: PositionCode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PositionCode, newItem: PositionCode): Boolean {
        return oldItem == newItem
    }
}
