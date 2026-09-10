package com.example.onetomany.data.repository

import com.example.onetomany.data.local.UserDao
import com.example.onetomany.data.local.UserEntity
import com.example.onetomany.data.model.User
import com.example.onetomany.data.remote.OnlineDatabaseService
import com.example.onetomany.data.security.SecurityManager
import com.example.onetomany.data.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val onlineDatabaseService: OnlineDatabaseService
) {

    fun getAllUsersFlow(): Flow<List<User>> {
        return userDao.getAllUsersFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getActiveUser(): User? = withContext(Dispatchers.IO) {
        val activeId = sessionManager.getActiveUserId() ?: return@withContext null
        userDao.getUserById(activeId)?.toDomain()
    }

    suspend fun registerUser(email: String, name: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please provide a valid email address"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists"))
        }

        val userId = UUID.randomUUID().toString()
        val salt = SecurityManager.generateSalt()
        val passwordHash = SecurityManager.hashPassword(password, salt)

        val entity = UserEntity(
            id = userId,
            email = trimmedEmail,
            displayName = if (trimmedName.isNotBlank()) trimmedName else trimmedEmail.substringBefore("@"),
            passwordHash = passwordHash,
            salt = salt,
            role = "Broadcaster",
            createdAt = System.currentTimeMillis()
        )

        userDao.insertUser(entity)
        val user = entity.toDomain()

        // Sync to online database
        onlineDatabaseService.syncUserToCloud(user)

        // Save session
        val token = SecurityManager.generateSecureToken()
        sessionManager.saveSession(user.id, user.email, user.displayName, token)

        Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val entity = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("No account found for $trimmedEmail"))

        val isValid = SecurityManager.verifyPassword(password, entity.salt, entity.passwordHash)
        if (!isValid) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        val user = entity.toDomain()
        val token = SecurityManager.generateSecureToken()
        sessionManager.saveSession(user.id, user.email, user.displayName, token)

        onlineDatabaseService.syncUserToCloud(user)
        Result.success(user)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        sessionManager.clearSession()
    }

    suspend fun switchUser(userId: String): Result<User> = withContext(Dispatchers.IO) {
        val entity = userDao.getUserById(userId)
            ?: return@withContext Result.failure(IllegalArgumentException("User not found"))
        val user = entity.toDomain()
        val token = SecurityManager.generateSecureToken()
        sessionManager.saveSession(user.id, user.email, user.displayName, token)
        Result.success(user)
    }

    suspend fun ensureDefaultUserIfEmpty(): User = withContext(Dispatchers.IO) {
        val existingActive = getActiveUser()
        if (existingActive != null) return@withContext existingActive

        // Register default creator account if none exists
        val defaultEmail = "creator@onetomany.io"
        val existing = userDao.getUserByEmail(defaultEmail)
        if (existing != null) {
            val token = SecurityManager.generateSecureToken()
            sessionManager.saveSession(existing.id, existing.email, existing.displayName, token)
            return@withContext existing.toDomain()
        }

        val userId = UUID.randomUUID().toString()
        val salt = SecurityManager.generateSalt()
        val passwordHash = SecurityManager.hashPassword("SecurePass2026!", salt)
        val defaultEntity = UserEntity(
            id = userId,
            email = defaultEmail,
            displayName = "Primary Broadcaster",
            passwordHash = passwordHash,
            salt = salt,
            role = "Admin / Broadcaster",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(defaultEntity)
        val defaultUser = defaultEntity.toDomain()
        onlineDatabaseService.syncUserToCloud(defaultUser)
        val token = SecurityManager.generateSecureToken()
        sessionManager.saveSession(defaultUser.id, defaultUser.email, defaultUser.displayName, token)
        defaultUser
    }
}
