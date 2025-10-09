package com.example.supervisor.data

data class ProjectReport(
    val id: String = "",
    val projectTitle: String = "",
    val description: String = "",
    val status: String = "accepted", // pending, accepted, rejected
    val submittedDate: Long = System.currentTimeMillis(),
    val acceptedDate: Long = System.currentTimeMillis(),
    val teamMembers: List<TeamMember> = emptyList(),
    val supervisorId: String = "",
    val supervisorName: String = "",
    val category: String = "",
    val tags: List<String> = emptyList()
) {
    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(acceptedDate))
        }
    
    val formattedTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(acceptedDate))
        }
    
    val teamSize: Int
        get() = teamMembers.size
}

data class TeamMember(
    val name: String = "",
    val rollNo: String = "",
    val email: String = "",
    val mobile: String = "",
    val role: String = "Member" // Lead, Member
)
