package com.example.onetomany.data.repository

import com.example.onetomany.data.local.BroadcastDao
import com.example.onetomany.data.local.BroadcastPostEntity
import com.example.onetomany.data.model.BroadcastOverallStatus
import com.example.onetomany.data.model.BroadcastPost
import com.example.onetomany.data.model.DeliveryState
import com.example.onetomany.data.model.PlatformDeliveryResult
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.remote.OnlineDatabaseService
import com.example.onetomany.data.remote.SocialApiDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class BroadcastRepository(
    private val broadcastDao: BroadcastDao,
    private val socialAccountsRepository: SocialAccountsRepository,
    private val apiDispatcher: SocialApiDispatcher,
    private val onlineDatabaseService: OnlineDatabaseService
) {

    fun getBroadcastHistoryFlow(userId: String): Flow<List<BroadcastPost>> {
        return broadcastDao.getPostsForUserFlow(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getPostById(id: String): BroadcastPost? = withContext(Dispatchers.IO) {
        broadcastDao.getPostById(id)?.toDomain()
    }

    suspend fun broadcastSimultaneously(
        userId: String,
        title: String,
        content: String,
        mediaUrl: String,
        hashtags: List<String>,
        targetPlatforms: List<SocialPlatform>,
        onProgressUpdate: (Map<SocialPlatform, PlatformDeliveryResult>) -> Unit = {}
    ): BroadcastPost = withContext(Dispatchers.IO) {
        val postId = UUID.randomUUID().toString()

        // Initialize progress map
        val progressMap = targetPlatforms.associateWith { platform ->
            PlatformDeliveryResult(
                platform = platform,
                state = DeliveryState.IN_PROGRESS,
                responseMessage = "Dispatching payload...",
                completedAt = 0L
            )
        }.toMutableMap()
        onProgressUpdate(progressMap.toMap())

        // Fetch credentials for all platforms concurrently
        val credsMap = targetPlatforms.associateWith { platform ->
            socialAccountsRepository.getCredentialForPlatform(userId, platform)
        }

        // Execute concurrent publishing to all 5 platforms simultaneously!
        coroutineScope {
            val jobs = targetPlatforms.map { platform ->
                async {
                    val cred = credsMap[platform] ?: socialAccountsRepository.getCredentialForPlatform(userId, platform)
                    val result = apiDispatcher.publishPost(
                        credential = cred,
                        title = title,
                        content = content,
                        mediaUrl = mediaUrl,
                        hashtags = hashtags
                    )
                    synchronized(progressMap) {
                        progressMap[platform] = result
                        onProgressUpdate(progressMap.toMap())
                    }
                    result
                }
            }
            jobs.awaitAll()
        }

        // Determine overall status
        val allSuccess = progressMap.values.all { it.state == DeliveryState.SUCCESS }
        val anySuccess = progressMap.values.any { it.state == DeliveryState.SUCCESS }
        val overallStatus = when {
            allSuccess -> BroadcastOverallStatus.SUCCESS_ALL
            anySuccess -> BroadcastOverallStatus.PARTIAL_SUCCESS
            else -> BroadcastOverallStatus.FAILED
        }

        val post = BroadcastPost(
            id = postId,
            userId = userId,
            title = title,
            content = content,
            mediaUrl = mediaUrl,
            hashtags = hashtags,
            targetPlatforms = targetPlatforms,
            deliveryResults = progressMap.toMap(),
            overallStatus = overallStatus,
            createdAt = System.currentTimeMillis(),
            isSyncedToOnlineDb = false
        )

        // Save locally in Room
        val entity = BroadcastPostEntity.fromDomain(post)
        broadcastDao.insertPost(entity)

        // Sync immediately to Online Cloud Database
        val cloudSynced = onlineDatabaseService.syncBroadcastPostToCloud(post)
        if (cloudSynced) {
            broadcastDao.markSynced(post.id)
        }

        post.copy(isSyncedToOnlineDb = cloudSynced)
    }

    suspend fun retryFailedPlatforms(
        post: BroadcastPost,
        onProgressUpdate: (Map<SocialPlatform, PlatformDeliveryResult>) -> Unit = {}
    ): BroadcastPost = withContext(Dispatchers.IO) {
        val failedPlatforms = post.deliveryResults.filter { it.value.state == DeliveryState.FAILED }.keys.toList()
        if (failedPlatforms.isEmpty()) return@withContext post

        val currentMap = post.deliveryResults.toMutableMap()
        failedPlatforms.forEach { platform ->
            currentMap[platform] = PlatformDeliveryResult(
                platform = platform,
                state = DeliveryState.IN_PROGRESS,
                responseMessage = "Retrying broadcast...",
                completedAt = 0L
            )
        }
        onProgressUpdate(currentMap.toMap())

        coroutineScope {
            val jobs = failedPlatforms.map { platform ->
                async {
                    val cred = socialAccountsRepository.getCredentialForPlatform(post.userId, platform)
                    val result = apiDispatcher.publishPost(
                        credential = cred,
                        title = post.title,
                        content = post.content,
                        mediaUrl = post.mediaUrl,
                        hashtags = post.hashtags
                    )
                    synchronized(currentMap) {
                        currentMap[platform] = result
                        onProgressUpdate(currentMap.toMap())
                    }
                    result
                }
            }
            jobs.awaitAll()
        }

        val allSuccess = currentMap.values.all { it.state == DeliveryState.SUCCESS }
        val anySuccess = currentMap.values.any { it.state == DeliveryState.SUCCESS }
        val overallStatus = when {
            allSuccess -> BroadcastOverallStatus.SUCCESS_ALL
            anySuccess -> BroadcastOverallStatus.PARTIAL_SUCCESS
            else -> BroadcastOverallStatus.FAILED
        }

        val updated = post.copy(
            deliveryResults = currentMap.toMap(),
            overallStatus = overallStatus
        )

        broadcastDao.insertPost(BroadcastPostEntity.fromDomain(updated))
        onlineDatabaseService.syncBroadcastPostToCloud(updated)

        updated
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        broadcastDao.deletePost(postId)
    }
}
