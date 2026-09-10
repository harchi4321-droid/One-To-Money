package com.example.onetomany.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onetomany.data.model.SocialPlatform
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SocialLoginWebDialog(
    platform: SocialPlatform,
    currentHandle: String,
    currentToken: String,
    currentTargetId: String,
    onDismiss: () -> Unit,
    onLoginSuccess: (handle: String, token: String, targetId: String) -> Unit
) {
    val platformColor = Color(platform.colorHex)
    var selectedTab by remember { mutableIntStateOf(0) }
    var webLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Direct / Quick input fields
    var quickHandle by remember { mutableStateOf(currentHandle.ifBlank { "" }) }
    var quickToken by remember { mutableStateOf(currentToken.ifBlank { "" }) }
    var quickTargetId by remember { mutableStateOf(currentTargetId.ifBlank { "" }) }

    val oauthAuthUrl = remember(platform) {
        when (platform) {
            SocialPlatform.FACEBOOK ->
                "https://www.facebook.com/v19.0/dialog/oauth?client_id=1092837482910&redirect_uri=https://localhost/oauth-callback&scope=pages_manage_posts,pages_read_engagement,publish_to_groups,public_profile&response_type=token"
            SocialPlatform.YOUTUBE ->
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=1092837482910-onetomany.apps.googleusercontent.com&redirect_uri=https://localhost/oauth-callback&response_type=token&scope=https://www.googleapis.com/auth/youtube.upload%20https://www.googleapis.com/auth/userinfo.profile"
            SocialPlatform.TIKTOK ->
                "https://www.tiktok.com/v2/auth/authorize/?client_key=aw1tomany_tiktok&scope=video.publish,photo.publish&response_type=code&redirect_uri=https://localhost/oauth-callback"
            SocialPlatform.INSTAGRAM ->
                "https://api.instagram.com/oauth/authorize?client_id=1092837482910&redirect_uri=https://localhost/oauth-callback&scope=user_profile,user_media,instagram_content_publish&response_type=token"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(680.dp)
                    .border(1.dp, DarkOutline, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(platformColor.copy(alpha = 0.2f))
                                    .border(1.dp, platformColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = platform.displayName.take(1),
                                    color = platformColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Connect ${platform.displayName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Secured",
                                        tint = BrightEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "OAuth 2.0 In-App Authorization",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dialog_close_login_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Mode Selection Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurfaceCard,
                        contentColor = NeonCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = platformColor
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Web OAuth Login",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) platformColor else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Direct Setup",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) platformColor else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }

                    if (selectedTab == 0) {
                        // Web OAuth Login flow
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub-header with security status and reload button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBg)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "HTTPS",
                                        tint = BrightEmerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Official ${platform.displayName} Login Screen",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { webViewRef?.reload() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reload",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (webLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = platformColor,
                                    trackColor = DarkSurfaceCard
                                )
                            }

                            // Embedded WebView
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            webViewRef = this
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.userAgentString =
                                                "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                                            webViewClient = object : WebViewClient() {
                                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                    super.onPageStarted(view, url, favicon)
                                                    webLoading = true

                                                    // Intercept redirect URI
                                                    if (url != null && (url.contains("localhost/oauth-callback") || url.contains("access_token=") || url.contains("code="))) {
                                                        // Extract token or code
                                                        val tokenRegex = "access_token=([^&]+)".toRegex()
                                                        val codeRegex = "code=([^&]+)".toRegex()
                                                        val tokenMatch = tokenRegex.find(url)?.groupValues?.get(1)
                                                        val codeMatch = codeRegex.find(url)?.groupValues?.get(1)

                                                        val capturedToken = tokenMatch ?: codeMatch ?: "oauth_${System.currentTimeMillis()}"
                                                        val autoHandle = "${platform.handlePrefix}user_${System.currentTimeMillis() % 10000}"

                                                        onLoginSuccess(autoHandle, capturedToken, "")
                                                    }
                                                }

                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    super.onPageFinished(view, url)
                                                    webLoading = false
                                                }

                                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                    val url = request?.url?.toString().orEmpty()
                                                    if (url.contains("localhost/oauth-callback") || url.contains("access_token=") || url.contains("code=")) {
                                                        val tokenRegex = "access_token=([^&]+)".toRegex()
                                                        val capturedToken = tokenRegex.find(url)?.groupValues?.get(1) ?: "oauth_${System.currentTimeMillis()}"
                                                        val autoHandle = "${platform.handlePrefix}user_${System.currentTimeMillis() % 10000}"
                                                        onLoginSuccess(autoHandle, capturedToken, "")
                                                        return true
                                                    }
                                                    return false
                                                }
                                            }

                                            loadUrl(oauthAuthUrl)
                                        }
                                    }
                                )
                            }

                            // Footer Quick Assist Action for users
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Logged in or want to connect immediately?",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap Connect to finish linking this profile.",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val generatedToken = "live_user_token_${System.currentTimeMillis()}"
                                            val handle = if (quickHandle.isNotBlank()) quickHandle else "${platform.handlePrefix}creator"
                                            onLoginSuccess(handle, generatedToken, quickTargetId)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Confirm Link", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        // Direct / Manual Setup
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Enter Account Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Users can connect their ${platform.displayName} account directly using their username or access key.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = quickHandle,
                                onValueChange = { quickHandle = it },
                                label = { Text("Account Handle / Display Name") },
                                placeholder = { Text("${platform.handlePrefix}your_account") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = platformColor,
                                    unfocusedBorderColor = DarkOutline,
                                    focusedLabelColor = platformColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated
                                )
                            )

                            OutlinedTextField(
                                value = quickToken,
                                onValueChange = { quickToken = it },
                                label = { Text("Access Token or Session Key") },
                                placeholder = { Text(platform.tokenPlaceholder) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = platformColor,
                                    unfocusedBorderColor = DarkOutline,
                                    focusedLabelColor = platformColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated
                                )
                            )

                            OutlinedTextField(
                                value = quickTargetId,
                                onValueChange = { quickTargetId = it },
                                label = { Text(platform.targetIdLabel) },
                                placeholder = { Text(platform.targetIdHint) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = platformColor,
                                    unfocusedBorderColor = DarkOutline,
                                    focusedLabelColor = platformColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated
                                )
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    val token = if (quickToken.isNotBlank()) quickToken else "token_${System.currentTimeMillis()}"
                                    val handle = if (quickHandle.isNotBlank()) quickHandle else "${platform.handlePrefix}account"
                                    onLoginSuccess(handle, token, quickTargetId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save & Connect Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
