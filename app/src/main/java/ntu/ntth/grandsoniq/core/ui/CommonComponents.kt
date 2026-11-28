package ntu.ntth.grandsoniq.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqArtworkBg
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqElevated
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqOnAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

enum class MainNavTab(val label: String) {
    HOME("Trang chủ"),
    DISCOVER("Khám phá"),
    LIBRARY("Thư viện"),
    FRIENDS("Bạn bè")
}

@Composable
fun GrandSoniqArtwork(
    url: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    fallbackIcon: ImageVector = Icons.Rounded.MusicNote,
    iconSizeFraction: Float = 0.42f
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(GrandSoniqArtworkBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = GrandSoniqTextMuted,
            modifier = Modifier.fillMaxSize(iconSizeFraction)
        )
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun PrimaryPlayPillButton(
    text: String = "Phát",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = GrandSoniqAccent,
            contentColor = GrandSoniqOnAccent
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        modifier = modifier.height(44.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RoundActionIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = GrandSoniqElevated,
    iconTint: Color = GrandSoniqTextPrimary
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
fun GrandSoniqBottomNavBar(
    selectedTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GrandSoniqBackground)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainNavTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val tint = if (isSelected) GrandSoniqAccent else GrandSoniqTextMuted
            val icon = when (tab) {
                MainNavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                MainNavTab.DISCOVER -> if (isSelected) Icons.Filled.Explore else Icons.Outlined.Explore
                MainNavTab.LIBRARY -> if (isSelected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
                MainNavTab.FRIENDS -> if (isSelected) Icons.Filled.Group else Icons.Outlined.Group
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = tab.label,
                    color = tint,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun LoadingStateView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = GrandSoniqAccent)
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = message,
            color = GrandSoniqTextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onRetry) {
            Text("Thử lại", color = GrandSoniqAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514)
@Composable
private fun CommonComponentsPreview() {
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .background(GrandSoniqBackground)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryPlayPillButton(text = "Phát", onClick = {})
                RoundActionIconButton(
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    onClick = {}
                )
            }
            GrandSoniqBottomNavBar(
                selectedTab = MainNavTab.LIBRARY,
                onTabSelected = {}
            )
        }
    }
}
