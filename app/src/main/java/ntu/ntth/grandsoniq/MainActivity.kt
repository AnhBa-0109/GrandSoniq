package ntu.ntth.grandsoniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController
import ntu.ntth.grandsoniq.navigation.GrandSoniqNavHost
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackController: PlaybackController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
                GrandSoniqNavHost(playbackController = playbackController)
            }
        }
    }
}
