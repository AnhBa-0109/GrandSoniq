package ntu.ntth.grandsoniq.feature.discover.ui

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ntu.ntth.grandsoniq.core.common.DateTimeUtils
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.ui.ErrorStateView
import ntu.ntth.grandsoniq.core.ui.GrandSoniqArtwork
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomNavBar
import ntu.ntth.grandsoniq.core.ui.LoadingStateView
import ntu.ntth.grandsoniq.core.ui.MainNavTab
import ntu.ntth.grandsoniq.core.ui.MiniPlayerBar
import ntu.ntth.grandsoniq.core.ui.PrimaryPlayPillButton
import ntu.ntth.grandsoniq.core.ui.RoundActionIconButton
import ntu.ntth.grandsoniq.core.ui.SongListItem
import ntu.ntth.grandsoniq.feature.discover.viewmodel.AlbumDetailUiState
import ntu.ntth.grandsoniq.feature.discover.viewmodel.AlbumDetailViewModel
import ntu.ntth.grandsoniq.feature.player.service.PlaybackUiSnapshot
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackgroundGlow
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedSongForSheet by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(state.feedbackMessage) {
        val msg = state.feedbackMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrandSoniqBackground)
    ) {
        AlbumDetailContent(
            state = state,
            playbackSnapshot = playbackSnapshot,
            onBack = onBack,
            onRetry = { viewModel.load() },
            onArtistClick = onArtistClick,
            onToggleSave = viewModel::toggleSaveAlbum,
            onToggleDownload = viewModel::toggleDownloadAlbum,
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Nghe album \"${state.album.title}\" của ${state.album.artistName} trên GrandSoniq!"
                    )
                }
                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ album"))
            },
            onToggleShuffle = viewModel::toggleShuffle,
            onPlayAll = { viewModel.playAll(shuffle = playbackSnapshot.isShuffle) },
            onSongClick = viewModel::playSong,
            onSongMoreClick = { selectedSongForSheet = it }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    selectedSongForSheet?.let { song ->
        val isFav = song.id in state.favoriteSongIds
        val isDown = song.id in state.downloadedSongIds
        ModalBottomSheet(
            onDismissRequest = { selectedSongForSheet = null },
            containerColor = GrandSoniqPanel,
            contentColor = GrandSoniqTextPrimary
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                ListItem(
                    headlineContent = { Text(song.title, color = GrandSoniqTextPrimary, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(song.artistName, color = GrandSoniqTextMuted) },
                    leadingContent = {
                        GrandSoniqArtwork(
                            url = song.coverUrl,
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = GrandSoniqPanel)
                )
                AlbumSheetActionRow(
                    icon = Icons.Filled.PlayArrow,
                    label = "Phát bài hát này",
                    onClick = {
                        viewModel.playSong(song)
                        selectedSongForSheet = null
                    }
                )
                AlbumSheetActionRow(
                    icon = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = if (isFav) "Bỏ khỏi bài hát đã thích" else "Thêm vào bài hát đã thích",
                    tint = if (isFav) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleFavoriteSong(song)
                        selectedSongForSheet = null
                    }
                )
                AlbumSheetActionRow(
                    icon = if (isDown) Icons.Filled.ArrowCircleDown else Icons.Outlined.DownloadForOffline,
                    label = if (isDown) "Xóa bản tải xuống" else "Tải về nghe offline",
                    tint = if (isDown) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleDownloadSong(song)
                        selectedSongForSheet = null
                    }
                )
                if (song.artistId.isNotBlank()) {
                    AlbumSheetActionRow(
                        icon = Icons.Rounded.Person,
                        label = "Xem hồ sơ nghệ sĩ (${song.artistName})",
                        onClick = {
                            selectedSongForSheet = null
                            onArtistClick(song.artistId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlbumDetailContent(
    state: AlbumDetailUiState,
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onArtistClick: (String) -> Unit,
    onToggleSave: () -> Unit,
    onToggleDownload: () -> Unit,
    onShare: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPlayAll: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    if (state.isLoading) {
        LoadingStateView(modifier.fillMaxSize())
        return
    }
    if (state.errorMessage != null) {
        Column(modifier.fillMaxSize()) {
            IconButton(onClick = onBack, modifier = Modifier.padding(6.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = GrandSoniqTextPrimary)
            }
            ErrorStateView(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    val album = state.album
    val artist = state.artist
    val totalMinutes = DateTimeUtils.formatTotalMinutes(state.songs.sumOf { it.duration })
    val releaseYear = DateTimeUtils.formatYear(album.releaseDate)
    val allDownloaded = state.songs.isNotEmpty() &&
        state.songs.all { it.id in state.downloadedSongIds }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GrandSoniqBackgroundGlow, GrandSoniqBackground)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = GrandSoniqTextPrimary
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu album",
                                    tint = GrandSoniqTextPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = GrandSoniqPanel
                            ) {
                                if (album.artistId.isNotBlank()) {
                                    DropdownMenuItem(
                                        text = { Text("Xem hồ sơ nghệ sĩ", color = GrandSoniqTextPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Person, null, tint = GrandSoniqAccent)
                                        },
                                        onClick = {
                                            showMenu = false
                                            onArtistClick(album.artistId)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (state.isSaved) "Bỏ lưu album" else "Lưu vào thư viện",
                                            color = GrandSoniqTextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (state.isSaved) Icons.Filled.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                                            null,
                                            tint = GrandSoniqTextPrimary
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onToggleSave()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    GrandSoniqArtwork(
                        url = album.coverUrl,
                        modifier = Modifier.size(210.dp),
                        shape = RoundedCornerShape(14.dp),
                        fallbackIcon = Icons.Rounded.Album,
                        iconSizeFraction = 0.36f
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = album.title,
                        color = GrandSoniqTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = album.artistId.isNotBlank()) {
                                onArtistClick(album.artistId)
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GrandSoniqArtwork(
                            url = artist?.avatarUrl.orEmpty(),
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            fallbackIcon = Icons.Rounded.Person,
                            iconSizeFraction = 0.6f
                        )
                        Text(
                            text = album.artistName.ifBlank { artist?.name ?: "Nghệ sĩ" },
                            color = GrandSoniqTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (artist?.isVerified == true) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Nghệ sĩ xác minh",
                                tint = GrandSoniqAccent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = buildList {
                            add("Album")
                            if (releaseYear.isNotBlank()) add(releaseYear)
                            add("${state.songs.size} bài hát")
                            if (totalMinutes > 0L) add("$totalMinutes phút")
                        }.joinToString(" · "),
                        color = GrandSoniqTextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onToggleSave) {
                                Icon(
                                    imageVector = if (state.isSaved) {
                                        Icons.Filled.BookmarkAdded
                                    } else {
                                        Icons.Outlined.BookmarkAdd
                                    },
                                    contentDescription = "Lưu album",
                                    tint = if (state.isSaved) GrandSoniqAccent else GrandSoniqTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onToggleDownload) {
                                Icon(
                                    imageVector = if (allDownloaded) {
                                        Icons.Filled.ArrowCircleDown
                                    } else {
                                        Icons.Outlined.DownloadForOffline
                                    },
                                    contentDescription = "Tải album",
                                    tint = if (allDownloaded) GrandSoniqAccent else GrandSoniqTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onShare) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Chia sẻ",
                                    tint = GrandSoniqTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RoundActionIconButton(
                                icon = Icons.Filled.Shuffle,
                                contentDescription = "Phát ngẫu nhiên",
                                onClick = onToggleShuffle,
                                iconTint = if (playbackSnapshot.isShuffle) {
                                    GrandSoniqAccent
                                } else {
                                    GrandSoniqTextPrimary
                                }
                            )
                            PrimaryPlayPillButton(
                                text = "Phát",
                                onClick = onPlayAll
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        if (state.songs.isEmpty()) {
            item {
                Text(
                    text = "Album chưa có bài hát nào.",
                    color = GrandSoniqTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                )
            }
        } else {
            itemsIndexed(state.songs, key = { _, s -> s.id }) { index, song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackSnapshot.currentSong?.id == song.id,
                    isDownloaded = song.id in state.downloadedSongIds,
                    trackNumber = index + 1,
                    subtitleOverride = "${song.artistName} · ${DateTimeUtils.formatTrackDuration(song.duration)}",
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }
    }
}

@Composable
private fun AlbumSheetActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = GrandSoniqTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514, heightDp = 820)
@Composable
private fun AlbumDetailScreenPreview() {
    val sampleSongs = listOf(
        Song(id = "s1", title = "Sau cơn mưa", artistName = "An Nhiên", duration = 225_000L),
        Song(id = "s2", title = "Đi qua tháng mười", artistName = "An Nhiên", duration = 230_000L),
        Song(id = "s3", title = "Phố vắng", artistName = "An Nhiên", duration = 198_000L),
        Song(id = "s4", title = "Đêm bình yên", artistName = "An Nhiên", duration = 242_000L)
    )
    val state = AlbumDetailUiState(
        isLoading = false,
        album = Album(
            id = "alb_1",
            title = "Sau Cơn Mưa",
            artistId = "art_1",
            artistName = "An Nhiên",
            songIds = sampleSongs.map { it.id },
            releaseDate = 1_735_689_600_000L
        ),
        artist = Artist(id = "art_1", name = "An Nhiên", isVerified = true),
        songs = sampleSongs,
        downloadedSongIds = setOf("s1"),
        isSaved = true
    )
    val playback = PlaybackUiSnapshot(
        currentSong = sampleSongs.first(),
        queue = sampleSongs,
        currentIndex = 0,
        isPlaying = true,
        progress = 0.4f
    )
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrandSoniqBackground)
        ) {
            AlbumDetailContent(
                state = state,
                playbackSnapshot = playback,
                onBack = {},
                onRetry = {},
                onArtistClick = {},
                onToggleSave = {},
                onToggleDownload = {},
                onShare = {},
                onToggleShuffle = {},
                onPlayAll = {},
                onSongClick = {},
                onSongMoreClick = {},
                modifier = Modifier.weight(1f)
            )
            MiniPlayerBar(
                song = sampleSongs.first(),
                isPlaying = true,
                progress = 0.4f,
                onTogglePlayPause = {},
                onNext = {},
                onClick = {}
            )
            GrandSoniqBottomNavBar(
                selectedTab = MainNavTab.LIBRARY,
                onTabSelected = {}
            )
        }
    }
}
