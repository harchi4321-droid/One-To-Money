package com.example.onetomany.data.model

enum class BroadcastOverallStatus {
    DRAFT,
    PUBLISHING,
    SUCCESS_ALL,
    PARTIAL_SUCCESS,
    FAILED
}

enum class DeliveryState {
    IDLE,
    WAITING,
    IN_PROGRESS,
    SUCCESS,
    FAILED
}

data class PlatformDeliveryResult(
    val platform: SocialPlatform,
    val state: DeliveryState = DeliveryState.IDLE,
    val responseCode: Int = 0,
    val responseMessage: String = "",
    val externalPostId: String? = null,
    val externalUrl: String? = null,
    val completedAt: Long = 0L
)

data class BroadcastPost(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val mediaUrl: String = "",
    val hashtags: List<String> = emptyList(),
    val targetPlatforms: List<SocialPlatform> = SocialPlatform.entries,
    val deliveryResults: Map<SocialPlatform, PlatformDeliveryResult> = emptyMap(),
    val overallStatus: BroadcastOverallStatus = BroadcastOverallStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedToOnlineDb: Boolean = false
) {
    val fullComposedText: String
        get() {
            val tagsSuffix = if (hashtags.isNotEmpty()) {
                "\n\n" + hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
            } else ""
            return (if (title.isNotBlank()) "$title\n\n$content" else content) + tagsSuffix
        }
}
