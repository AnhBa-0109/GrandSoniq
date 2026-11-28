package ntu.ntth.grandsoniq.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqOnAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    isDownloaded: Boolean = false,
    trackNumber: Int? = null,
    subtitleOverride: String? = null,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (trackNumber != null) {
            Text(
                text = trackNumber.toString(),
                color = if (isPlaying) GrandSoniqAccent else GrandSoniqTextMuted,
                fontSize = 14.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
        } else {
            GrandSoniqArtwork(
                url = song.coverUrl,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                fallbackIcon = Icons.Rounded.MusicNote
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = song.title,
                color = if (isPlaying) GrandSoniqAccent else GrandSoniqTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDownloaded) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GrandSoniqAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Đã tải",
                            tint = GrandSoniqOnAccent,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = subtitleOverride ?: song.artistName,
                    color = GrandSoniqTextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isPlaying) {
            Icon(
                imageVector = Icons.Filled.Equalizer,
                contentDescription = "Đang phát",
                tint = GrandSoniqAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Tùy chọn",
                tint = GrandSoniqTextMuted,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514)
@Composable
private fun SongListItemPreview() {
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(modifier = Modifier.background(GrandSoniqBackground)) {
            SongListItem(
                song = Song(id = "1", title = "Sau cơn mưa", artistName = "An Nhiên"),
                isPlaying = true,
                isDownloaded = true,
                onClick = {},
                onMoreClick = {}
            )
            SongListItem(
                song = Song(id = "2", title = "Đèn phố", artistName = "Hải Nam"),
                isPlaying = false,
                isDownloaded = false,
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}
