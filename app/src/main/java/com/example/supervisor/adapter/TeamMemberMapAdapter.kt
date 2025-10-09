package com.example.supervisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.google.android.material.card.MaterialCardView

/**
 * Adapter for team members that works with Map<String, String> data
 * This is for backward compatibility with existing code
 */
class TeamMemberMapAdapter(
    private val teamMembers: List<Map<String, String>>
) : RecyclerView.Adapter<TeamMemberMapAdapter.TeamMemberViewHolder>() {

    class TeamMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMemberInitial: TextView = itemView.findViewById(R.id.tvMemberInitial)
        val tvMemberName: TextView = itemView.findViewById(R.id.tvMemberName)
        val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)
        val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val roleBadge: MaterialCardView = itemView.findViewById(R.id.roleBadge)
        val memberAvatar: MaterialCardView = itemView.findViewById(R.id.memberAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return TeamMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        val memberMap = teamMembers[position]
        
        // Get values from map with fallback keys
        val name = memberMap["name"] ?: memberMap["fullName"] ?: ""
        val rollNo = memberMap["rollNo"] ?: memberMap["roll"] ?: ""
        val email = memberMap["email"] ?: ""
        val mobile = memberMap["mobile"] ?: memberMap["contact"] ?: ""
        val role = memberMap["role"] ?: "Member"

        // Set initial
        val initial = name.firstOrNull()?.uppercase() ?: "?"
        holder.tvMemberInitial.text = initial

        // Set member info
        holder.tvMemberName.text = name
        holder.tvRollNo.text = "Roll: $rollNo"
        holder.tvEmail.text = email
        holder.tvMobile.text = mobile

        // Set role with different styling
        holder.tvRole.text = role
        if (role.equals("Lead", ignoreCase = true)) {
            holder.roleBadge.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.black)
            )
            holder.tvRole.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.memberAvatar.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.black)
            )
        } else {
            holder.roleBadge.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            )
            holder.roleBadge.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.gray)
            holder.roleBadge.strokeWidth = 2
            holder.tvRole.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.gray)
            )
            holder.memberAvatar.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.gray)
            )
        }
    }

    override fun getItemCount(): Int = teamMembers.size
}
