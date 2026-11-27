package ntu.ntth.grandsoniq.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import ntu.ntth.grandsoniq.R

internal enum class MainTab(@param:StringRes val label: Int, val icon: String) {
    Home(R.string.nav_home, "home"),
    Discover(R.string.nav_discover, "explore"),
    Library(R.string.nav_library, "library"),
    Friends(R.string.nav_friends, "friends")
}

@Composable
internal fun GrandSoniqBottomBar(
    selectedTab: MainTab,
    enabledTabs: Set<MainTab>,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.widthIn(max = 480.dp).fillMaxWidth().background(Color(0xFF101311))
    ) {
        HorizontalDivider(color = Color(0xFF343D36).copy(alpha = 0.4f))
        Row(Modifier.fillMaxWidth().height(71.dp).padding(horizontal = 8.dp).selectableGroup()) {
            MainTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val color = if (selected) Color(0xFFD5F47A) else Color(0xFFB2BBB4)
                Column(
                    Modifier.weight(1f).height(71.dp).selectable(
                        selected = selected,
                        enabled = tab in enabledTabs,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) }
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AssetSvgIcon("discover/${tab.icon}.svg", 24.dp, tint = color)
                    Text(
                        stringResource(tab.label), color = color, fontSize = 12.sp, lineHeight = 16.sp,
                        letterSpacing = 0.2.sp, maxLines = 1,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
internal fun AssetSvgIcon(path: String, size: Dp, description: String? = null, tint: Color? = null) {
    val context = LocalContext.current
    val model = remember(context, path) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/$path")
            .decoderFactory(SvgDecoder.Factory())
            .build()
    }
    AsyncImage(
        model = model,
        contentDescription = description,
        modifier = Modifier.size(size),
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}
