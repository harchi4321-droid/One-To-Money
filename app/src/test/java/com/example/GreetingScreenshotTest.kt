package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.model.User
import com.example.onetomany.data.remote.OnlineDbStatus
import com.example.onetomany.ui.screens.BroadcastScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun broadcast_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BroadcastScreen(
          currentUser = User("test-u1", "demo@1tomany.com", "Studio Creator", "salt", "hash", System.currentTimeMillis()),
          onlineDbStatus = OnlineDbStatus.Connected("Firestore Cloud DB", "Live"),
          postTitle = "Global Broadcast Announcement",
          postContent = "Publishing simultaneously across all 4 channels: Facebook, YouTube, TikTok, and Instagram with 1 To Many!",
          postMediaUrl = "",
          hashtagInput = "",
          hashtags = listOf("1ToMany", "SocialMedia", "Viral"),
          selectedPlatforms = SocialPlatform.entries.toSet(),
          activePreviewTab = SocialPlatform.FACEBOOK,
          isBroadcasting = false,
          onTitleChange = {},
          onContentChange = {},
          onMediaUrlChange = {},
          onHashtagInputChange = {},
          onAddHashtag = {},
          onRemoveHashtag = {},
          onTogglePlatform = {},
          onSelectAllPlatforms = {},
          onPreviewTabChange = {},
          onBroadcastSubmit = {},
          onOpenAuthDialog = {},
          onOpenOnlineDbDialog = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/broadcast_screen.png")
  }
}

