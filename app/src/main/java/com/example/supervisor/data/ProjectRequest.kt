package com.example.supervisor.data

data class ProjectRequest(
    val id: String = "",
    val leaderId: String = "",
    val supervisorId: String = "",
    val supervisorName: String = "",
    val teamMembers: List<Map<String, String>> = emptyList(),
    val projectTitle: String = "",
    val projectSummary: String = "",
    val status: String = "pending",
    val timestamp: Long = 0L,
    val requestDate: String = "",
    val supervisorComment: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
