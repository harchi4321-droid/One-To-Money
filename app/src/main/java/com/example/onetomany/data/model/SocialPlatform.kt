package com.example.onetomany.data.model

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val handlePrefix: String,
    val colorHex: Long,
    val maxCharLimit: Int,
    val isMediaPreferred: Boolean,
    val apiEndpointDescription: String,
    val developerPortalUrl: String,
    val tokenPlaceholder: String,
    val targetIdLabel: String,
    val targetIdHint: String,
    val requiredScopesSummary: String,
    val tokenSetupGuide: String
) {
    FACEBOOK(
        id = "facebook",
        displayName = "Facebook",
        handlePrefix = "fb.com/",
        colorHex = 0xFF1877F2,
        maxCharLimit = 63206,
        isMediaPreferred = false,
        apiEndpointDescription = "Meta Graph API v19.0 (Feed / Pages)",
        developerPortalUrl = "https://developers.facebook.com/tools/explorer/",
        tokenPlaceholder = "EAAB...",
        targetIdLabel = "Facebook Page ID or 'me'",
        targetIdHint = "Page ID (e.g. 10928374829) or 'me' for personal timeline",
        requiredScopesSummary = "pages_manage_posts, pages_read_engagement, publish_to_groups",
        tokenSetupGuide = "1. Go to Meta for Developers > Graph API Explorer.\n2. Select your Meta App and generate a User or Page Access Token.\n3. Include 'pages_manage_posts' and 'pages_read_engagement' permissions.\n4. If posting to a Page, copy the Page Access Token and paste it below."
    ),
    YOUTUBE(
        id = "youtube",
        displayName = "YouTube",
        handlePrefix = "@channel",
        colorHex = 0xFFFF0000,
        maxCharLimit = 5000,
        isMediaPreferred = true,
        apiEndpointDescription = "YouTube Data API v3 (Uploads & Community)",
        developerPortalUrl = "https://console.cloud.google.com/apis/credentials",
        tokenPlaceholder = "ya29.a0A...",
        targetIdLabel = "Target Channel ID (Optional)",
        targetIdHint = "e.g. UC_mine or numeric channel ID",
        requiredScopesSummary = "https://www.googleapis.com/auth/youtube.upload",
        tokenSetupGuide = "1. In Google Cloud Console, enable 'YouTube Data API v3'.\n2. Under APIs & Services > Credentials, create an OAuth 2.0 Client ID or Bearer Access Token.\n3. Request the 'https://www.googleapis.com/auth/youtube.upload' scope.\n4. Paste the Bearer token below."
    ),
    TIKTOK(
        id = "tiktok",
        displayName = "TikTok",
        handlePrefix = "@tiktok",
        colorHex = 0xFFFE2C55,
        maxCharLimit = 2200,
        isMediaPreferred = true,
        apiEndpointDescription = "TikTok Content Posting API v2",
        developerPortalUrl = "https://developers.tiktok.com/",
        tokenPlaceholder = "act.example_token...",
        targetIdLabel = "Open ID / Creator Account",
        targetIdHint = "e.g. open_id or creator user identifier",
        requiredScopesSummary = "video.publish, photo.publish",
        tokenSetupGuide = "1. Register in TikTok for Developers portal.\n2. Create an App with Content Posting API v2 enabled.\n3. Request 'video.publish' & 'photo.publish' permissions.\n4. Obtain the user access token and paste it here."
    ),
    INSTAGRAM(
        id = "instagram",
        displayName = "Instagram",
        handlePrefix = "@",
        colorHex = 0xFFE1306C,
        maxCharLimit = 2200,
        isMediaPreferred = true,
        apiEndpointDescription = "Instagram Graph API (Media & Reels)",
        developerPortalUrl = "https://developers.facebook.com/docs/instagram-api/",
        tokenPlaceholder = "IGQ...",
        targetIdLabel = "Instagram Business Account ID",
        targetIdHint = "Numeric ID e.g. 17841400123456789",
        requiredScopesSummary = "instagram_content_publish, instagram_basic",
        tokenSetupGuide = "1. Connect your Instagram Professional/Creator account to your Facebook Page.\n2. In Meta for Developers Graph API Explorer, obtain an Access Token.\n3. Add 'instagram_content_publish' and 'instagram_basic' permissions.\n4. Enter your Instagram Business Account ID in the field below."
    );

    companion object {
        fun fromId(id: String): SocialPlatform {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: FACEBOOK
        }
    }
}
