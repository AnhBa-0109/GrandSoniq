package ntu.ntth.grandsoniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import ntu.ntth.grandsoniq.feature.discover.ui.HomeScreen
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeViewModel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val home = ViewModelProvider(this)[HomeViewModel::class.java]
        setContent {
            GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
                HomeScreen(home.state, home::reload)
            }
        }
    }
}
