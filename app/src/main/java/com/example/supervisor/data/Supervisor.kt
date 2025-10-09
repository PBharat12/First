package com.example.supervisor.data

data class Supervisor(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val department: String = "",
    val specialization: String = "",
    val maxRequests: Long = 20L,
    val currentRequests: Long = 0L,
    val bio: String = "",
    val phone: String = "",
    val isAvailable: Boolean = true,
    val profileImageUrl: String = ""
)