package com.example.onetomany.data.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String = "Creator",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val onlineDatabaseSynced: Boolean = true
)
