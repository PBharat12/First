package com.example.supervisor.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.adapter.ProjectReportAdapter
import com.example.supervisor.data.ProjectReport
import com.example.supervisor.data.SupervisorAllocation
import com.example.supervisor.data.TeamMember
import com.google.firebase.firestore.FirebaseFirestore

class SupervisorProjectsActivity : AppCompatActivity() {

    private lateinit var tvInitialsLarge: TextView
    private lateinit var tvSupervisorNameLarge: TextView
    private lateinit var tvEmailLarge: TextView
    private lateinit var tvProjectCount: TextView
    private lateinit var recyclerViewProjects: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var emptyStateView: LinearLayout
    private lateinit var backButton: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val projectList = mutableListOf<ProjectReport>()
    private lateinit var projectAdapter: ProjectReportAdapter

    private var supervisorId: String = ""
    private var supervisorName: String = ""
    private var supervisorEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_projects)

        initViews()
        getSupervisorData()
        setupRecyclerView()
        loadProjects()
    }

    private fun initViews() {
        tvInitialsLarge = findViewById(R.id.tvInitialsLarge)
        tvSupervisorNameLarge = findViewById(R.id.tvSupervisorNameLarge)
        tvEmailLarge = findViewById(R.id.tvEmailLarge)
        tvProjectCount = findViewById(R.id.tvProjectCount)
        recyclerViewProjects = findViewById(R.id.recyclerViewProjects)
        loadingView = findViewById(R.id.loadingView)
        emptyStateView = findViewById(R.id.emptyStateView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getSupervisorData() {
        // Get supervisor data from intent
        supervisorId = intent.getStringExtra("supervisorId") ?: ""
        supervisorName = intent.getStringExtra("supervisorName") ?: ""
        supervisorEmail = intent.getStringExtra("supervisorEmail") ?: ""

        // Set supervisor info
        tvSupervisorNameLarge.text = supervisorName
        tvEmailLarge.text = supervisorEmail

        // Get initials
        val initials = supervisorName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        tvInitialsLarge.text = initials
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectReportAdapter(projectList)
        recyclerViewProjects.apply {
            layoutManager = LinearLayoutManager(this@SupervisorProjectsActivity)
            adapter = projectAdapter
        }
    }

    private fun loadProjects() {
        showLoading(true)

        // Query projects where supervisorId matches and status is "accepted"
        db.collection("projects")
            .whereEqualTo("supervisorId", supervisorId)
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { documents ->
                projectList.clear()

                for (document in documents) {
                    try {
                        // Parse team members
                        val teamMembersList = mutableListOf<TeamMember>()
                        val teamMembersData = document.get("teamMembers") as? List<HashMap<String, Any>>

                        teamMembersData?.forEach { memberData ->
                            val teamMember = TeamMember(
                                name = memberData["name"] as? String ?: "",
                                rollNo = memberData["rollNo"] as? String ?: "",
                                email = memberData["email"] as? String ?: "",
                                mobile = memberData["mobile"] as? String ?: "",
                                role = memberData["role"] as? String ?: "Member"
                            )
                            teamMembersList.add(teamMember)
                        }

                        val project = ProjectReport(
                            id = document.id,
                            projectTitle = document.getString("projectTitle") ?: "",
                            description = document.getString("description") ?: "",
                            status = document.getString("status") ?: "accepted",
                            submittedDate = document.getLong("submittedDate") ?: System.currentTimeMillis(),
                            acceptedDate = document.getLong("acceptedDate") ?: System.currentTimeMillis(),
                            teamMembers = teamMembersList,
                            supervisorId = document.getString("supervisorId") ?: "",
                            supervisorName = document.getString("supervisorName") ?: "",
                            category = document.getString("category") ?: "",
                            tags = (document.get("tags") as? List<String>) ?: emptyList()
                        )
                        projectList.add(project)
                    } catch (e: Exception) {
                        Log.e("SupervisorProjects", "Error parsing project: ${e.message}")
                    }
                }

                updateUI()
                showLoading(false)
            }
            .addOnFailureListener { exception ->
                Log.e("SupervisorProjects", "Error loading projects: ${exception.message}")
                Toast.makeText(
                    this,
                    "Failed to load projects: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                showLoading(false)
            }
    }

    private fun updateUI() {
        if (projectList.isEmpty()) {
            recyclerViewProjects.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
            tvProjectCount.text = "📊 0 Accepted Projects"
        } else {
            recyclerViewProjects.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            tvProjectCount.text = "📊 ${projectList.size} Accepted Project${if (projectList.size != 1) "s" else ""}"
            projectAdapter.notifyDataSetChanged()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            recyclerViewProjects.visibility = View.GONE
            emptyStateView.visibility = View.GONE
        }
    }
}
