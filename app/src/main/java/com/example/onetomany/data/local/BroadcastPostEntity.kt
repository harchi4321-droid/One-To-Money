package com.example.onetomany.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.onetomany.data.model.BroadcastOverallStatus
import com.example.onetomany.data.model.BroadcastPost
import com.example.onetomany.data.model.DeliveryState
import com.example.onetomany.data.model.PlatformDeliveryResult
import com.example.onetomany.data.model.SocialPlatform
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "broadcast_posts")
data class BroadcastPostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mediaUrl: String,
    val hashtagsRaw: String, // comma-separated or JSON
    val targetPlatformsRaw: String, // comma-separated
    val deliveryResultsJson: String, // JSON string
    val overallStatus: String,
    val createdAt: Long,
    val isSyncedToOnlineDb: Boolean
) {
    fun toDomain(): BroadcastPost {
        val tags = if (hashtagsRaw.isBlank()) emptyList() else hashtagsRaw.split("||").filter { it.isNotBlank() }
        val platforms = if (targetPlatformsRaw.isBlank()) {
            SocialPlatform.entries
        } else {
            targetPlatformsRaw.split(",").mapNotNull { id ->
                SocialPlatform.entries.find { it.id.equals(id.trim(), ignoreCase = true) }
            }
        }

        val resultsMap = mutableMapOf<SocialPlatform, PlatformDeliveryResult>()
        try {
            if (deliveryResultsJson.isNotBlank()) {
                val json = JSONObject(deliveryResultsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val p = SocialPlatform.fromId(key)
                    val obj = json.getJSONObject(key)
                    val stateStr = obj.optString("state", DeliveryState.IDLE.name)
                    val state = runCatching { DeliveryState.valueOf(stateStr) }.getOrDefault(DeliveryState.IDLE)
                    val code = obj.optInt("code", 0)
                    val msg = obj.optString("msg", "")
                    val postId = obj.optString("postId").takeIf { it.isNotBlank() }
                    val postUrl = obj.optString("postUrl").takeIf { it.isNotBlank() }
                    val completedAt = obj.optLong("time", 0L)

                    resultsMap[p] = PlatformDeliveryResult(
                        platform = p,
                        state = state,
                        responseCode = code,
                        responseMessage = msg,
                        externalPostId = postId,
                        externalUrl = postUrl,
                        completedAt = completedAt
                    )
                }
            }
        } catch (_: Exception) {
        }

        val status = runCatching { BroadcastOverallStatus.valueOf(overallStatus) }
            .getOrDefault(BroadcastOverallStatus.DRAFT)

        return BroadcastPost(
            id = id,
            userId = userId,
            title = title,
            content = content,
            mediaUrl = mediaUrl,
            hashtags = tags,
            targetPlatforms = platforms,
            deliveryResults = resultsMap,
            overallStatus = status,
            createdAt = createdAt,
            isSyncedToOnlineDb = isSyncedToOnlineDb
        )
    }

    companion object {
        fun fromDomain(domain: BroadcastPost): BroadcastPostEntity {
            val tagsJoined = domain.hashtags.joinToString("||")
            val platformsJoined = domain.targetPlatforms.joinToString(",") { it.id }

            val json = JSONObject()
            domain.deliveryResults.forEach { (platform, result) ->
                val obj = JSONObject()
                obj.put("state", result.state.name)
                obj.put("code", result.responseCode)
                obj.put("msg", result.responseMessage)
                obj.put("postId", result.externalPostId ?: "")
                obj.put("postUrl", result.externalUrl ?: "")
                obj.put("time", result.completedAt)
                json.put(platform.id, obj)
            }

            return BroadcastPostEntity(
                id = domain.id,
                userId = domain.userId,
                title = domain.title,
                content = domain.content,
                mediaUrl = domain.mediaUrl,
                hashtagsRaw = tagsJoined,
                targetPlatformsRaw = platformsJoined,
                deliveryResultsJson = json.toString(),
                overallStatus = domain.overallStatus.name,
                createdAt = domain.createdAt,
                isSyncedToOnlineDb = domain.isSyncedToOnlineDb
            )
        }
    }
}
