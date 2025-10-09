package com.example.supervisor.supervisor

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
import com.example.supervisor.data.ProjectRequest
import com.example.supervisor.adapter.ProjectRequestsAdapter
import com.example.supervisor.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import java.io.ByteArrayOutputStream


class SupervisorRequestsActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingView: ProgressBar
    private lateinit var noResultsView: TextView

    private val db = FirebaseFirestore.getInstance()

    private lateinit var requestsAdapter: ProjectRequestsAdapter
    private val projectRequests = mutableListOf<ProjectRequest>()

    // Custom auth variables
    private var currentSupervisorId: String? = null
    private var currentSupervisorName: String? = null
    private var currentSupervisorEmail: String? = null

    private val CHANNEL_ID = "excel_download_channel"
    private val NOTIFICATION_ID = 1002
    private var downloadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_requests)

        initializeUserData()
        initViews()
        setupRecyclerView()
        setupDrawerNavigation()
        loadProjectRequests()
    }

    private fun initializeUserData() {
        // Get supervisor data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        currentSupervisorId = sharedPref.getString("supervisor_id", null)
        currentSupervisorName = sharedPref.getString("supervisor_name", null)
        currentSupervisorEmail = sharedPref.getString("supervisor_email", null)

        Log.d("SupervisorRequests", "============ USER DATA ============")
        Log.d("SupervisorRequests", "Supervisor ID: '$currentSupervisorId'")
        Log.d("SupervisorRequests", "Supervisor Name: '$currentSupervisorName'")
        Log.d("SupervisorRequests", "Supervisor Email: '$currentSupervisorEmail'")
        Log.d("SupervisorRequests", "===================================")

        // Check if user is authenticated
        if (currentSupervisorId == null || currentSupervisorName == null) {
            Log.e("SupervisorRequests", "Supervisor not authenticated - redirecting to login")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }
    }

    private fun initViews() {
        navigationView = findViewById(R.id.navigation_view_sup)
        drawerLayout = findViewById(R.id.drawer_layout_sup)
        recyclerView = findViewById(R.id.recyclerViewRequestList)
        loadingView = findViewById(R.id.loadingView)
        noResultsView = findViewById(R.id.noResultsView)

        val chatFab: View = findViewById(R.id.groupChatButton)
        chatFab.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            intent.putExtra("supervisor_id", currentSupervisorId)
            intent.putExtra("supervisor_name", currentSupervisorName)
            startActivity(intent)
        }

        // Add download Excel click listener
        val downloadFab: View = findViewById(R.id.downloadExcelButton)
        downloadFab.setOnClickListener {
            startExcelDownloadWithNotification()
        }

        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    
    private fun startExcelDownloadWithNotification() {
        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                return
            }
        }

        createNotificationChannel()
        showDownloadNotification(0, "Preparing Excel export...")

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = "Accepted_Projects_${System.currentTimeMillis()}.xlsx"

                updateNotification(10, "Filtering accepted requests...")
                delay(500)

                updateNotification(30, "Generating Excel file...")
                val excelBytes = generateExcelBytes()
                delay(1000)

                updateNotification(60, "Processing data...")
                delay(500)

                updateNotification(90, "Saving to Downloads...")
                val uri = saveExcelToDownloads(fileName, excelBytes)
                delay(500)

                updateNotification(100, "Export complete!")
                delay(1000)

                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        showDownloadCompleteNotification(uri, fileName)
                    } else {
                        showDownloadErrorNotification("Failed to save Excel file to Downloads")
                    }
                }

            } catch (e: Exception) {
                Log.e("ExcelExport", "Error generating Excel", e)
                withContext(Dispatchers.Main) {
                    showDownloadErrorNotification("Excel export failed: ${e.message}")
                }
            }
        }
    }

    private fun saveExcelToDownloads(fileName: String, bytes: ByteArray): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = contentResolver
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val itemUri = resolver.insert(collection, values) ?: return null

            resolver.openOutputStream(itemUri)?.use { it.write(bytes) } ?: return null

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)
            itemUri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            Uri.fromFile(file)
        }
    }
    
    private fun generateExcelBytes(): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Project Details")

        // Create header style
        val headerStyle = workbook.createCellStyle()
        val headerFont = workbook.createFont()
        headerFont.bold = true
        headerFont.fontHeightInPoints = 14
        headerStyle.setFont(headerFont)
        headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.index
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        // Add title row with date/time
        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        val currentDateTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        titleCell.setCellValue("PROJECT DETAILS UPTO:- $currentDateTime")
        titleCell.cellStyle = headerStyle

        // Merge cells for title (spans 6 columns)
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))

        // Add empty row
        sheet.createRow(1)

        // Add column headers
        val headerRow = sheet.createRow(2)
        val headers = arrayOf("Group No", "Team Members", "Roll No", "Project Title", "Contact")

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Filter accepted requests
        val acceptedRequests = projectRequests.filter { it.status == "accepted" }

        // Add data rows
        acceptedRequests.forEachIndexed { rowIndex, request ->
            val row = sheet.createRow(rowIndex + 3)

            val groupNo = rowIndex + 1

            // Format team members vertically (one below another)
            val teamMembers = request.teamMembers.joinToString("\n") { member ->
                member["name"] ?: "N/A"
            }

            // Format roll numbers vertically
            val rollNumbers = request.teamMembers.joinToString("\n") { member ->
                member["roll"] ?: member["rollNo"] ?: "N/A"
            }

            // Format contact info vertically for all members
            val contact = request.teamMembers.joinToString("\n") { member ->
                val mobile = member["mobile"] ?: ""
                val email = member["email"] ?: ""
                listOf(mobile, email).filter { it.isNotBlank() }.joinToString(" / ")
            }.ifBlank { "N/A" }

            row.createCell(0).setCellValue(groupNo.toString())
            row.createCell(1).setCellValue(teamMembers)
            row.createCell(2).setCellValue(rollNumbers)
            row.createCell(3).setCellValue(request.projectTitle)
            row.createCell(4).setCellValue(contact)
        }

        // Manual column sizing (instead of autoSizeColumn)
        sheet.setColumnWidth(0, 2000)  // Group No
        sheet.setColumnWidth(1, 8000)  // Team Members
        sheet.setColumnWidth(2, 6000)  // Roll No
        sheet.setColumnWidth(3, 10000) // Project Title
        sheet.setColumnWidth(4, 8000)  // Contact
        
        // Convert to bytes
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Excel Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Excel export notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDownloadNotification(progress: Int, status: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Exporting Excel")
            .setContentText(status)
            .setProgress(100, progress, progress < 100)
            .setOngoing(progress < 100)
            .setAutoCancel(progress >= 100)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(progress: Int, status: String) {
        showDownloadNotification(progress, status)
    }

    private fun showDownloadCompleteNotification(uri: Uri, fileName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Excel Export Complete")
            .setContentText("$fileName saved to Downloads")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showDownloadErrorNotification(error: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Excel Export Failed")
            .setContentText(error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
    }

    private fun setupRecyclerView() {
        requestsAdapter = ProjectRequestsAdapter(projectRequests) { request ->
            val intent = Intent(this, RequestDetailsActivity::class.java)
            intent.putExtra("request_id", request.id)
            intent.putExtra("supervisor_id", currentSupervisorId)
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SupervisorRequestsActivity)
            adapter = requestsAdapter
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Navigate to profile
                }
                R.id.nav_supervisor_history -> {
                    // Navigate to history
                }
                R.id.nav_logout_sup -> {
                    handleLogout()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadProjectRequests() {
        if (currentSupervisorId == null) {
            Log.e("SupervisorRequests", "❌ Supervisor ID is NULL - cannot load requests")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        Log.d("SupervisorRequests", "========== STARTING QUERY ==========")
        Log.d("SupervisorRequests", "Querying with supervisorId: '$currentSupervisorId'")
        Log.d("SupervisorRequests", "Collection: project_requests")
        Log.d("SupervisorRequests", "====================================")

        showLoading(true)

        // First, let's check if there's ANY data in the collection
        db.collection("project_requests")
            .limit(5)
            .get()
            .addOnSuccessListener { allDocs ->
                Log.d("SupervisorRequests", "📊 DATABASE CHECK:")
                Log.d("SupervisorRequests", "Total documents in project_requests (first 5): ${allDocs.size()}")
                
                if (allDocs.isEmpty) {
                    Log.w("SupervisorRequests", "⚠️ project_requests collection is EMPTY!")
                } else {
                    Log.d("SupervisorRequests", "✅ Sample documents found:")
                    allDocs.forEachIndexed { index, doc ->
                        val storedSupervisorId = doc.getString("supervisorId")
                        val title = doc.getString("projectTitle")
                        val status = doc.getString("status")
                        
                        Log.d("SupervisorRequests", "  [$index] Doc ID: ${doc.id}")
                        Log.d("SupervisorRequests", "      supervisorId: '$storedSupervisorId'")
                        Log.d("SupervisorRequests", "      projectTitle: '$title'")
                        Log.d("SupervisorRequests", "      status: '$status'")
                        Log.d("SupervisorRequests", "      Match? ${storedSupervisorId == currentSupervisorId}")
                    }
                }
                
                // Now do the actual filtered query
                performFilteredQuery()
            }
            .addOnFailureListener { e ->
                Log.e("SupervisorRequests", "❌ Failed to check database: ${e.message}", e)
                performFilteredQuery() // Try filtered query anyway
            }
    }

    private fun performFilteredQuery() {
        Log.d("SupervisorRequests", "========== FILTERED QUERY ==========")
        Log.d("SupervisorRequests", "Query: whereEqualTo('supervisorId', '$currentSupervisorId')")
        
        db.collection("project_requests")
            .whereEqualTo("supervisorId", currentSupervisorId)
            .get()
            .addOnSuccessListener { documents ->
                showLoading(false)
                
                Log.d("SupervisorRequests", "✅ QUERY SUCCESSFUL!")
                Log.d("SupervisorRequests", "Documents found: ${documents.size()}")
                
                projectRequests.clear()

                if (documents.isEmpty) {
                    Log.w("SupervisorRequests", "⚠️ No documents matched the query")
                    Log.w("SupervisorRequests", "Current supervisorId: '$currentSupervisorId'")
                    Log.w("SupervisorRequests", "Possible reasons:")
                    Log.w("SupervisorRequests", "  1. No requests sent to this supervisor")
                    Log.w("SupervisorRequests", "  2. supervisorId mismatch (check spacing/case)")
                    Log.w("SupervisorRequests", "  3. Data stored with different field name")
                } else {
                    Log.d("SupervisorRequests", "📝 Processing ${documents.size()} documents...")
                }

                for (document in documents) {
                    try {
                        Log.d("SupervisorRequests", "Processing doc: ${document.id}")
                        
                        val request = ProjectRequest(
                            id = document.getString("id") ?: document.id,
                            leaderId = document.getString("leaderId") ?: "",
                            supervisorId = document.getString("supervisorId") ?: "",
                            supervisorName = document.getString("supervisorName") ?: "",
                            teamMembers = document.get("teamMembers") as? List<Map<String, String>> ?: emptyList(),
                            projectTitle = document.getString("projectTitle") ?: "",
                            projectSummary = document.getString("projectSummary") ?: "",
                            status = document.getString("status") ?: "pending",
                            timestamp = document.getLong("timestamp") ?: 0L,
                            requestDate = document.getString("requestDate") ?: "",
                            supervisorComment = document.getString("supervisorComment") ?: "",
                            createdAt = document.getLong("createdAt") ?: 0L,
                            updatedAt = document.getLong("updatedAt") ?: 0L
                        )
                        projectRequests.add(request)

                        Log.d("SupervisorRequests", "✅ Added: '${request.projectTitle}' - ${request.status}")
                    } catch (e: Exception) {
                        Log.e("SupervisorRequests", "❌ Parse error for doc ${document.id}: ${e.message}", e)
                    }
                }

                // Sort by timestamp (newest first)
                projectRequests.sortByDescending { it.timestamp }
                
                Log.d("SupervisorRequests", "========== QUERY COMPLETE ==========")
                Log.d("SupervisorRequests", "Final list size: ${projectRequests.size}")

                updateUI()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e("SupervisorRequests", "========== QUERY FAILED ==========")
                Log.e("SupervisorRequests", "❌ Error: ${exception.message}", exception)
                Log.e("SupervisorRequests", "Error type: ${exception.javaClass.simpleName}")
                
                Toast.makeText(
                    this,
                    "Failed to load requests: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                
                updateUI()
            }
    }

    private fun updateUI() {
        if (projectRequests.isEmpty()) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.VISIBLE
            Log.d("SupervisorRequests", "UI: Showing 'No results' view")
        } else {
            recyclerView.visibility = View.VISIBLE
            noResultsView.visibility = View.GONE
            requestsAdapter.notifyDataSetChanged()
            Log.d("SupervisorRequests", "UI: Showing ${projectRequests.size} requests")
        }
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            recyclerView.visibility = View.GONE
            noResultsView.visibility = View.GONE
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, SupervisorLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleLogout() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        Log.d("SupervisorRequests", "User logged out successfully")
        redirectToLogin()
    }

    private fun isUserAuthenticated(): Boolean {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val supervisorId = sharedPref.getString("supervisor_id", null)
        val role = sharedPref.getString("role", null)
        return supervisorId != null && role == "supervisor"
    }

    override fun onResume() {
        super.onResume()

        if (!isUserAuthenticated()) {
            redirectToLogin()
            return
        }

        initializeUserData()
        loadProjectRequests()
    }
}
