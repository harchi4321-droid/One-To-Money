package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.onetomany.data.remote.OnlineDbStatus
import com.example.onetomany.ui.MainViewModel
import com.example.onetomany.ui.screens.AccountsScreen
import com.example.onetomany.ui.screens.AuthDialog
import com.example.onetomany.ui.screens.BroadcastScreen
import com.example.onetomany.ui.screens.DeliveryStatusDialog
import com.example.onetomany.ui.screens.HistoryScreen
import com.example.onetomany.ui.screens.OnlineDbSettingsDialog
import com.example.onetomany.ui.screens.SocialLoginWebDialog
import com.example.ui.theme.BrightEmerald
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoNotice) {
        uiState.infoNotice?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearNotice()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it", duration = SnackbarDuration.Long)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(DarkBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Logo & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "1 To Many Logo",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "1 To Many",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "4 CHANNELS",
                                        color = NeonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = "FB • YouTube • TikTok • Instagram",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Top Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Online DB Status Icon
                        IconButton(
                            onClick = { viewModel.openOnlineDbDialog() },
                            modifier = Modifier.testTag("top_online_db_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.onlineDbStatus is OnlineDbStatus.Connected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                contentDescription = "Cloud Database",
                                tint = if (uiState.onlineDbStatus is OnlineDbStatus.Connected) BrightEmerald else TextSecondary
                            )
                        }

                        // User profile switcher icon
                        IconButton(
                            onClick = { viewModel.openAuthDialog() },
                            modifier = Modifier.testTag("top_auth_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkOutline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Profile",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Compose") },
                    label = { Text("Compose", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_compose")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = "Social Accounts") },
                    label = { Text("Accounts", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBg,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_accounts")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    BroadcastScreen(
                        currentUser = uiState.currentUser,
                        onlineDbStatus = uiState.onlineDbStatus,
                        postTitle = uiState.postTitle,
                        postContent = uiState.postContent,
                        postMediaUrl = uiState.postMediaUrl,
                        hashtagInput = uiState.hashtagInput,
                        hashtags = uiState.hashtags,
                        selectedPlatforms = uiState.selectedPlatforms,
                        activePreviewTab = uiState.activePreviewTab,
                        isBroadcasting = uiState.isBroadcasting,
                        onTitleChange = viewModel::updateTitle,
                        onContentChange = viewModel::updateContent,
                        onMediaUrlChange = viewModel::updateMediaUrl,
                        onHashtagInputChange = viewModel::updateHashtagInput,
                        onAddHashtag = viewModel::addHashtag,
                        onRemoveHashtag = viewModel::removeHashtag,
                        onTogglePlatform = viewModel::togglePlatform,
                        onSelectAllPlatforms = viewModel::selectAllPlatforms,
                        onPreviewTabChange = viewModel::setActivePreviewTab,
                        onBroadcastSubmit = viewModel::broadcastToAllSelected,
                        onOpenAuthDialog = viewModel::openAuthDialog,
                        onOpenOnlineDbDialog = viewModel::openOnlineDbDialog
                    )
                }
                1 -> {
                    HistoryScreen(
                        posts = uiState.historyPosts,
                        onRetryPost = viewModel::retryFailedPlatforms,
                        onDeletePost = viewModel::deletePost
                    )
                }
                2 -> {
                    AccountsScreen(
                        credentials = uiState.accountCredentials,
                        testingPlatform = uiState.testingPlatform,
                        onOpenWebLogin = viewModel::openSocialWebLogin,
                        onSaveCredential = viewModel::updateCredential,
                        onTestConnection = viewModel::testAccountConnection
                    )
                }
            }

            // Dialogs
            uiState.socialWebLoginPlatform?.let { platform ->
                val cred = uiState.accountCredentials.find { it.platform == platform }
                SocialLoginWebDialog(
                    platform = platform,
                    currentHandle = cred?.accountHandle.orEmpty(),
                    currentToken = cred?.accessToken.orEmpty(),
                    currentTargetId = cred?.targetPageOrChannelId.orEmpty(),
                    onDismiss = viewModel::closeSocialWebLogin,
                    onLoginSuccess = { handle, token, targetId ->
                        viewModel.completeSocialLogin(platform, handle, token, targetId)
                    }
                )
            }
            if (uiState.showDeliveryStatusDialog) {
                DeliveryStatusDialog(
                    isBroadcasting = uiState.isBroadcasting,
                    progressMap = uiState.activeDeliveryProgress,
                    onDismiss = viewModel::dismissDeliveryDialog,
                    onRetryFailed = {
                        val lastPost = uiState.historyPosts.firstOrNull()
                        if (lastPost != null) {
                            viewModel.retryFailedPlatforms(lastPost)
                        }
                    }
                )
            }

            if (uiState.isAuthDialogOpen) {
                AuthDialog(
                    currentUser = uiState.currentUser,
                    allUsers = uiState.allUsers,
                    onDismiss = viewModel::closeAuthDialog,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onSwitchUser = viewModel::switchUser,
                    onLogout = viewModel::logout
                )
            }

            if (uiState.isOnlineDbDialogOpen) {
                OnlineDbSettingsDialog(
                    status = uiState.onlineDbStatus,
                    onDismiss = viewModel::closeOnlineDbDialog,
                    onRefreshStatus = viewModel::refreshOnlineDbStatus,
                    onSaveSettings = viewModel::updateOnlineDbEndpoint
                )
            }
        }
    }
}
