package com.example.supervisor.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.data.Supervisor
import com.example.supervisor.R

class SupervisorAdapter(
    private val supervisors: List<Supervisor>,
    private val onItemClick: (Supervisor) -> Unit
) : RecyclerView.Adapter<SupervisorAdapter.SupervisorViewHolder>() {

    inner class SupervisorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.supervisorCard)
        val tvSupervisorName: TextView = itemView.findViewById(R.id.tvSupervisorName)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val tvSpecialization: TextView = itemView.findViewById(R.id.tvSpecialization)
        val tvAvailability: TextView = itemView.findViewById(R.id.tvAvailability)
        val tvCapacity: TextView = itemView.findViewById(R.id.tvCapacity)
        val tvBioPreview: TextView = itemView.findViewById(R.id.tvBioPreview)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvProfileInitial: TextView = itemView.findViewById(R.id.tvProfileInitial)

        init {
            cardView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(supervisors[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupervisorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_supervisor, parent, false)
        return SupervisorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupervisorViewHolder, position: Int) {
        val supervisor = supervisors[position]

        // Set basic info
        holder.tvSupervisorName.text = supervisor.name
        holder.tvDepartment.text = supervisor.department
        holder.tvSpecialization.text = supervisor.specialization
        holder.tvEmail.text = supervisor.email
        holder.tvPhone.text = supervisor.phone

        // Set profile initial (first two characters of name)
        val initials = supervisor.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
        holder.tvProfileInitial.text = initials.ifEmpty { "DR" }

        // Set capacity info
        holder.tvCapacity.text = "${supervisor.currentRequests}/${supervisor.maxRequests}"

        // Set bio preview (first 100 characters)
        val bioPreview = if (supervisor.bio.length > 100) {
            supervisor.bio.substring(0, 100) + "..."
        } else {
            supervisor.bio
        }
        holder.tvBioPreview.text = bioPreview.ifEmpty { "No bio available" }

        // Set availability status
        if (supervisor.isAvailable && supervisor.currentRequests < supervisor.maxRequests) {
            holder.tvAvailability.text = "Available"
            holder.tvAvailability.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            holder.tvAvailability.setTextColor(Color.WHITE)
        } else if (supervisor.currentRequests >= supervisor.maxRequests) {
            holder.tvAvailability.text = "Full"
            holder.tvAvailability.setBackgroundColor(Color.parseColor("#F44336")) // Red
            holder.tvAvailability.setTextColor(Color.WHITE)
        } else {
            holder.tvAvailability.text = "Unavailable"
            holder.tvAvailability.setBackgroundColor(Color.parseColor("#FF9800")) // Orange
            holder.tvAvailability.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = supervisors.size
}