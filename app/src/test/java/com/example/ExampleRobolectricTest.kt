package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import com.example.ui.JewelryViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gestor de Joias", appName)
  }

  @Test
  fun testDashboardLaunchesSuccessfully() {
    composeTestRule.setContent {
      val app = ApplicationProvider.getApplicationContext<Application>()
      val viewModel: JewelryViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
      )
      MyApplicationTheme {
        JewelryAdminDashboard(viewModel = viewModel)
      }
    }
    // Just verifying it renders without crashing
    composeTestRule.waitForIdle()
  }
}
