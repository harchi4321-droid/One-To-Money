package com.example.onetomany.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onetomany.data.model.SocialAccountCredential
import com.example.onetomany.data.model.SocialPlatform
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(
    credentials: List<SocialAccountCredential>,
    testingPlatform: SocialPlatform?,
    onOpenWebLogin: (SocialPlatform) -> Unit,
    onSaveCredential: (platform: SocialPlatform, handle: String, token: String, targetId: String) -> Unit,
    onTestConnection: (platform: SocialPlatform) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("accounts_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Informational Security & Real API Broadcast Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Social Accounts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Log in with Facebook, YouTube (Google), TikTok, or Instagram. All connected accounts publish simultaneously with 1 click.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(SocialPlatform.entries, key = { it.id }) { platform ->
            val cred = credentials.find { it.platform == platform } ?: SocialAccountCredential(
                userId = "",
                platform = platform,
                accountHandle = "",
                accessToken = "",
                isConnected = false
            )

            AccountCredentialCard(
                platform = platform,
                credential = cred,
                isTesting = testingPlatform == platform,
                onOpenWebLogin = { onOpenWebLogin(platform) },
                onSave = { handle, token, targetId ->
                    onSaveCredential(platform, handle, token, targetId)
                },
                onTest = {
                    onTestConnection(platform)
                },
                onOpenPortal = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(platform.developerPortalUrl))
                        context.startActivity(intent)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountCredentialCard(
    platform: SocialPlatform,
    credential: SocialAccountCredential,
    isTesting: Boolean,
    onOpenWebLogin: () -> Unit,
    onSave: (handle: String, token: String, targetId: String) -> Unit,
    onTest: () -> Unit,
    onOpenPortal: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var handle by remember(credential.accountHandle) { mutableStateOf(credential.accountHandle) }
    var token by remember(credential.accessToken) { mutableStateOf(credential.accessToken) }
    var targetId by remember(credential.targetPageOrChannelId) { mutableStateOf(credential.targetPageOrChannelId) }
    var showToken by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val platformColor = Color(platform.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (credential.isConnected) 1.5.dp else 1.dp,
                color = if (credential.isConnected) platformColor.copy(alpha = 0.8f) else DarkOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("account_card_${platform.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Platform circular symbol
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(platformColor.copy(alpha = 0.2f))
                        .border(1.5.dp, platformColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = platform.displayName.take(2).uppercase(),
                        color = platformColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = platform.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (credential.isConnected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrightEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Connected",
                                        tint = BrightEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("Verified Live", color = BrightEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkOutline.copy(alpha = 0.4f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Not Connected", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    Text(
                        text = if (credential.accountHandle.isNotBlank()) credential.accountHandle
                        else if (credential.accessToken.isNotBlank()) "Token saved (tap to verify)"
                        else "No account logged in",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (credential.isConnected) BrightEmerald else TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }

            // Direct Social Log In Button for Users
            if (!credential.isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenWebLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (platform) {
                            SocialPlatform.YOUTUBE -> "Sign in with Google (YouTube)"
                            SocialPlatform.FACEBOOK -> "Log in with Facebook"
                            SocialPlatform.TIKTOK -> "Log in with TikTok"
                            SocialPlatform.INSTAGRAM -> "Log in with Instagram"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenWebLogin,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Account / Reconnect", fontSize = 11.sp, color = TextPrimary)
                    }

                    IconButton(
                        onClick = {
                            token = ""
                            handle = ""
                            targetId = ""
                            onSave("", "", "")
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, DarkOutline, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Disconnect",
                            tint = DangerCoral,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick Toggle for Manual Advanced Settings
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide manual API token settings ▲" else "Or configure API token manually ▾",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Expanded Edit Section
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    // Endpoint info & Portal Launcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = platform.apiEndpointDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Open Portal ↗",
                            color = platformColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onOpenPortal() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Required Scopes Chips
                    Text(
                        text = "Required Scopes: ${platform.requiredScopesSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Account Handle / Page name
                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it },
                        label = { Text("Account Handle / Display Name") },
                        placeholder = { Text(platform.handlePrefix + "channel_name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("handle_input_${platform.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platformColor,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Access Token with quick paste button
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Official Access Token / Bearer Key") },
                        placeholder = { Text(platform.tokenPlaceholder) },
                        singleLine = true,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        token = clip.trim()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste Token",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { showToken = !showToken }) {
                                    Icon(
                                        imageVector = if (showToken) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("token_input_${platform.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platformColor,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Page / Channel / Business ID tailored to platform
                    OutlinedTextField(
                        value = targetId,
                        onValueChange = { targetId = it },
                        label = { Text(platform.targetIdLabel) },
                        placeholder = { Text(platform.targetIdHint) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_id_input_${platform.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = platformColor,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // How to get token guide expander
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showGuide = !showGuide }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Text("Step-by-step guide for ${platform.displayName}", color = NeonCyan, fontSize = 12.sp)
                    }

                    AnimatedVisibility(visible = showGuide) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = platform.tokenSetupGuide,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = onOpenPortal,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open ${platform.displayName} Developer Portal", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (credential.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = DangerCoral, modifier = Modifier.size(16.dp))
                            Text(
                                text = credential.errorMessage,
                                color = DangerCoral,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Test Connection, Save, and Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (token.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    token = ""
                                    handle = ""
                                    targetId = ""
                                    onSave("", "", "")
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceCard)
                                    .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear Token",
                                    tint = DangerCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onTest,
                            enabled = !isTesting && token.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_btn_${platform.id}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = platformColor)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verifying...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Connection", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onSave(handle, token, targetId) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_btn_${platform.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

