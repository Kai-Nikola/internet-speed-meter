package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SpeedFormatter
import com.example.data.model.SpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Speed Meter", appName)
  }

  @Test
  fun `format speed values correctly`() {
    val speedKb = SpeedFormatter.formatSpeed(1024 * 500L, SpeedUnit.KILOBYTES)
    assertEquals("500", speedKb.value)
    assertEquals("KB/s", speedKb.unit)

    val speedMb = SpeedFormatter.formatSpeed(1024 * 1024 * 5L, SpeedUnit.MEGABYTES)
    assertEquals("5.0", speedMb.value)
    assertEquals("MB/s", speedMb.unit)
  }
}
