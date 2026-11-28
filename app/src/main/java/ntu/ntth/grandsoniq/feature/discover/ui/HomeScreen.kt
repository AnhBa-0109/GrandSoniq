package ntu.ntth.grandsoniq.feature.discover.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomNavBar
import ntu.ntth.grandsoniq.core.ui.MainNavTab
import ntu.ntth.grandsoniq.core.ui.MiniPlayerBar
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import ntu.ntth.grandsoniq.R
import ntu.ntth.grandsoniq.core.ui.GrandSoniqHeader
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeUiState
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

private val Background = Color(0xFF101311)
private val Panel = Color(0xFF191E1B)
private val ArtworkBackground = Color(0xFF191919)
private val Foreground = Color(0xFFF3F5EF)
private val Muted = Color(0xFFB2BBB4)
private val Accent = Color(0xFFD5F47A)
private val SidePadding = 20.dp

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    onPlaySong: ((Song) -> Unit)? = null,
    onArtistClick: (String) -> Unit = {}
) {
    val genres = state.songs.filter { it.genre.isNotBlank() }.distinctBy { it.genre }

    Column(Modifier.fillMaxSize().background(Background)) {
        GrandSoniqHeader(
            title = stringResource(R.string.home_title),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
            }
        
            if (state.errorCodes.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = SidePadding)) {
                        Text("Không tải được dữ liệu Firestore", color = Foreground)
                        Text(state.errorCodes.joinToString(), color = Muted, fontSize = 13.sp)
                        TextButton(onClick = onRetry) { Text("Thử lại", color = Accent) }
                    }
                }
            }
            if (!state.isLoading && state.errorCodes.isEmpty() &&
                state.songs.isEmpty() && state.albums.isEmpty() &&
                state.artists.isEmpty() && state.playlists.isEmpty()
            ) {
                item     {
                    Text(
                        "Chưa có bài hát, album hoặc nghệ sĩ.",
                        Modifier.padding(horizontal = SidePadding),
                        color = Muted
                    )
                }
                if (state.songs.isNotEmpty()) {
                    item {
                        HomeSection("Bắt đầu nghe") {
                            items(state.songs.chunked(3)) { songs ->
                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    songs.forEach { song ->
                                        SongTile(
                                            song = song,
                                            current = currentSongId == song.id,
                                            isPlaying = isPlaying,
                                            onPlaySong = onPlaySong
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (state.playlists.isNotEmpty()) {
                item {
                    HomeSection("Playlist gợi ý") {
                        items(state.playlists, key = { it.id }) { playlist ->
                            ArtworkCard(
                                title = playlist.name,
                                subtitle = listOf(playlist.ownerName, "${playlist.songIds.size} bài")
                                    .filter(String::isNotBlank).joinToString(" · "),
                                url = playlist.coverUrl,
                                fallbackIcon = Icons.AutoMirrored.Rounded.QueueMusic,
                                onPlaylistClick: (String) -> Unit = {}
                            )
                        }
                    }
                }
            }
            if (state.albums.isNotEmpty()) {
                item {
                    HomeSection("Dành cho bạn") {
                        items(state.albums, key = { it.id }) { album ->
                            ArtworkCard(
                                title = album.title,
                                subtitle = listOf(album.artistName, "Album")
                                    .filter(String::isNotBlank).joinToString(" · "),
                                url = album.coverUrl,
                                fallbackIcon = Icons.Rounded.Album,
                                onAlbumClick: (String) -> Unit = {}
                            )
                        }
                    }
                }
            }
            if (state.artists.isNotEmpty()) {
                item {
                    HomeSection("Nghệ sĩ") {
                        items(state.artists, key = { it.id }) { artist ->
                            ArtworkCard(
                                title = artist.name,
                                subtitle = "Nghệ sĩ",
                                url = artist.avatarUrl,
                                round = true,
                                fallbackIcon = Icons.Rounded.Person,
                                onClick = { onArtistClick(artist.id) }
                            )
                        }
                    }
                }
            }
            if (genres.isNotEmpty()) {
                item {
                    HomeSection("Khám phá các thể loại") {
                        items(genres, key = { it.genre }) { song ->
                            ArtworkCard(
                                title = song.genre,
                                subtitle = "",
                                url = song.coverUrl,
                                fallbackIcon = Icons.Rounded.MusicNote,
                                onSongClick: (Song) -> Unit = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSection(title: String, content: LazyListScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title, Modifier.padding(horizontal = SidePadding).semantics { heading() },
            color = Foreground, fontSize = 21.sp, fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SidePadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SongTile(song: Song, current: Boolean, isPlaying: Boolean, onPlaySong: ((Song) -> Unit)?) {
    val playing = current && isPlaying
    val titleColor by animateColorAsState(if (current) Accent else Color.White, label = "Song title")
    val playbackDescription = stringResource(if (playing) R.string.home_now_playing else R.string.home_paused)
    val clickModifier = if (onPlaySong != null) Modifier.clickable { onPlaySong(song) } else Modifier

    Row(
        Modifier.width(310.dp).clip(RoundedCornerShape(10.dp)).background(Panel)
            .then(clickModifier)
            .semantics { if (current) stateDescription = playbackDescription }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Artwork(song.coverUrl, Modifier.size(48.dp), RoundedCornerShape(7.dp), Icons.Rounded.MusicNote)
        Box(Modifier.weight(1f)) {
            MediaText(song.title, song.artistName, titleColor)
        }
        PlaybackIndicator(playing)
    }
}

@Composable
private fun PlaybackIndicator(playing: Boolean) {
    val transition = updateTransition(playing, label = "Playback icon")
    val equalizerHeight by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 250) },
        label = "Equalizer height"
    ) { if (it) 1f else 0f }

    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Filled.MoreHoriz,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.graphicsLayer { alpha = 1f - equalizerHeight }
        )
        if (transition.currentState || transition.targetState) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.playing_music))
            Box(
                Modifier.matchParentSize().graphicsLayer {
                    scaleY = equalizerHeight
                    alpha = equalizerHeight
                },
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    // Zoom past the transparent padding around the equalizer in the source artboard.
                    LottieAnimation(
                        composition,
                        modifier = Modifier.size(24.dp).scale(7f),
                        isPlaying = playing,
                        iterations = LottieConstants.IterateForever
                    )
                } else {
                    Icon(Icons.Filled.Equalizer, contentDescription = null, tint = Accent)
                }
            }
        }
    }
}

@Composable
private fun ArtworkCard(
    title: String,
    subtitle: String,
    url: String,
    round: Boolean = false,
    fallbackIcon: ImageVector = Icons.Rounded.MusicNote,
    onClick: () -> Unit = {}
) {
    val size = if (round) 128.dp else 148.dp
    Column(
        Modifier
            .width(size)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = if (round) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Artwork(
    url = url,
    modifier = Modifier.size(size),
    shape = if (round) CircleShape else RoundedCornerShape(8.dp),
    fallbackIcon = fallbackIcon
))
        Column(Modifier.padding(top = 6.dp),
            horizontalAlignment = if (round) Alignment.CenterHorizontally else Alignment.Start) {
            MediaText(
                title,
                subtitle,
                horizontalAlignment = if (round) Alignment.CenterHorizontally else Alignment.Start
            )
        }
    }
}

@Composable
private fun Artwork(url: String, modifier: Modifier, shape: Shape, fallbackIcon: ImageVector) {
    Box(modifier.clip(shape).background(ArtworkBackground), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(28.dp)
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
private fun MediaText(title: String, subtitle: String, titleColor: Color = Foreground, horizontalAlignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514, heightDp = 820)
@Composable
private fun HomeScreenPreview() {
    val sampleSongs = listOf(
        Song(id = "s1", title = "Sau cơn mưa", artistName = "An Nhiên", genre = "Indie"),
        Song(id = "s2", title = "Đèn phố", artistName = "Hải Nam", genre = "Ballad"),
        Song(id = "s3", title = "Một ngày nhẹ tênh", artistName = "Mây Xanh", genre = "Lofi")
    )
    val sampleState = HomeUiState(
        isLoading = false,
        songs = sampleSongs,
        playlists = listOf(
            Playlist(id = "pl_1", name = "Đường về nhà", ownerName = "Minh Anh", songIds = List(14) { "s$it" })
        ),
        albums = listOf(
            Album(id = "alb_1", title = "Sau Cơn Mưa", artistName = "An Nhiên")
        ),
        artists = listOf(
            Artist(id = "art_1", name = "An Nhiên", isVerified = true)
        )
    )
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(modifier = Modifier.fillMaxSize().background(Background)) {
            Box(modifier = Modifier.weight(1f)) {
                HomeScreen(
                    state = sampleState,
                    currentSongId = "s1",
                    onRetry = {}
                )
            }
            MiniPlayerBar(
                song = sampleSongs.first(),
                isPlaying = true,
                progress = 0.3f,
                onTogglePlayPause = {},
                onNext = {},
                onClick = {}
            )
            GrandSoniqBottomNavBar(selectedTab = MainNavTab.HOME, onTabSelected = {})
        }
    }
}
