package ntu.ntth.grandsoniq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverViewModel
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeViewModel
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
        val home = ViewModelProvider(this)[HomeViewModel::class.java]
        val discover by lazy { ViewModelProvider(this)[DiscoverViewModel::class.java] }
        setContent {
            GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
                GrandSoniqNavHost(
                    home = home,
                    discover = { discover },
                    playbackController = playbackController
                )
            }
        }
    }

}
