package com.example.onetomany.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.onetomany.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String,
    val role: String = "Creator",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        displayName = displayName,
        role = role,
        avatarUrl = avatarUrl,
        createdAt = createdAt
    )
}
