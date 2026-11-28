package ntu.ntth.grandsoniq.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqDivider
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GrandSoniqPanel)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GrandSoniqArtwork(
                url = song.coverUrl,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(7.dp),
                fallbackIcon = Icons.Rounded.MusicNote
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = GrandSoniqTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.artistName.isNotBlank()) {
                    Text(
                        text = song.artistName,
                        color = GrandSoniqTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                    tint = GrandSoniqTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Bài tiếp theo",
                    tint = GrandSoniqTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(GrandSoniqDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(2.5.dp)
                    .background(GrandSoniqAccent)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514)
@Composable
private fun MiniPlayerBarPreview() {
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Box(modifier = Modifier.background(GrandSoniqBackground)) {
            MiniPlayerBar(
                song = Song(
                    id = "song_01",
                    title = "Sau cơn mưa",
                    artistName = "An Nhiên"
                ),
                isPlaying = true,
                progress = 0.35f,
                onTogglePlayPause = {},
                onNext = {},
                onClick = {}
            )
        }
    }
}
