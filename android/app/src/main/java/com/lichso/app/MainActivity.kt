package com.lichso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lichso.app.ui.LichSoMainScreen
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.ui.theme.LichSoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by settingsDataStore.data
                .map { it[SettingsKeys.DARK_MODE] ?: true }
                .collectAsState(initial = true)

            LichSoTheme(darkTheme = darkMode) {
                LichSoMainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
