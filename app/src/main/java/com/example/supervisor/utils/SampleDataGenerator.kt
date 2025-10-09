package com.example.supervisor.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.example.supervisor.data.Supervisor

object SampleDataGenerator {
    
    private val db = FirebaseFirestore.getInstance()
    
    fun addSampleSupervisors() {
        val sampleSupervisors = listOf(
            mapOf(
                "name" to "Dr. Sarah Johnson",
                "email" to "sarah.johnson@university.edu",
                "department" to "Computer Science",
                "specialization" to "Artificial Intelligence, Machine Learning, Deep Learning",
                "maxRequests" to 20L,
                "requestCount" to 5L,
                "bio" to "Professor with 15+ years of experience in AI research. Specializes in neural networks, computer vision, and natural language processing. Published 50+ papers in top-tier conferences.",
                "phone" to "+1-555-0101",
                "isAvailable" to true,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. Michael Chen",
                "email" to "michael.chen@university.edu",
                "department" to "Computer Science",
                "specialization" to "Software Engineering, Database Systems, Cloud Computing",
                "maxRequests" to 18L,
                "requestCount" to 12L,
                "bio" to "Expert in software architecture and distributed systems. Led multiple industry projects and has extensive experience in modern software development practices.",
                "phone" to "+1-555-0102",
                "isAvailable" to true,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. Emily Rodriguez",
                "email" to "emily.rodriguez@university.edu",
                "department" to "Information Technology",
                "specialization" to "Cybersecurity, Network Security, Cryptography",
                "maxRequests" to 15L,
                "requestCount" to 15L,
                "bio" to "Cybersecurity expert with focus on network security and cryptography. Former industry security consultant with hands-on experience in enterprise security solutions.",
                "phone" to "+1-555-0103",
                "isAvailable" to false,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. James Wilson",
                "email" to "james.wilson@university.edu",
                "department" to "Computer Science",
                "specialization" to "Data Science, Big Data Analytics, Statistics",
                "maxRequests" to 25L,
                "requestCount" to 8L,
                "bio" to "Data science researcher specializing in big data analytics and statistical modeling. Works with both academic and industry partners on data-driven solutions.",
                "phone" to "+1-555-0104",
                "isAvailable" to true,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. Lisa Thompson",
                "email" to "lisa.thompson@university.edu",
                "department" to "Computer Science",
                "specialization" to "Mobile Development, User Experience, Human-Computer Interaction",
                "maxRequests" to 22L,
                "requestCount" to 3L,
                "bio" to "Mobile app development expert with focus on user experience and human-computer interaction. Has published extensively on mobile UX design patterns.",
                "phone" to "+1-555-0105",
                "isAvailable" to true,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. Robert Kim",
                "email" to "robert.kim@university.edu",
                "department" to "Information Technology",
                "specialization" to "Web Development, Full-Stack Development, DevOps",
                "maxRequests" to 20L,
                "requestCount" to 20L,
                "bio" to "Full-stack development expert with extensive experience in modern web technologies and DevOps practices. Industry veteran with 20+ years of experience.",
                "phone" to "+1-555-0106",
                "isAvailable" to false,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. Maria Garcia",
                "email" to "maria.garcia@university.edu",
                "department" to "Computer Science",
                "specialization" to "Computer Graphics, Game Development, Virtual Reality",
                "maxRequests" to 16L,
                "requestCount" to 4L,
                "bio" to "Computer graphics researcher with focus on real-time rendering and VR/AR technologies. Has worked on several commercial game projects.",
                "phone" to "+1-555-0107",
                "isAvailable" to true,
                "profileImageUrl" to ""
            ),
            mapOf(
                "name" to "Dr. David Lee",
                "email" to "david.lee@university.edu",
                "department" to "Information Technology",
                "specialization" to "Blockchain, Cryptocurrency, Distributed Systems",
                "maxRequests" to 14L,
                "requestCount" to 6L,
                "bio" to "Blockchain technology expert with research focus on cryptocurrency systems and distributed ledger technologies. Industry consultant for fintech companies.",
                "phone" to "+1-555-0108",
                "isAvailable" to true,
                "profileImageUrl" to ""
            )
        )
        
        // Add each supervisor to Firestore
        sampleSupervisors.forEachIndexed { index, supervisorData ->
            db.collection("supervisors")
                .add(supervisorData)
                .addOnSuccessListener { documentReference ->
                    println("Sample supervisor ${index + 1} added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    println("Error adding sample supervisor ${index + 1}: $e")
                }
        }
    }
}