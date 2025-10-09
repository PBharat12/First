package com.example.supervisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.data.ProjectReport

class ProjectReportAdapter(
    private val projectList: List<ProjectReport>
) : RecyclerView.Adapter<ProjectReportAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvProjectTitle: TextView = itemView.findViewById(R.id.tvProjectTitle)
        val tvTeamSize: TextView = itemView.findViewById(R.id.tvTeamSize)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        val expandableDetails: LinearLayout = itemView.findViewById(R.id.expandableDetails)
        val divider: View = itemView.findViewById(R.id.divider)
        val recyclerViewTeamMembers: RecyclerView = itemView.findViewById(R.id.recyclerViewTeamMembers)
        val mainContent: androidx.constraintlayout.widget.ConstraintLayout = itemView.findViewById(R.id.mainContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_report, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projectList[position]

        // Set basic info
        holder.tvStatus.text = when (project.status.lowercase()) {
            "accepted" -> "✓ Accepted"
            "pending" -> "⏳ Pending"
            "rejected" -> "✗ Rejected"
            else -> project.status
        }

        holder.tvDate.text = project.formattedDate
        holder.tvTime.text = project.formattedTime
        holder.tvProjectTitle.text = project.projectTitle
        holder.tvTeamSize.text = "${project.teamSize} Member${if (project.teamSize != 1) "s" else ""}"
        holder.tvDescription.text = project.description

        // Setup team members RecyclerView
        if (project.teamMembers.isNotEmpty()) {
            val teamAdapter = TeamMemberAdapter(project.teamMembers)
            holder.recyclerViewTeamMembers.apply {
                layoutManager = LinearLayoutManager(holder.itemView.context)
                adapter = teamAdapter
                isNestedScrollingEnabled = false
            }
        }

        // Initially collapsed
        holder.expandableDetails.visibility = View.GONE
        holder.divider.visibility = View.GONE
        var isExpanded = false

        // Handle expand/collapse
        holder.mainContent.setOnClickListener {
            isExpanded = !isExpanded

            if (isExpanded) {
                holder.expandableDetails.visibility = View.VISIBLE
                holder.divider.visibility = View.VISIBLE
                holder.expandIcon.animate().rotation(180f).setDuration(200).start()
            } else {
                holder.expandableDetails.visibility = View.GONE
                holder.divider.visibility = View.GONE
                holder.expandIcon.animate().rotation(0f).setDuration(200).start()
            }
        }

        holder.expandIcon.setOnClickListener {
            holder.mainContent.performClick()
        }
    }

    override fun getItemCount(): Int = projectList.size
}
