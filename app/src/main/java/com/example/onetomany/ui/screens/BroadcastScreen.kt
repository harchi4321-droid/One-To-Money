package com.example.onetomany.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.model.User
import com.example.onetomany.data.remote.OnlineDbStatus
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastScreen(
    currentUser: User?,
    onlineDbStatus: OnlineDbStatus,
    postTitle: String,
    postContent: String,
    postMediaUrl: String,
    hashtagInput: String,
    hashtags: List<String>,
    selectedPlatforms: Set<SocialPlatform>,
    activePreviewTab: SocialPlatform,
    isBroadcasting: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onMediaUrlChange: (String) -> Unit,
    onHashtagInputChange: (String) -> Unit,
    onAddHashtag: () -> Unit,
    onRemoveHashtag: (String) -> Unit,
    onTogglePlatform: (SocialPlatform) -> Unit,
    onSelectAllPlatforms: () -> Unit,
    onPreviewTabChange: (SocialPlatform) -> Unit,
    onBroadcastSubmit: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    onOpenOnlineDbDialog: () -> Unit
) {
    val totalChars = remember(postTitle, postContent, hashtags) {
        val tagsLen = if (hashtags.isNotEmpty()) hashtags.sumOf { it.length + 2 } + 2 else 0
        val titleLen = if (postTitle.isNotBlank()) postTitle.length + 2 else 0
        titleLen + postContent.length + tagsLen
    }

    val exceedsShortFormLimit = (selectedPlatforms.contains(SocialPlatform.TIKTOK) || selectedPlatforms.contains(SocialPlatform.INSTAGRAM)) && totalChars > 2200

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("broadcast_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User & Online DB Bar
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Chip
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenAuthDialog() }
                        .testTag("current_user_profile_chip"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = currentUser?.displayName ?: "Broadcaster",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Online DB indicator chip
                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenOnlineDbDialog() }
                        .testTag("online_db_indicator_chip"),
                    colors = CardDefaults.cardColors(
                        containerColor = when (onlineDbStatus) {
                            is OnlineDbStatus.Connected -> BrightEmerald.copy(alpha = 0.15f)
                            is OnlineDbStatus.Connecting -> NeonCyan.copy(alpha = 0.15f)
                            is OnlineDbStatus.OfflineOrStandby -> DarkSurfaceCard
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (onlineDbStatus is OnlineDbStatus.Connected) BrightEmerald else DarkOutline
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Online DB",
                            tint = if (onlineDbStatus is OnlineDbStatus.Connected) BrightEmerald else NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (onlineDbStatus is OnlineDbStatus.Connected) "Online DB Live" else "Cloud Sync",
                            color = if (onlineDbStatus is OnlineDbStatus.Connected) BrightEmerald else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section: Select 5 Social Networks
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Channels (${selectedPlatforms.size}/${SocialPlatform.entries.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    TextButton(
                        onClick = onSelectAllPlatforms,
                        modifier = Modifier.testTag("select_all_channels_btn")
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All ${SocialPlatform.entries.size} Channels", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5 Platform Horizontal selector
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SocialPlatform.entries, key = { it.id }) { platform ->
                        val isSelected = selectedPlatforms.contains(platform)
                        val platformColor = Color(platform.colorHex)

                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onTogglePlatform(platform) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) platformColor else DarkOutline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("platform_chip_${platform.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) platformColor.copy(alpha = 0.15f) else DarkSurfaceCard
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) platformColor else DarkSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    } else {
                                        Text(
                                            text = platform.displayName.take(1),
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = platform.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Post Composer Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkOutline, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1 To Many Broadcast Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Post Title / Subject
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = onTitleChange,
                        label = { Text("Title / Headline (Optional)") },
                        placeholder = { Text("e.g. Major Product Launch Announcement!") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Main Content TextField
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = onContentChange,
                        label = { Text("What do you want to broadcast?") },
                        placeholder = { Text("Write your message to post simultaneously across Facebook, YouTube, TikTok, X, and Instagram...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("composer_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Character counters & constraint indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (exceedsShortFormLimit) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = DangerCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exceeds TikTok/IG limit (max 2200)!",
                                    color = DangerCoral,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "$totalChars characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (exceedsShortFormLimit) DangerCoral else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    // Media Link URL input
                    OutlinedTextField(
                        value = postMediaUrl,
                        onValueChange = onMediaUrlChange,
                        label = { Text("Media / Photo / Video URL (Optional)") },
                        placeholder = { Text("https://example.com/photo.jpg") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_media_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Media preview if URL provided
                    if (postMediaUrl.isNotBlank()) {
                        AsyncImage(
                            model = postMediaUrl,
                            contentDescription = "Media Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceCard),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    // Hashtags Section
                    Column {
                        Text(
                            text = "Hashtags",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hashtags.forEach { tag ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("#$tag", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { onRemoveHashtag(tag) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hashtagInput,
                                onValueChange = onHashtagInputChange,
                                placeholder = { Text("Add tag...") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("hashtag_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = DarkOutline,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Button(
                                onClick = onAddHashtag,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("add_hashtag_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = NeonCyan)
                            }
                        }
                    }
                }
            }
        }

        // Section: Live Social Network Previews
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DarkOutline, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Feed Format Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ScrollableTabRow(
                        selectedTabIndex = SocialPlatform.entries.indexOf(activePreviewTab),
                        containerColor = DarkBg,
                        contentColor = NeonCyan,
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            val index = SocialPlatform.entries.indexOf(activePreviewTab)
                            if (index in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[index]),
                                    color = Color(activePreviewTab.colorHex)
                                )
                            }
                        }
                    ) {
                        SocialPlatform.entries.forEach { platform ->
                            Tab(
                                selected = activePreviewTab == platform,
                                onClick = { onPreviewTabChange(platform) },
                                text = {
                                    Text(
                                        text = platform.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (activePreviewTab == platform) Color(platform.colorHex) else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feed card simulation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(activePreviewTab.colorHex).copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(activePreviewTab.colorHex).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activePreviewTab.displayName.take(1),
                                        color = Color(activePreviewTab.colorHex),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = currentUser?.displayName ?: "Broadcaster",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = activePreviewTab.handlePrefix + (currentUser?.displayName?.lowercase()?.replace(" ", "") ?: "creator"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (postTitle.isNotBlank()) {
                                Text(
                                    text = postTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = postContent.ifBlank { "Live preview of your multi-channel post will appear here..." },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (postContent.isBlank()) TextSecondary else TextPrimary,
                                lineHeight = 16.sp
                            )

                            if (hashtags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = hashtags.joinToString(" ") { "#$it" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan,
                                    fontSize = 11.sp
                                )
                            }

                            if (postMediaUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = postMediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Main 1 To Many Broadcast Button
        item {
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onBroadcastSubmit,
                enabled = !isBroadcasting && (postContent.isNotBlank() || postTitle.isNotBlank()) && selectedPlatforms.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("broadcast_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Broadcast",
                        tint = DarkBg,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBroadcasting) "Broadcasting Payload..."
                        else "Broadcast 1 To Many (${selectedPlatforms.size} Channels)",
                        color = DarkBg,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
