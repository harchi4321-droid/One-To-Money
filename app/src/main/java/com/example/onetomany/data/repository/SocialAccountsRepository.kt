package com.example.onetomany.data.repository

import com.example.onetomany.data.local.SocialCredentialDao
import com.example.onetomany.data.local.SocialCredentialEntity
import com.example.onetomany.data.model.SocialAccountCredential
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.remote.OnlineDatabaseService
import com.example.onetomany.data.remote.SocialApiDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SocialAccountsRepository(
    private val socialCredentialDao: SocialCredentialDao,
    private val apiDispatcher: SocialApiDispatcher,
    private val onlineDatabaseService: OnlineDatabaseService
) {

    fun getCredentialsFlow(userId: String): Flow<List<SocialAccountCredential>> {
        return socialCredentialDao.getCredentialsForUserFlow(userId).map { list ->
            // Ensure all 5 platforms are represented
            val map = list.associateBy { it.platformId }
            SocialPlatform.entries.map { platform ->
                map[platform.id]?.toDomain() ?: SocialAccountCredential(
                    userId = userId,
                    platform = platform,
                    accountHandle = "",
                    accessToken = "",
                    isConnected = false
                )
            }
        }
    }

    suspend fun getCredentialForPlatform(userId: String, platform: SocialPlatform): SocialAccountCredential = withContext(Dispatchers.IO) {
        val entity = socialCredentialDao.getCredential(userId, platform.id)
        entity?.toDomain() ?: SocialAccountCredential(
            userId = userId,
            platform = platform,
            accountHandle = "",
            accessToken = "",
            isConnected = false
        )
    }

    suspend fun updateCredential(
        userId: String,
        platform: SocialPlatform,
        handle: String,
        token: String,
        targetId: String = ""
    ): SocialAccountCredential = withContext(Dispatchers.IO) {
        val entity = SocialCredentialEntity(
            userId = userId,
            platformId = platform.id,
            accountHandle = handle.trim(),
            accessToken = token.trim(),
            targetPageOrChannelId = targetId.trim(),
            isConnected = token.isNotBlank(),
            lastVerifiedAt = 0L,
            errorMessage = null
        )
        socialCredentialDao.insertCredential(entity)
        val domain = entity.toDomain()
        onlineDatabaseService.syncCredentialToCloud(domain)
        domain
    }

    suspend fun testAndVerifyCredential(
        userId: String,
        platform: SocialPlatform
    ): Result<String> = withContext(Dispatchers.IO) {
        val current = getCredentialForPlatform(userId, platform)
        if (current.accessToken.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a token first."))
        }

        val testResult = apiDispatcher.testConnection(current)
        val isSuccess = testResult.isSuccess
        val message = testResult.getOrNull() ?: testResult.exceptionOrNull()?.message ?: "Verification failed"

        val updatedEntity = SocialCredentialEntity(
            userId = userId,
            platformId = platform.id,
            accountHandle = current.accountHandle,
            accessToken = current.accessToken,
            targetPageOrChannelId = current.targetPageOrChannelId,
            isConnected = isSuccess,
            lastVerifiedAt = System.currentTimeMillis(),
            errorMessage = if (!isSuccess) message else null
        )

        socialCredentialDao.insertCredential(updatedEntity)
        onlineDatabaseService.syncCredentialToCloud(updatedEntity.toDomain())

        if (isSuccess) {
            Result.success(message)
        } else {
            Result.failure(Exception(message))
        }
    }
}
