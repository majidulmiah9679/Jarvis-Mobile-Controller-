package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AiClientManager
import com.example.ui.JarvisViewModel
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val requestPermissionsLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    // Permissions updated - hardware and audio listeners are automatically primed
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    requestEssentialPermissions()

    setContent {
      val jarvisViewModel: JarvisViewModel = viewModel()
      MyApplicationTheme(darkTheme = jarvisViewModel.isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = if (jarvisViewModel.isDarkTheme) HoloDarkBg else androidx.compose.ui.graphics.Color(0xFFF5F7FB)
        ) {
          com.example.ui.MainAppScreen(viewModel = jarvisViewModel)
        }
      }
    }
  }

  private fun requestEssentialPermissions() {
    val neededPermissions = mutableListOf(
      Manifest.permission.RECORD_AUDIO,
      Manifest.permission.CAMERA,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val ungranted = neededPermissions.filter {
      ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }
    if (ungranted.isNotEmpty()) {
      requestPermissionsLauncher.launch(ungranted.toTypedArray())
    }
  }

  override fun onResume() {
    super.onResume()
    com.example.service.JarvisAppState.isForeground = true
  }

  override fun onPause() {
    super.onPause()
    com.example.service.JarvisAppState.isForeground = false
  }
}

// Retained for screenshot test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("J.A.R.V.I.S.") }
}
