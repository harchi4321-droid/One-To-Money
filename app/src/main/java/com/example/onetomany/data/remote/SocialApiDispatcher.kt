package com.example.onetomany.data.remote

import com.example.onetomany.data.model.DeliveryState
import com.example.onetomany.data.model.PlatformDeliveryResult
import com.example.onetomany.data.model.SocialAccountCredential
import com.example.onetomany.data.model.SocialPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SocialApiDispatcher {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(credential: SocialAccountCredential): Result<String> = withContext(Dispatchers.IO) {
        if (credential.accessToken.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Access token is empty"))
        }

        try {
            val request = when (credential.platform) {
                SocialPlatform.FACEBOOK -> {
                    val target = credential.targetPageOrChannelId.ifBlank { "me" }
                    Request.Builder()
                        .url("https://graph.facebook.com/v19.0/$target?fields=id,name&access_token=${credential.accessToken}")
                        .get()
                        .build()
                }
                SocialPlatform.INSTAGRAM -> {
                    val igTarget = credential.targetPageOrChannelId.ifBlank { "me" }
                    Request.Builder()
                        .url("https://graph.facebook.com/v19.0/$igTarget?fields=id,username,name&access_token=${credential.accessToken}")
                        .get()
                        .build()
                }
                SocialPlatform.YOUTUBE -> {
                    Request.Builder()
                        .url("https://youtube.googleapis.com/youtube/v3/channels?part=snippet&mine=true")
                        .addHeader("Authorization", "Bearer ${credential.accessToken}")
                        .get()
                        .build()
                }
                SocialPlatform.TIKTOK -> {
                    Request.Builder()
                        .url("https://open.tiktokapis.com/v2/user/info/?fields=open_id,union_id,avatar_url,display_name")
                        .addHeader("Authorization", "Bearer ${credential.accessToken}")
                        .get()
                        .build()
                }
            }

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                var accountName = credential.accountHandle.ifBlank { credential.platform.displayName }
                try {
                    val json = JSONObject(responseBody)
                    if (json.has("name")) accountName = json.getString("name")
                    if (json.has("username")) accountName = "@" + json.getString("username")
                    if (json.has("data") && json.getJSONObject("data").has("user") && json.getJSONObject("data").getJSONObject("user").has("display_name")) {
                        accountName = json.getJSONObject("data").getJSONObject("user").getString("display_name")
                    } else if (json.has("data") && json.getJSONObject("data").has("display_name")) {
                        accountName = json.getJSONObject("data").getString("display_name")
                    }
                    if (json.has("items")) {
                        val items = json.getJSONArray("items")
                        if (items.length() > 0) {
                            accountName = items.getJSONObject(0).getJSONObject("snippet").getString("title")
                        }
                    }
                } catch (_: Exception) {}
                Result.success("Connected: $accountName")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    if (json.has("error")) {
                        val errObj = json.get("error")
                        if (errObj is JSONObject) errObj.optString("message", "API Error (${response.code})")
                        else errObj.toString()
                    } else if (json.has("detail")) {
                        json.getString("detail")
                    } else {
                        "HTTP ${response.code}: ${response.message}"
                    }
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishPost(
        credential: SocialAccountCredential,
        title: String,
        content: String,
        mediaUrl: String,
        hashtags: List<String>
    ): PlatformDeliveryResult = withContext(Dispatchers.IO) {
        val platform = credential.platform

        if (credential.accessToken.isBlank()) {
            return@withContext PlatformDeliveryResult(
                platform = platform,
                state = DeliveryState.FAILED,
                responseCode = 401,
                responseMessage = "Token missing. Please configure credentials in Accounts tab.",
                completedAt = System.currentTimeMillis()
            )
        }

        val fullText = buildString {
            if (title.isNotBlank()) append(title).append("\n\n")
            append(content)
            if (hashtags.isNotEmpty()) {
                append("\n\n")
                append(hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" })
            }
        }

        try {
            when (platform) {
                SocialPlatform.FACEBOOK -> publishToFacebook(credential, fullText, mediaUrl)
                SocialPlatform.INSTAGRAM -> publishToInstagram(credential, fullText, mediaUrl)
                SocialPlatform.YOUTUBE -> publishToYouTube(credential, title, content, mediaUrl)
                SocialPlatform.TIKTOK -> publishToTikTok(credential, fullText, mediaUrl)
            }
        } catch (e: Exception) {
            PlatformDeliveryResult(
                platform = platform,
                state = DeliveryState.FAILED,
                responseCode = -1,
                responseMessage = e.localizedMessage ?: "Network connection error",
                completedAt = System.currentTimeMillis()
            )
        }
    }

    private fun publishToFacebook(
        credential: SocialAccountCredential,
        message: String,
        mediaUrl: String
    ): PlatformDeliveryResult {
        val targetId = credential.targetPageOrChannelId.ifBlank { "me" }
        val url = "https://graph.facebook.com/v19.0/$targetId/feed"

        val bodyJson = JSONObject().apply {
            put("message", message)
            if (mediaUrl.isNotBlank()) {
                put("link", mediaUrl)
            }
            put("access_token", credential.accessToken)
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        val resBody = response.body?.string().orEmpty()

        return if (response.isSuccessful) {
            val json = JSONObject(resBody)
            val postId = json.optString("id", System.currentTimeMillis().toString())
            PlatformDeliveryResult(
                platform = SocialPlatform.FACEBOOK,
                state = DeliveryState.SUCCESS,
                responseCode = response.code,
                responseMessage = "Published successfully to Facebook Feed",
                externalPostId = postId,
                externalUrl = "https://facebook.com/$postId",
                completedAt = System.currentTimeMillis()
            )
        } else {
            val msg = parseApiErrorMessage(resBody, response.code)
            PlatformDeliveryResult(
                platform = SocialPlatform.FACEBOOK,
                state = DeliveryState.FAILED,
                responseCode = response.code,
                responseMessage = msg,
                completedAt = System.currentTimeMillis()
            )
        }
    }

    private fun publishToInstagram(
        credential: SocialAccountCredential,
        caption: String,
        mediaUrl: String
    ): PlatformDeliveryResult {
        val igUserId = credential.targetPageOrChannelId.ifBlank { "me" }
        // Step 1: Create media container
        val createMediaUrl = "https://graph.facebook.com/v19.0/$igUserId/media"
        val createJson = JSONObject().apply {
            put("caption", caption)
            if (mediaUrl.isNotBlank()) {
                put("image_url", mediaUrl)
            }
            put("access_token", credential.accessToken)
        }

        val request = Request.Builder()
            .url(createMediaUrl)
            .post(createJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        val resBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            return PlatformDeliveryResult(
                platform = SocialPlatform.INSTAGRAM,
                state = DeliveryState.FAILED,
                responseCode = response.code,
                responseMessage = parseApiErrorMessage(resBody, response.code),
                completedAt = System.currentTimeMillis()
            )
        }

        val creationId = JSONObject(resBody).optString("id")

        // Step 2: Publish media
        val publishUrl = "https://graph.facebook.com/v19.0/$igUserId/media_publish"
        val publishJson = JSONObject().apply {
            put("creation_id", creationId)
            put("access_token", credential.accessToken)
        }

        val pubRequest = Request.Builder()
            .url(publishUrl)
            .post(publishJson.toString().toRequestBody(jsonMediaType))
            .build()

        val pubResponse = httpClient.newCall(pubRequest).execute()
        val pubBody = pubResponse.body?.string().orEmpty()

        return if (pubResponse.isSuccessful) {
            val publishedId = JSONObject(pubBody).optString("id", creationId)
            PlatformDeliveryResult(
                platform = SocialPlatform.INSTAGRAM,
                state = DeliveryState.SUCCESS,
                responseCode = pubResponse.code,
                responseMessage = "Post published successfully to Instagram",
                externalPostId = publishedId,
                externalUrl = "https://instagram.com/p/$publishedId",
                completedAt = System.currentTimeMillis()
            )
        } else {
            PlatformDeliveryResult(
                platform = SocialPlatform.INSTAGRAM,
                state = DeliveryState.FAILED,
                responseCode = pubResponse.code,
                responseMessage = parseApiErrorMessage(pubBody, pubResponse.code),
                completedAt = System.currentTimeMillis()
            )
        }
    }

    private fun publishToYouTube(
        credential: SocialAccountCredential,
        title: String,
        content: String,
        mediaUrl: String
    ): PlatformDeliveryResult {
        // Broadcast to YouTube channel via Data API v3
        val url = "https://youtube.googleapis.com/youtube/v3/commentThreads?part=snippet"
        val snippetJson = JSONObject().apply {
            put("channelId", credential.targetPageOrChannelId.ifBlank { "UC_mine" })
            put("topLevelComment", JSONObject().apply {
                put("snippet", JSONObject().apply {
                    put("textOriginal", if (title.isNotBlank()) "$title\n\n$content" else content)
                })
            })
        }
        val postJson = JSONObject().apply {
            put("snippet", snippetJson)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(postJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        val resBody = response.body?.string().orEmpty()

        return if (response.isSuccessful) {
            val json = JSONObject(resBody)
            val pubId = json.optString("id", System.currentTimeMillis().toString())
            PlatformDeliveryResult(
                platform = SocialPlatform.YOUTUBE,
                state = DeliveryState.SUCCESS,
                responseCode = response.code,
                responseMessage = "Posted to YouTube Channel Community Feed",
                externalPostId = pubId,
                externalUrl = "https://youtube.com",
                completedAt = System.currentTimeMillis()
            )
        } else {
            PlatformDeliveryResult(
                platform = SocialPlatform.YOUTUBE,
                state = DeliveryState.FAILED,
                responseCode = response.code,
                responseMessage = parseApiErrorMessage(resBody, response.code),
                completedAt = System.currentTimeMillis()
            )
        }
    }

    private fun publishToTikTok(
        credential: SocialAccountCredential,
        text: String,
        mediaUrl: String
    ): PlatformDeliveryResult {
        val url = "https://open.tiktokapis.com/v2/post/publish/content/init/"
        val bodyJson = JSONObject().apply {
            put("post_info", JSONObject().apply {
                put("title", text.take(150))
                put("description", text)
                put("privacy_level", "PUBLIC_TO_EVERYONE")
            })
            if (mediaUrl.isNotBlank()) {
                put("source_info", JSONObject().apply {
                    put("source", "PULL_FROM_URL")
                    put("photo_cover_index", 1)
                    put("photo_images", org.json.JSONArray().apply { put(mediaUrl) })
                })
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${credential.accessToken}")
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        val resBody = response.body?.string().orEmpty()

        return if (response.isSuccessful) {
            val publishId = runCatching {
                JSONObject(resBody).getJSONObject("data").getString("publish_id")
            }.getOrDefault(System.currentTimeMillis().toString())

            PlatformDeliveryResult(
                platform = SocialPlatform.TIKTOK,
                state = DeliveryState.SUCCESS,
                responseCode = response.code,
                responseMessage = "Published to TikTok account successfully",
                externalPostId = publishId,
                externalUrl = "https://www.tiktok.com",
                completedAt = System.currentTimeMillis()
            )
        } else {
            PlatformDeliveryResult(
                platform = SocialPlatform.TIKTOK,
                state = DeliveryState.FAILED,
                responseCode = response.code,
                responseMessage = parseApiErrorMessage(resBody, response.code),
                completedAt = System.currentTimeMillis()
            )
        }
    }

    private fun parseApiErrorMessage(body: String, statusCode: Int): String {
        return try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val err = json.get("error")
                if (err is JSONObject) {
                    err.optString("message", "API Error ($statusCode)")
                } else err.toString()
            } else if (json.has("detail")) {
                json.getString("detail")
            } else if (json.has("message")) {
                json.getString("message")
            } else {
                "HTTP $statusCode: Error received from social network"
            }
        } catch (_: Exception) {
            "HTTP $statusCode: Failed to deliver to social platform"
        }
    }
}
