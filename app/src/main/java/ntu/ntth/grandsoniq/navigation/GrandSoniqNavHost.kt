package ntu.ntth.grandsoniq.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomBar
import ntu.ntth.grandsoniq.core.ui.MainTab
import ntu.ntth.grandsoniq.feature.discover.ui.DiscoverScreen
import ntu.ntth.grandsoniq.feature.discover.ui.HomeScreen
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverViewModel
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeViewModel

private val AvailableTabs = setOf(MainTab.Home, MainTab.Discover)

@Composable
internal fun GrandSoniqNavHost(home: HomeViewModel, discover: () -> DiscoverViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val tabState = rememberSaveableStateHolder()
    BackHandler(enabled = selectedTab != MainTab.Home) { selectedTab = MainTab.Home }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF101311)).safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            tabState.SaveableStateProvider(selectedTab.name) {
                when (selectedTab) {
                    MainTab.Home -> HomeScreen(home.state, home::reload)
                    MainTab.Discover -> DiscoverScreen(discover())
                    MainTab.Library, MainTab.Friends -> Unit
                }
            }
        }
        GrandSoniqBottomBar(
            selectedTab = selectedTab,
            enabledTabs = AvailableTabs,
            onTabSelected = { selectedTab = it }
        )
    }
}
