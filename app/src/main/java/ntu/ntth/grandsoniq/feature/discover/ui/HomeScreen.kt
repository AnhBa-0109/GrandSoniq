package ntu.ntth.grandsoniq.feature.discover.ui

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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeUiState
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

private val Background = Color(0xFF111514)
private val Panel = Color(0xFF1B211E)
private val ArtworkBackground = Color(0xFF303B32)
private val Foreground = Color(0xFFF4F5F1)
private val Muted = Color(0xFFB8BFB8)
private val Accent = Color(0xFFDFFF73)
private val SidePadding = 20.dp

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    activeSongId: String? = null,
    onRetry: () -> Unit,
    onSongClick: (Song) -> Unit = {},
    onAlbumClick: (String) -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onPlaylistClick: (String) -> Unit = {}
) {
    val genres = state.songs.filter { it.genre.isNotBlank() }.distinctBy { it.genre }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HomeHeader() }
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
            item {
                Text(
                    "Chưa có bài hát, album hoặc nghệ sĩ.",
                    Modifier.padding(horizontal = SidePadding),
                    color = Muted
                )
            }
        }
        if (state.songs.isNotEmpty()) {
            item {
                HomeSection("Bắt đầu nghe") {
                    items(state.songs.chunked(3)) { songs ->
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            songs.forEach { song ->
                                SongTile(song, activeSongId == song.id) {
                                    onSongClick(song)
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
                            onClick = { onPlaylistClick(playlist.id) }
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
                            onClick = { onAlbumClick(album.id) }
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
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = SidePadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).background(ArtworkBackground, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Accent)
        }
        Column(Modifier.weight(1f)) {
            Text("Chào bạn", color = Muted, fontSize = 12.sp)
            Text("Nhạc cho hôm nay", color = Foreground, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Foreground)
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
private fun SongTile(song: Song, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier.width(310.dp).clip(RoundedCornerShape(10.dp)).background(Panel)
            .clickable(onClick = onSelect).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Artwork(song.coverUrl, Modifier.size(48.dp), RoundedCornerShape(7.dp), Icons.Rounded.MusicNote)
        Box(Modifier.weight(1f)) {
            MediaText(song.title, song.artistName, if (selected) Accent else Foreground)
        }
        Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
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
        Artwork(url, Modifier.size(size), if (round) CircleShape else RoundedCornerShape(8.dp), fallbackIcon)
        Column(
            Modifier.padding(top = 6.dp),
            horizontalAlignment = if (round) Alignment.CenterHorizontally else Alignment.Start
        ) {
            MediaText(title, subtitle)
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
private fun MediaText(title: String, subtitle: String, titleColor: Color = Foreground) {
    Column {
        Text(
            title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
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
                HomeScreen(state = sampleState, activeSongId = "s1", onRetry = {})
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
