package com.example.onetomany.data.local

import androidx.room.Entity
import com.example.onetomany.data.model.SocialAccountCredential
import com.example.onetomany.data.model.SocialPlatform

@Entity(
    tableName = "social_credentials",
    primaryKeys = ["userId", "platformId"]
)
data class SocialCredentialEntity(
    val userId: String,
    val platformId: String,
    val accountHandle: String,
    val accessToken: String,
    val targetPageOrChannelId: String = "",
    val isConnected: Boolean = false,
    val lastVerifiedAt: Long = 0L,
    val errorMessage: String? = null
) {
    fun toDomain(): SocialAccountCredential = SocialAccountCredential(
        userId = userId,
        platform = SocialPlatform.fromId(platformId),
        accountHandle = accountHandle,
        accessToken = accessToken,
        targetPageOrChannelId = targetPageOrChannelId,
        isConnected = isConnected,
        lastVerifiedAt = lastVerifiedAt,
        errorMessage = errorMessage
    )

    companion object {
        fun fromDomain(domain: SocialAccountCredential): SocialCredentialEntity =
            SocialCredentialEntity(
                userId = domain.userId,
                platformId = domain.platform.id,
                accountHandle = domain.accountHandle,
                accessToken = domain.accessToken,
                targetPageOrChannelId = domain.targetPageOrChannelId,
                isConnected = domain.isConnected,
                lastVerifiedAt = domain.lastVerifiedAt,
                errorMessage = domain.errorMessage
            )
    }
}
