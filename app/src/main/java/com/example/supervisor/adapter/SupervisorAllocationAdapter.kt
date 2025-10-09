package com.example.supervisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.data.SupervisorAllocation
import com.google.android.material.card.MaterialCardView

class SupervisorAllocationAdapter(
    private val supervisorList: MutableList<SupervisorAllocation>,
    private val onSupervisorClick: (SupervisorAllocation) -> Unit
) : RecyclerView.Adapter<SupervisorAllocationAdapter.SupervisorViewHolder>() {

    class SupervisorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitials: TextView = itemView.findViewById(R.id.tvInitials)
        val tvSupervisorName: TextView = itemView.findViewById(R.id.tvSupervisorName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvAvailability: TextView = itemView.findViewById(R.id.tvAvailability)
        val tvCurrentProjects: TextView = itemView.findViewById(R.id.tvCurrentProjects)
        val tvMaxCapacity: TextView = itemView.findViewById(R.id.tvMaxCapacity)
        val tvAvailableSlots: TextView = itemView.findViewById(R.id.tvAvailableSlots)
        val availabilityIndicator: MaterialCardView = itemView.findViewById(R.id.availabilityIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupervisorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supervisor_allocation, parent, false)
        return SupervisorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupervisorViewHolder, position: Int) {
        val supervisor = supervisorList[position]

        // Set basic info
        holder.tvInitials.text = supervisor.initials
        holder.tvSupervisorName.text = supervisor.name
        holder.tvEmail.text = supervisor.email

        // Set project statistics
        holder.tvCurrentProjects.text = supervisor.requestCount.toString()
        holder.tvMaxCapacity.text = supervisor.maxRequests.toString()
        holder.tvAvailableSlots.text = supervisor.availableSlots.toString()

        // Set availability status
        if (supervisor.isAvailable() && supervisor.availableSlots > 0) {
            holder.tvAvailability.text = "Available"
            holder.availabilityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )
        } else if (supervisor.requestCount >= supervisor.maxRequests) {
            holder.tvAvailability.text = "Full"
            holder.availabilityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.red)
            )
        } else {
            holder.tvAvailability.text = "Busy"
            holder.availabilityIndicator.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.orange)
            )
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onSupervisorClick(supervisor)
        }
    }

    override fun getItemCount(): Int = supervisorList.size

    fun updateList(newList: List<SupervisorAllocation>) {
        supervisorList.clear()
        supervisorList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filterList(filteredList: List<SupervisorAllocation>) {
        supervisorList.clear()
        supervisorList.addAll(filteredList)
        notifyDataSetChanged()
    }
}
