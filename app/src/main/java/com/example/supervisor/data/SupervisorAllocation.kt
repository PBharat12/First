package com.example.supervisor.data

data class SupervisorAllocation(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val department: String = "",
    val requestCount: Int = 0,
    val maxRequests: Int = 20,
    val isActive: Boolean = true,
    val lastActive: Long = System.currentTimeMillis(),
    val specialization: String = "",
    val experience: String = ""
) {
    val availableSlots: Int
        get() = maxRequests - requestCount

    val utilizationPercentage: Int
        get() = if (maxRequests > 0) (requestCount * 100) / maxRequests else 0

    val initials: String
        get() = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

    fun getAvailabilityText(): String = "$requestCount/$maxRequests"

    fun isAvailable(): Boolean = requestCount < maxRequests
}
