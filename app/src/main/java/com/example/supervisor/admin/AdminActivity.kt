package com.example.supervisor.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.adapter.SupervisorAllocationAdapter
import com.example.supervisor.data.SupervisorAllocation
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var noResultsView: TextView
    private lateinit var searchView: SearchView
    private lateinit var tvTotalSupervisors: TextView
    private lateinit var tvTotalProjects: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var supervisorAdapter: SupervisorAllocationAdapter
    private val supervisorList = mutableListOf<SupervisorAllocation>()
    private val allSupervisors = mutableListOf<SupervisorAllocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        setupRecyclerView()
        setupDrawerNavigation()
        loadAdminInfo()
        loadSupervisorData()
    }

    private fun initViews() {
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        recyclerView = findViewById(R.id.recyclerViewSupervisorList)
        loadingView = findViewById(R.id.loadingView)
        noResultsView = findViewById(R.id.noResultsView)
        searchView = findViewById(R.id.searchView)
        tvTotalSupervisors = findViewById(R.id.tvTotalSupervisors)
        tvTotalProjects = findViewById(R.id.tvTotalProjects)

        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSupervisors(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        supervisorAdapter = SupervisorAllocationAdapter(supervisorList) { supervisor ->
            // Navigate to supervisor's allocated projects
            val intent = Intent(this, SupervisorProjectsActivity::class.java).apply {
                putExtra("supervisorId", supervisor.id)
                putExtra("supervisorName", supervisor.name)
                putExtra("supervisorEmail", supervisor.email)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = supervisorAdapter
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Navigate to profile
                }
                R.id.nav_add_supervisor -> {
                    val intent = Intent(this, AddSupervisorActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    handleLogout()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadAdminInfo() {
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.user_name_text_view)


        val adminName = intent.getStringExtra("adminName") ?: "Admin"
        tvUserName.text = "Welcome, $adminName"
    }

    private fun loadSupervisorData() {
        showLoading(true)

        // Load supervisors from Firestore
        db.collection("supervisors")
            .get()
            .addOnSuccessListener { documents ->
                allSupervisors.clear()
                supervisorList.clear()

                for (document in documents) {
                    try {
                        val supervisor = SupervisorAllocation(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            email = document.getString("email") ?: "",
                            mobile = document.getString("mobile") ?: "",
                            department = document.getString("department") ?: "Computer Science",
                            requestCount = (document.getLong("requestCount") ?: 0L).toInt(),
                            maxRequests = (document.getLong("maxRequests") ?: 20L).toInt(),
                            isActive = (document.getLong("requestCount") ?: 0L) < (document.getLong("maxRequests") ?: 20L),
                            lastActive = document.getLong("lastActive") ?: System.currentTimeMillis(),
                            specialization = document.getString("specialization") ?: "",
                            experience = document.getString("experience") ?: ""
                        )
                        allSupervisors.add(supervisor)
                    } catch (e: Exception) {
                        Log.e("AdminActivity", "Error parsing supervisor: ${e.message}")
                    }
                }

                supervisorList.addAll(allSupervisors)
                updateUI()
                updateStatistics()
                showLoading(false)
            }
            .addOnFailureListener { exception ->
                Log.e("AdminActivity", "Error loading supervisors: ${exception.message}")
                Toast.makeText(this, "Failed to load supervisors: ${exception.message}", Toast.LENGTH_LONG).show()
                showLoading(false)
            }
    }

    private fun filterSupervisors(query: String) {
        if (query.isEmpty()) {
            supervisorAdapter.updateList(allSupervisors)
        } else {
            val filteredList = allSupervisors.filter { supervisor ->
                supervisor.name.contains(query, ignoreCase = true) ||
                        supervisor.email.contains(query, ignoreCase = true) ||
                        supervisor.department.contains(query, ignoreCase = true)
            }
            supervisorAdapter.filterList(filteredList)
        }
        updateUI()
    }

    private fun updateUI() {
        if (supervisorList.isEmpty()) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noResultsView.visibility = View.GONE
            supervisorAdapter.notifyDataSetChanged()
        }
    }

    private fun updateStatistics() {
        tvTotalSupervisors.text = allSupervisors.size.toString()

        val totalProjects = allSupervisors.sumOf { it.requestCount }
        tvTotalProjects.text = totalProjects.toString()
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.GONE
        }
    }

    private fun handleLogout() {
        auth.signOut()
        val intent = Intent(this, com.example.supervisor.AuthUser.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        loadSupervisorData()
    }
}
