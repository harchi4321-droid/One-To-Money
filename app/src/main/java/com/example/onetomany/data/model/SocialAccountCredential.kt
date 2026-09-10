package com.example.onetomany.data.model

data class SocialAccountCredential(
    val userId: String,
    val platform: SocialPlatform,
    val accountHandle: String,
    val accessToken: String,
    val targetPageOrChannelId: String = "",
    val isConnected: Boolean = false,
    val lastVerifiedAt: Long = 0L,
    val errorMessage: String? = null
) {
    val maskedToken: String
        get() {
            if (accessToken.isBlank()) return "Not configured"
            if (accessToken.length <= 8) return "••••••••"
            return "${accessToken.take(4)}••••••••${accessToken.takeLast(4)}"
        }
}
