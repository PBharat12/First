package com.example.supervisor.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.adapter.SupervisorAdapter
import com.example.supervisor.data.Supervisor
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class SupervisorListActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var noResultsView: TextView

    private val db = FirebaseFirestore.getInstance()

    private lateinit var supervisorAdapter: SupervisorAdapter
    private val supervisors = mutableListOf<Supervisor>()

    // Custom auth variables
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_list)

        initializeUserData()
        initViews()
        setupRecyclerView()
        setupDrawerNavigation()
        loadSupervisors()
    }

    private fun initializeUserData() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Try different possible keys for user data
        currentUserId = sharedPref.getString("user_id", null)
            ?: sharedPref.getString("student_id", null)
            ?: sharedPref.getString("userId", null)

        currentUserName = sharedPref.getString("user_name", null)
            ?: sharedPref.getString("student_name", null)
            ?: sharedPref.getString("name", null)

        currentUserEmail = sharedPref.getString("user_email", null)
            ?: sharedPref.getString("student_email", null)
            ?: sharedPref.getString("email", null)

        Log.d("SupervisorList", "Current user data loaded: ID=$currentUserId, Name=$currentUserName, Email=$currentUserEmail")

        // If still no user ID found, generate a temporary one or use a fallback
        if (currentUserId == null) {
            Log.w("SupervisorList", "No user ID found in SharedPreferences, using fallback")
            currentUserId = "temp_user_" + System.currentTimeMillis()
        }
    }

    private fun initViews() {
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        recyclerView = findViewById(R.id.recyclerViewSupervisors)
        loadingView = findViewById(R.id.loadingView)
        noResultsView = findViewById(R.id.noResultsView)

        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupRecyclerView() {
        supervisorAdapter = SupervisorAdapter(supervisors) { supervisor ->
            // Navigate to request supervisor activity
            val intent = Intent(this, RequestSupervisorActivity::class.java)
            intent.putExtra("supervisor_id", supervisor.id)
            intent.putExtra("supervisor_name", supervisor.name)
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SupervisorListActivity)
            adapter = supervisorAdapter
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Navigate to profile
                    // val intent = Intent(this, ProfileActivity::class.java)
                    // startActivity(intent)
                }
                R.id.nav_my_requests -> {
                    // Navigate to my requests
                    // val intent = Intent(this, MyRequestsActivity::class.java)
                    // startActivity(intent)
                }
                R.id.nav_supervisors -> {
                    // Already in supervisor list, just close drawer
                    drawerLayout.closeDrawers()
                }
                R.id.nav_logout -> {
                    handleLogout()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadSupervisors() {
        Log.d("SupervisorList", "Loading supervisors from Firestore")
        showLoading(true)

        db.collection("supervisors")
            .get()
            .addOnSuccessListener { documents ->
                showLoading(false)
                supervisors.clear()

                Log.d("SupervisorList", "Query successful! Found ${documents.size()} supervisors")

                if (documents.isEmpty) {
                    Log.w("SupervisorList", "No supervisors found in database")
                    updateUI()
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    try {
                        val supervisor = Supervisor(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            email = document.getString("email") ?: "",
                            department = document.getString("department") ?: "",
                            specialization = document.getString("specialization") ?: "",
                            maxRequests = document.getLong("maxRequests") ?: 20L,
                            currentRequests = document.getLong("requestCount") ?: 0L,
                            bio = document.getString("bio") ?: "",
                            phone = document.getString("phone") ?: "",
                            isAvailable = document.getBoolean("isAvailable") ?: true,
                            profileImageUrl = document.getString("profileImageUrl") ?: ""
                        )
                        supervisors.add(supervisor)
                        Log.d("SupervisorList", "Added: ${supervisor.name} - ${supervisor.department}")
                    } catch (e: Exception) {
                        Log.e("SupervisorList", "Parse error: ${e.message}", e)
                    }
                }

                // Sort by availability and then by name
                supervisors.sortWith(compareBy<Supervisor> { !it.isAvailable || it.currentRequests >= it.maxRequests }
                    .thenBy { it.name })

                updateUI()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e("SupervisorList", "Query failed: ${exception.message}", exception)
                Toast.makeText(
                    this,
                    "Failed to load supervisors: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                updateUI()
            }
    }

    private fun updateUI() {
        if (supervisors.isEmpty()) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noResultsView.visibility = View.GONE
            supervisorAdapter.notifyDataSetChanged()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.GONE
        }
    }

    private fun handleLogout() {
        // Clear user preferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        Log.d("SupervisorList", "User logged out successfully")

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Helper method to check if user is authenticated
    private fun isUserAuthenticated(): Boolean {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        val role = sharedPref.getString("role", null)
        return userId != null && role == "student"
    }

    override fun onResume() {
        super.onResume()

        // Check authentication status when resuming
        if (!isUserAuthenticated()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Refresh user data in case it was updated
        initializeUserData()

        // Refresh the supervisors when returning to this activity
        loadSupervisors()
    }
}