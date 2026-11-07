package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ColorPalettePreset
import com.example.data.model.SpeedUnit
import com.example.ui.components.SpeedGauge
import com.example.ui.theme.SpeedMeterTheme
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
      SpeedMeterTheme(darkTheme = true, palette = ColorPalettePreset.CYAN_NEON) {
        SpeedGauge(
          downloadBps = 1024 * 1024 * 12L,
          uploadBps = 1024 * 1024 * 2L,
          speedUnit = SpeedUnit.AUTO,
          primaryColor = Color(0xFF00E5FF),
          downloadColor = Color(0xFF00E676),
          uploadColor = Color(0xFF00E5FF)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
