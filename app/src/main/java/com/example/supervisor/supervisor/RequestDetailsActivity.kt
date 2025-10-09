package com.example.supervisor.supervisor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supervisor.R
import com.example.supervisor.adapter.TeamMemberAdapter
import com.example.supervisor.data.TeamMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class RequestDetailsActivity : AppCompatActivity() {

    private lateinit var tvProjectTitle: TextView
    private lateinit var tvProjectSummary: TextView
    private lateinit var tvRequestDate: TextView
    private lateinit var rvTeamMembers: RecyclerView
    private lateinit var etComment: EditText
    private lateinit var btnAccept: Button
    private lateinit var btnReject: Button
    private lateinit var supervisorId: String

    private val CHANNEL_ID = "download_channel"
    private val NOTIFICATION_ID = 1001
    private var downloadJob: Job? = null

    private val db = FirebaseFirestore.getInstance()
    private var requestId: String? = null
    private var teamAdapter: TeamMemberAdapter? = null
    private val teamList = mutableListOf<TeamMember>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)

        requestId = intent.getStringExtra("request_id")
        initViews()
        loadRequestDetails()
    }

    private fun initViews() {
        tvProjectTitle = findViewById(R.id.tvProjectTitle)
        tvProjectSummary = findViewById(R.id.tvProjectSummary)
        tvRequestDate = findViewById(R.id.tvRequestDate)
        rvTeamMembers = findViewById(R.id.rvTeamMembers)
        etComment = findViewById(R.id.etComment)
        btnAccept = findViewById(R.id.btnAccept)
        btnReject = findViewById(R.id.btnReject)

        supervisorId = intent.getStringExtra("supervisor_id") ?: ""

        rvTeamMembers.layoutManager = LinearLayoutManager(this)
        teamAdapter = TeamMemberAdapter(teamList)
        rvTeamMembers.adapter = teamAdapter

        btnAccept.setOnClickListener {
            if (!requestId.isNullOrEmpty()) {
                updateProjectStatus(requestId!!, supervisorId, "accepted", etComment.text.toString().trim())
            } else {
                Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<android.view.View>(R.id.btnDownload).setOnClickListener {
            startDownloadWithNotification()
        }

        btnReject.setOnClickListener {
            if (!requestId.isNullOrEmpty()) {
                updateProjectStatus(requestId!!, supervisorId, "rejected", etComment.text.toString().trim())
            } else {
                Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDownloadWithNotification() {
        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                return
            }
        }

        createNotificationChannel()
        showDownloadNotification(0, "Preparing download...")

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = "Project_Request_${System.currentTimeMillis()}.docx"

                updateNotification(10, "Preparing document...")
                delay(500)

                updateNotification(30, "Generating document...")
                val docBytes = generateDocumentBytes()
                delay(1000)

                updateNotification(60, "Processing document...")
                delay(500)

                updateNotification(90, "Saving to Downloads...")
                val uri = saveToDownloads(fileName, docBytes)
                delay(500)

                updateNotification(100, "Download complete!")
                delay(1000)

                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        showDownloadCompleteNotification(uri, fileName)
                    } else {
                        showDownloadErrorNotification("Failed to save file to Downloads")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showDownloadErrorNotification(e.message ?: "Download failed")
                }
            }
        }
    }

    private fun generateDocumentBytes(): ByteArray {
        val members = teamList
        
        fun getSafe(list: List<TeamMember>, idx: Int, accessor: (TeamMember) -> String): String {
            return list.getOrNull(idx)?.let(accessor) ?: ""
        }

        val placeholders = mapOf(
            "\${roll1}" to getSafe(members, 0) { it.rollNo },
            "\${name1}" to getSafe(members, 0) { it.name },
            "\${contact1}" to getSafe(members, 0) { 
                listOf(it.mobile, it.email).filter { str -> str.isNotBlank() }.joinToString(" / ") 
            },
            "\${roll2}" to getSafe(members, 1) { it.rollNo },
            "\${name2}" to getSafe(members, 1) { it.name },
            "\${contact2}" to getSafe(members, 1) { 
                listOf(it.mobile, it.email).filter { str -> str.isNotBlank() }.joinToString(" / ") 
            },
            "\${roll3}" to getSafe(members, 2) { it.rollNo },
            "\${name3}" to getSafe(members, 2) { it.name },
            "\${contact3}" to getSafe(members, 2) { 
                listOf(it.mobile, it.email).filter { str -> str.isNotBlank() }.joinToString(" / ") 
            },
            "\${projectTitle}" to (tvProjectTitle.text?.toString().orEmpty()),
            "\${projectAbstract}" to (tvProjectSummary.text?.toString().orEmpty())
        )

        return createDocxBytesFromTemplate("request_template.docx", placeholders)
    }

    private fun createDocxBytesFromTemplate(assetName: String, placeholders: Map<String, String>): ByteArray {
        val templateBytes = assets.open(assetName).use { it.readBytes() }

        val zipEntries: MutableMap<String, ByteArray> = mutableMapOf()
        ByteArrayInputStream(templateBytes).use { bais ->
            ZipInputStream(bais).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val bos = ByteArrayOutputStream()
                    zis.copyTo(bos)
                    zipEntries[entry.name] = bos.toByteArray()
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        val docPath = "word/document.xml"
        val originalXml = zipEntries[docPath]?.toString(Charsets.UTF_8)
            ?: throw IllegalStateException("document.xml missing in template")

        var replacedXml = originalXml
        placeholders.forEach { (key, value) ->
            replacedXml = replacedXml.replace(key, escapeXml(value))
        }
        zipEntries[docPath] = replacedXml.toByteArray(Charsets.UTF_8)

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            zipEntries.forEach { (name, bytes) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun saveToDownloads(fileName: String, bytes: ByteArray): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download notifications"
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
            .setContentTitle("Downloading Document")
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
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Download Complete")
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
            .setContentTitle("Download Failed")
            .setContentText(error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun updateProjectStatus(requestId: String?, supervisorId: String, newStatus: String, comment: String) {
        if (requestId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show()
            return
        }
        if (supervisorId.isEmpty()) {
            Toast.makeText(this, "Invalid Supervisor ID", Toast.LENGTH_SHORT).show()
            return
        }

        val requestRef = db.collection("project_requests").document(requestId)
        val supervisorRef = db.collection("supervisors").document(supervisorId)

        db.runTransaction { transaction ->
            val requestSnapshot = transaction.get(requestRef)
            if (!requestSnapshot.exists()) {
                throw Exception("Request not found")
            }
            val oldStatus = (requestSnapshot.getString("status") ?: "pending").lowercase()

            var supervisorCurrentCount: Long? = null
            var supervisorMax: Long? = null
            if (newStatus == "accepted" && oldStatus != "accepted") {
                val supervisorSnapshot = transaction.get(supervisorRef)
                if (!supervisorSnapshot.exists()) {
                    throw Exception("Supervisor document not found")
                }
                supervisorCurrentCount = supervisorSnapshot.getLong("requestCount") ?: 0L
                supervisorMax = supervisorSnapshot.getLong("maxRequests") ?: 20L

                if (supervisorCurrentCount >= supervisorMax) {
                    throw Exception("This supervisor has already reached the maximum accepted requests.")
                }
            }

            transaction.update(requestRef, mapOf(
                "status" to newStatus,
                "supervisorComment" to comment,
                "updatedAt" to System.currentTimeMillis()
            ))

            if (newStatus == "accepted" && supervisorCurrentCount != null) {
                transaction.update(supervisorRef, "requestCount", supervisorCurrentCount + 1)
            }
        }.addOnSuccessListener {
            Toast.makeText(this, "Request $newStatus successfully", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("RequestDetails", "Transaction failed", e)
        }
    }

    private fun loadRequestDetails() {
        if (requestId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("project_requests").document(requestId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvProjectTitle.text = document.getString("projectTitle")
                    tvProjectSummary.text = document.getString("projectSummary")
                    tvRequestDate.text = "Requested on: ${document.getString("requestDate")}"

                    // Convert Map<String, String> to TeamMember objects
                    val members = document.get("teamMembers") as? List<HashMap<String, Any>>
                    if (members != null) {
                        teamList.clear()
                        members.forEach { memberMap ->
                            val teamMember = TeamMember(
                                name = memberMap["name"] as? String ?: memberMap["fullName"] as? String ?: "",
                                rollNo = memberMap["rollNo"] as? String ?: memberMap["roll"] as? String ?: "",
                                email = memberMap["email"] as? String ?: "",
                                mobile = memberMap["mobile"] as? String ?: memberMap["contact"] as? String ?: "",
                                role = memberMap["role"] as? String ?: "Member"
                            )
                            teamList.add(teamMember)
                        }
                        teamAdapter?.notifyDataSetChanged()
                    }

                    etComment.setText(document.getString("supervisorComment") ?: "")
                } else {
                    Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RequestDetails", "Error loading request: ${e.message}")
                Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
    }
}
