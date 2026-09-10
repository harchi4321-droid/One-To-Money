package com.example.onetomany.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.onetomany.data.model.DeliveryState
import com.example.onetomany.data.model.PlatformDeliveryResult
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

@Composable
fun DeliveryStatusDialog(
    isBroadcasting: Boolean,
    progressMap: Map<SocialPlatform, PlatformDeliveryResult>?,
    onDismiss: () -> Unit,
    onRetryFailed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val results = progressMap?.values?.toList() ?: emptyList()
    val hasFailures = results.any { it.state == DeliveryState.FAILED }
    val allCompleted = results.isNotEmpty() && results.all { it.state == DeliveryState.SUCCESS || it.state == DeliveryState.FAILED }

    Dialog(onDismissRequest = {
        if (!isBroadcasting) onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkOutline, RoundedCornerShape(24.dp))
                .testTag("delivery_status_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBroadcasting) "Broadcasting 1 To Many..." else "Broadcast Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBroadcasting) "Dispatching payload simultaneously" else "Channels delivery summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (!isBroadcasting) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_delivery_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonCyan,
                            strokeWidth = 2.5.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of platforms
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results, key = { it.platform.id }) { result ->
                        PlatformResultItem(result = result, onOpenUrl = { url ->
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasFailures && !isBroadcasting && onRetryFailed != null) {
                        OutlinedButton(
                            onClick = onRetryFailed,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retry_failed_platforms_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Failed")
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        enabled = !isBroadcasting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_delivery_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBroadcasting) "Broadcasting..." else "Done",
                            color = DarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformResultItem(
    result: PlatformDeliveryResult,
    onOpenUrl: (String) -> Unit
) {
    val platformColor = Color(result.platform.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Platform badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(platformColor.copy(alpha = 0.2f))
                    .border(1.5.dp, platformColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.platform.displayName.take(2).uppercase(),
                    color = platformColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = result.platform.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    if (result.responseCode > 0) {
                        Text(
                            text = "(${result.responseCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = result.responseMessage.ifBlank {
                        when (result.state) {
                            DeliveryState.IN_PROGRESS -> "Delivering post..."
                            DeliveryState.SUCCESS -> "Delivered successfully"
                            DeliveryState.FAILED -> "Delivery failed"
                            else -> "Waiting"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (result.state) {
                        DeliveryState.SUCCESS -> BrightEmerald
                        DeliveryState.FAILED -> DangerCoral
                        else -> TextSecondary
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status Icon or Action
            when (result.state) {
                DeliveryState.IN_PROGRESS -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = platformColor
                    )
                }
                DeliveryState.SUCCESS -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BrightEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        if (!result.externalUrl.isNullOrBlank()) {
                            IconButton(
                                onClick = { onOpenUrl(result.externalUrl) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open post",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                DeliveryState.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = DangerCoral,
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Text("...", color = TextSecondary)
                }
            }
        }
    }
}
