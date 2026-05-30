package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.VirtualLibraryCard
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { 
      MyApplicationTheme { 
        VirtualLibraryCard(studentName = "Alex Rivera (Grade 11)", studentId = "StudentID-2026-HSL") 
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun auth_entry_screen_rendering() {
    composeTestRule.setContent {
      MyApplicationTheme {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val vm = com.example.ui.auth.AuthViewModel(context as android.app.Application)
        com.example.ui.auth.AuthEntryScreen(viewModel = vm)
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/auth_screen.png")
  }

  @Test
  fun library_dashboard_rendering() {
    composeTestRule.setContent {
      MyApplicationTheme {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val vm = com.example.ui.LibraryViewModel(context as android.app.Application)
        com.example.ui.LibraryDashboard(viewModel = vm, onLogout = {})
      }
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
  }
}
