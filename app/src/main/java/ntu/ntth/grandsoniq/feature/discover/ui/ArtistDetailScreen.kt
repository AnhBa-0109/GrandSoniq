package ntu.ntth.grandsoniq.feature.discover.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ntu.ntth.grandsoniq.core.common.DateTimeUtils
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Playlist
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
import ntu.ntth.grandsoniq.feature.discover.viewmodel.ArtistDetailUiState
import ntu.ntth.grandsoniq.feature.discover.viewmodel.ArtistDetailViewModel
import ntu.ntth.grandsoniq.feature.player.service.PlaybackUiSnapshot
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqArtworkBg
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackgroundGlow
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqDivider
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqElevated
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
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
        ArtistDetailContent(
            state = state,
            playbackSnapshot = playbackSnapshot,
            onBack = onBack,
            onRetry = { viewModel.load() },
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Nghe nhạc của ${state.artist.name} trên GrandSoniq!"
                    )
                }
                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ nghệ sĩ"))
            },
            onToggleFollow = viewModel::toggleFollow,
            onToggleShuffle = viewModel::toggleShuffle,
            onPlayAll = { viewModel.playAll(shuffle = playbackSnapshot.isShuffle) },
            onToggleBio = viewModel::toggleBioExpanded,
            onSongClick = viewModel::playSong,
            onSongMoreClick = { selectedSongForSheet = it },
            onAlbumClick = onAlbumClick,
            onPlaylistClick = onPlaylistClick
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
                ArtistSheetActionRow(
                    icon = Icons.Filled.PlayArrow,
                    label = "Phát bài hát này",
                    onClick = {
                        viewModel.playSong(song)
                        selectedSongForSheet = null
                    }
                )
                ArtistSheetActionRow(
                    icon = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = if (isFav) "Bỏ khỏi bài hát đã thích" else "Thêm vào bài hát đã thích",
                    tint = if (isFav) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleFavoriteSong(song)
                        selectedSongForSheet = null
                    }
                )
                ArtistSheetActionRow(
                    icon = if (isDown) Icons.Filled.ArrowCircleDown else Icons.Outlined.DownloadForOffline,
                    label = if (isDown) "Xóa bản tải xuống" else "Tải về nghe offline",
                    tint = if (isDown) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleDownloadSong(song)
                        selectedSongForSheet = null
                    }
                )
                if (song.albumId.isNotBlank()) {
                    ArtistSheetActionRow(
                        icon = Icons.Rounded.Album,
                        label = "Xem album",
                        onClick = {
                            selectedSongForSheet = null
                            onAlbumClick(song.albumId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun ArtistDetailContent(
    state: ArtistDetailUiState,
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onToggleFollow: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPlayAll: () -> Unit,
    onToggleBio: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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

    val artist = state.artist
    val bannerUrl = artist.coverUrl.ifBlank { artist.avatarUrl }
    var showAllSongs by remember { mutableStateOf(false) }
    val visibleSongs = if (showAllSongs || state.topSongs.size <= 5) {
        state.topSongs
    } else {
        state.topSongs.take(5)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(GrandSoniqBackgroundGlow)
            ) {
                if (bannerUrl.isNotBlank()) {
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GrandSoniqArtworkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = GrandSoniqTextMuted,
                            modifier = Modifier.size(92.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x66111514),
                                    Color(0x44111514),
                                    GrandSoniqBackground
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundActionIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        onClick = onBack,
                        containerColor = Color(0x88111514)
                    )
                    RoundActionIconButton(
                        icon = Icons.Outlined.Share,
                        contentDescription = "Chia sẻ nghệ sĩ",
                        onClick = onShare,
                        containerColor = Color(0x88111514)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = artist.name,
                                color = GrandSoniqTextPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (artist.isVerified) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Nghệ sĩ xác minh",
                                    tint = GrandSoniqAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${DateTimeUtils.formatFollowerCount(artist.followerCount)} người theo dõi",
                            color = GrandSoniqTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (state.isFollowing) GrandSoniqAccent else GrandSoniqDivider
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (state.isFollowing) GrandSoniqElevated else Color.Transparent,
                        contentColor = if (state.isFollowing) GrandSoniqAccent else GrandSoniqTextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (state.isFollowing) Icons.Filled.Check else Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (state.isFollowing) "Đang theo dõi" else "Theo dõi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
        }

        if (artist.bio.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GrandSoniqPanel)
                        .clickable(onClick = onToggleBio)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Giới thiệu",
                        color = GrandSoniqTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = artist.bio,
                        color = GrandSoniqTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = if (state.isBioExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.isBioExpanded) "Thu gọn" else "Xem thêm",
                        color = GrandSoniqAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bài hát nổi bật",
                    color = GrandSoniqTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                if (state.topSongs.size > 5) {
                    Text(
                        text = if (showAllSongs) "Thu gọn" else "Xem tất cả (${state.topSongs.size})",
                        color = GrandSoniqAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAllSongs = !showAllSongs }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (state.topSongs.isEmpty()) {
            item {
                Text(
                    text = "Chưa có bài hát nào của nghệ sĩ này.",
                    color = GrandSoniqTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            itemsIndexed(visibleSongs, key = { _, s -> s.id }) { index, song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackSnapshot.currentSong?.id == song.id,
                    isDownloaded = song.id in state.downloadedSongIds,
                    trackNumber = index + 1,
                    subtitleOverride = "${DateTimeUtils.formatFollowerCount(song.playCount)} lượt nghe",
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }

        if (state.albums.isNotEmpty()) {
            item {
                Text(
                    text = "Album & Đĩa đơn",
                    color = GrandSoniqTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.albums, key = { it.id }) { album ->
                        Column(
                            modifier = Modifier
                                .width(146.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAlbumClick(album.id) }
                        ) {
                            GrandSoniqArtwork(
                                url = album.coverUrl,
                                modifier = Modifier.size(146.dp),
                                shape = RoundedCornerShape(10.dp),
                                fallbackIcon = Icons.Rounded.Album
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = album.title,
                                color = GrandSoniqTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val year = DateTimeUtils.formatYear(album.releaseDate)
                            Text(
                                text = listOfNotNull(
                                    year.takeIf { it.isNotBlank() },
                                    "${album.songIds.size} bài"
                                ).joinToString(" · "),
                                color = GrandSoniqTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        if (state.relatedPlaylists.isNotEmpty()) {
            item {
                Text(
                    text = "Có sự góp mặt trong Playlist",
                    color = GrandSoniqTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.relatedPlaylists, key = { it.id }) { pl ->
                        Column(
                            modifier = Modifier
                                .width(146.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onPlaylistClick(pl.id) }
                        ) {
                            GrandSoniqArtwork(
                                url = pl.coverUrl,
                                modifier = Modifier.size(146.dp),
                                shape = RoundedCornerShape(10.dp),
                                fallbackIcon = Icons.AutoMirrored.Rounded.QueueMusic
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = pl.name,
                                color = GrandSoniqTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Tạo bởi ${pl.ownerName}",
                                color = GrandSoniqTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistSheetActionRow(
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
private fun ArtistDetailScreenPreview() {
    val sampleSongs = listOf(
        Song(id = "s1", title = "Sau cơn mưa", artistName = "An Nhiên", playCount = 1_240_000L, duration = 225_000L),
        Song(id = "s2", title = "Đi qua tháng mười", artistName = "An Nhiên", playCount = 860_000L, duration = 230_000L),
        Song(id = "s3", title = "Chiều thu", artistName = "An Nhiên", playCount = 412_000L, duration = 205_000L)
    )
    val state = ArtistDetailUiState(
        isLoading = false,
        artist = Artist(
            id = "art_1",
            name = "An Nhiên",
            bio = "Ca sĩ kiêm nhạc sĩ indie theo đuổi màu sắc acoustic nhẹ nhàng và những bản ballad sâu lắng về thành phố lúc mưa về.",
            followerCount = 345_200L,
            isVerified = true
        ),
        topSongs = sampleSongs,
        albums = listOf(
            Album(id = "alb_1", title = "Sau Cơn Mưa", artistName = "An Nhiên", songIds = List(8) { "s$it" }, releaseDate = 1_735_689_600_000L),
            Album(id = "alb_2", title = "Tháng Mười", artistName = "An Nhiên", songIds = List(6) { "s$it" }, releaseDate = 1_704_067_200_000L)
        ),
        relatedPlaylists = listOf(
            Playlist(id = "pl_1", name = "Đường về nhà", ownerName = "Minh Anh")
        ),
        downloadedSongIds = setOf("s1"),
        isFollowing = true
    )
    val playback = PlaybackUiSnapshot(
        currentSong = sampleSongs.first(),
        queue = sampleSongs,
        currentIndex = 0,
        isPlaying = true,
        progress = 0.28f
    )
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrandSoniqBackground)
        ) {
            ArtistDetailContent(
                state = state,
                playbackSnapshot = playback,
                onBack = {},
                onRetry = {},
                onShare = {},
                onToggleFollow = {},
                onToggleShuffle = {},
                onPlayAll = {},
                onToggleBio = {},
                onSongClick = {},
                onSongMoreClick = {},
                onAlbumClick = {},
                onPlaylistClick = {},
                modifier = Modifier.weight(1f)
            )
            MiniPlayerBar(
                song = sampleSongs.first(),
                isPlaying = true,
                progress = 0.28f,
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
