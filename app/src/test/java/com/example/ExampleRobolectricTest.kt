package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.onetomany.data.model.SocialPlatform
import com.example.onetomany.data.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("1 To Many", appName)
  }

  @Test
  fun `all 4 social platforms are registered`() {
    val platforms = SocialPlatform.entries
    assertEquals(4, platforms.size)
    assertTrue(platforms.any { it == SocialPlatform.FACEBOOK })
    assertTrue(platforms.any { it == SocialPlatform.YOUTUBE })
    assertTrue(platforms.any { it == SocialPlatform.TIKTOK })
    assertTrue(platforms.any { it == SocialPlatform.INSTAGRAM })
  }

  @Test
  fun `security password hashing produces valid salt and hash`() {
    val password = "StrongUserPassword123"
    val salt = SecurityManager.generateSalt()
    val hash = SecurityManager.hashPassword(password, salt)
    assertNotNull(hash)
    assertNotNull(salt)
    assertTrue(SecurityManager.verifyPassword(password, salt, hash))
    org.junit.Assert.assertFalse(SecurityManager.verifyPassword("WrongPassword", salt, hash))
  }
}

