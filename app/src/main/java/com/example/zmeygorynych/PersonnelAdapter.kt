package com.example.zmeygorynych

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PersonnelAdapter(
    private val onEditClick: (Personnel) -> Unit,
    private val onDeleteClick: (Personnel) -> Unit
) : ListAdapter<Personnel, PersonnelAdapter.PersonnelViewHolder>(PersonnelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonnelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personnel, parent, false)
        return PersonnelViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonnelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonnelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)

        fun bind(personnel: Personnel) {
            tvFullName.text = formatShortName(personnel)
            tvPosition.text = personnel.displayPosition

            // Клик по всей карточке для редактирования
            itemView.setOnClickListener { onEditClick(personnel) }
        }
    }

    private class PersonnelDiffCallback : DiffUtil.ItemCallback<Personnel>() {
        override fun areItemsTheSame(oldItem: Personnel, newItem: Personnel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Personnel, newItem: Personnel): Boolean {
            return oldItem == newItem
        }
    }

    private fun formatShortName(personnel: Personnel): String {
        val lastName = personnel.lastName
        val firstInitial = personnel.firstName.firstOrNull()?.uppercaseChar()?.let { "$it." } ?: ""
        val middleInitial = personnel.middleName.firstOrNull()?.uppercaseChar()?.let { "$it." } ?: ""
        val initials = (firstInitial + middleInitial).trim()
        return if (initials.isNotEmpty()) "$lastName $initials" else lastName
    }
}

