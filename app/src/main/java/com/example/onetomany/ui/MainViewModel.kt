package com.example.onetomany.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetomany.data.local.AppDatabase
import com.example.onetomany.data.model.BroadcastPost
import com.example.onetomany.data.model.PlatformDeliveryResult
import com.example.onetomany.data.model.SocialAccountCredential
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.model.User
import com.example.onetomany.data.remote.OnlineDatabaseService
import com.example.onetomany.data.remote.OnlineDbStatus
import com.example.onetomany.data.remote.SocialApiDispatcher
import com.example.onetomany.data.repository.AuthRepository
import com.example.onetomany.data.repository.BroadcastRepository
import com.example.onetomany.data.repository.SocialAccountsRepository
import com.example.onetomany.data.security.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val currentUser: User? = null,
    val allUsers: List<User> = emptyList(),
    val onlineDbStatus: OnlineDbStatus = OnlineDbStatus.Connecting,
    // Compose Form
    val postTitle: String = "",
    val postContent: String = "",
    val postMediaUrl: String = "",
    val hashtagInput: String = "",
    val hashtags: List<String> = listOf("1ToMany", "SocialBroadcaster"),
    val selectedPlatforms: Set<SocialPlatform> = SocialPlatform.entries.toSet(),
    val activePreviewTab: SocialPlatform = SocialPlatform.FACEBOOK,
    // Broadcasting In Flight
    val isBroadcasting: Boolean = false,
    val activeDeliveryProgress: Map<SocialPlatform, PlatformDeliveryResult>? = null,
    val showDeliveryStatusDialog: Boolean = false,
    // History
    val historyPosts: List<BroadcastPost> = emptyList(),
    // Accounts
    val accountCredentials: List<SocialAccountCredential> = emptyList(),
    val testingPlatform: SocialPlatform? = null,
    val socialWebLoginPlatform: SocialPlatform? = null,
    // Dialogs & Notification
    val isAuthDialogOpen: Boolean = false,
    val isOnlineDbDialogOpen: Boolean = false,
    val infoNotice: String? = null,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getInstance(application)
    private val onlineDatabaseService = OnlineDatabaseService(application, sessionManager)
    private val apiDispatcher = SocialApiDispatcher()

    private val authRepository = AuthRepository(
        database.userDao(),
        sessionManager,
        onlineDatabaseService
    )

    private val socialAccountsRepository = SocialAccountsRepository(
        database.socialCredentialDao(),
        apiDispatcher,
        onlineDatabaseService
    )

    private val broadcastRepository = BroadcastRepository(
        database.broadcastDao(),
        socialAccountsRepository,
        apiDispatcher,
        onlineDatabaseService
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var historyCollectionJob: Job? = null
    private var credentialsCollectionJob: Job? = null

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            // Check online cloud DB status
            refreshOnlineDbStatus()

            // Ensure active or default user
            val activeUser = authRepository.ensureDefaultUserIfEmpty()
            _uiState.update { it.copy(currentUser = activeUser) }

            // Observe all users
            launch {
                authRepository.getAllUsersFlow().collectLatest { users ->
                    _uiState.update { it.copy(allUsers = users) }
                }
            }

            // Bind user data
            bindUserData(activeUser.id)
        }
    }

    private fun bindUserData(userId: String) {
        historyCollectionJob?.cancel()
        credentialsCollectionJob?.cancel()

        historyCollectionJob = viewModelScope.launch {
            broadcastRepository.getBroadcastHistoryFlow(userId).collectLatest { posts ->
                _uiState.update { it.copy(historyPosts = posts) }
            }
        }

        credentialsCollectionJob = viewModelScope.launch {
            socialAccountsRepository.getCredentialsFlow(userId).collectLatest { creds ->
                _uiState.update { it.copy(accountCredentials = creds) }
            }
        }
    }

    fun refreshOnlineDbStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(onlineDbStatus = OnlineDbStatus.Connecting) }
            val status = onlineDatabaseService.checkConnection()
            _uiState.update { it.copy(onlineDbStatus = status) }
        }
    }

    // Composer interactions
    fun updateTitle(title: String) {
        _uiState.update { it.copy(postTitle = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(postContent = content) }
    }

    fun updateMediaUrl(url: String) {
        _uiState.update { it.copy(postMediaUrl = url) }
    }

    fun updateHashtagInput(text: String) {
        _uiState.update { it.copy(hashtagInput = text) }
    }

    fun addHashtag() {
        val raw = _uiState.value.hashtagInput.trim().removePrefix("#")
        if (raw.isNotBlank() && !_uiState.value.hashtags.contains(raw)) {
            _uiState.update {
                it.copy(
                    hashtags = it.hashtags + raw,
                    hashtagInput = ""
                )
            }
        }
    }

    fun removeHashtag(tag: String) {
        _uiState.update {
            it.copy(hashtags = it.hashtags.filterNot { it.equals(tag, ignoreCase = true) })
        }
    }

    fun togglePlatform(platform: SocialPlatform) {
        _uiState.update { state ->
            val current = state.selectedPlatforms
            val next = if (current.contains(platform)) {
                if (current.size > 1) current - platform else current // Keep at least one
            } else {
                current + platform
            }
            state.copy(selectedPlatforms = next)
        }
    }

    fun selectAllPlatforms() {
        _uiState.update { it.copy(selectedPlatforms = SocialPlatform.entries.toSet()) }
    }

    fun setActivePreviewTab(platform: SocialPlatform) {
        _uiState.update { it.copy(activePreviewTab = platform) }
    }

    // Simultaneous Broadcast 1 To Many
    fun broadcastToAllSelected() {
        val state = _uiState.value
        val user = state.currentUser ?: return
        if (state.postContent.isBlank() && state.postTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter some content or title to broadcast.") }
            return
        }
        if (state.selectedPlatforms.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one social platform.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBroadcasting = true,
                    showDeliveryStatusDialog = true,
                    errorMessage = null
                )
            }

            try {
                broadcastRepository.broadcastSimultaneously(
                    userId = user.id,
                    title = state.postTitle,
                    content = state.postContent,
                    mediaUrl = state.postMediaUrl,
                    hashtags = state.hashtags,
                    targetPlatforms = state.selectedPlatforms.toList(),
                    onProgressUpdate = { progressMap ->
                        _uiState.update { it.copy(activeDeliveryProgress = progressMap) }
                    }
                )

                _uiState.update {
                    it.copy(
                        isBroadcasting = false,
                        infoNotice = "Broadcast processed across selected channels!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBroadcasting = false,
                        errorMessage = "Broadcast failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun retryFailedPlatforms(post: BroadcastPost) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBroadcasting = true,
                    showDeliveryStatusDialog = true,
                    activeDeliveryProgress = post.deliveryResults
                )
            }

            try {
                broadcastRepository.retryFailedPlatforms(
                    post = post,
                    onProgressUpdate = { progressMap ->
                        _uiState.update { it.copy(activeDeliveryProgress = progressMap) }
                    }
                )
                _uiState.update {
                    it.copy(
                        isBroadcasting = false,
                        infoNotice = "Retry complete!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBroadcasting = false,
                        errorMessage = "Retry error: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissDeliveryDialog() {
        _uiState.update { it.copy(showDeliveryStatusDialog = false) }
    }

    // Social Accounts management
    fun openSocialWebLogin(platform: SocialPlatform) {
        _uiState.update { it.copy(socialWebLoginPlatform = platform) }
    }

    fun closeSocialWebLogin() {
        _uiState.update { it.copy(socialWebLoginPlatform = null) }
    }

    fun completeSocialLogin(platform: SocialPlatform, handle: String, token: String, targetId: String = "") {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            socialAccountsRepository.updateCredential(user.id, platform, handle, token, targetId)
            _uiState.update {
                it.copy(
                    socialWebLoginPlatform = null,
                    infoNotice = "Successfully connected ${platform.displayName} ($handle)!"
                )
            }
            // Auto test connection to verify
            testAccountConnection(platform)
        }
    }

    fun updateCredential(platform: SocialPlatform, handle: String, token: String, targetId: String = "") {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            socialAccountsRepository.updateCredential(user.id, platform, handle, token, targetId)
            _uiState.update { it.copy(infoNotice = "Saved credentials for ${platform.displayName}") }
        }
    }

    fun testAccountConnection(platform: SocialPlatform) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(testingPlatform = platform, errorMessage = null) }
            val result = socialAccountsRepository.testAndVerifyCredential(user.id, platform)
            _uiState.update {
                it.copy(
                    testingPlatform = null,
                    infoNotice = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // Auth actions
    fun register(email: String, name: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.registerUser(email, name, pass)
            result.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user, isAuthDialogOpen = false, infoNotice = "Welcome ${user.displayName}!") }
                bindUserData(user.id)
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message) }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.loginUser(email, pass)
            result.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user, isAuthDialogOpen = false, infoNotice = "Signed in as ${user.displayName}") }
                bindUserData(user.id)
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message) }
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            val result = authRepository.switchUser(userId)
            result.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user, isAuthDialogOpen = false, infoNotice = "Switched to ${user.displayName}") }
                bindUserData(user.id)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(currentUser = null, isAuthDialogOpen = true) }
        }
    }

    fun openAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = true) }
    }

    fun closeAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = false) }
    }

    fun openOnlineDbDialog() {
        _uiState.update { it.copy(isOnlineDbDialogOpen = true) }
    }

    fun closeOnlineDbDialog() {
        _uiState.update { it.copy(isOnlineDbDialogOpen = false) }
    }

    fun updateOnlineDbEndpoint(endpoint: String, syncEnabled: Boolean) {
        sessionManager.setOnlineDbEndpoint(endpoint)
        sessionManager.setOnlineSyncEnabled(syncEnabled)
        refreshOnlineDbStatus()
    }

    fun clearNotice() {
        _uiState.update { it.copy(infoNotice = null, errorMessage = null) }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            broadcastRepository.deletePost(postId)
            _uiState.update { it.copy(infoNotice = "Post record deleted") }
        }
    }
}
