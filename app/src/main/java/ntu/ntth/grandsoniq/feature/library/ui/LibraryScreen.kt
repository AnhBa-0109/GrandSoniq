package ntu.ntth.grandsoniq.feature.library.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ntu.ntth.grandsoniq.core.common.DateTimeUtils
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.LocalDeviceSong
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.model.User
import ntu.ntth.grandsoniq.core.ui.ErrorStateView
import ntu.ntth.grandsoniq.core.ui.GrandSoniqArtwork
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomNavBar
import ntu.ntth.grandsoniq.core.ui.LoadingStateView
import ntu.ntth.grandsoniq.core.ui.MainNavTab
import ntu.ntth.grandsoniq.core.ui.MiniPlayerBar
import ntu.ntth.grandsoniq.core.ui.SongListItem
import ntu.ntth.grandsoniq.feature.library.viewmodel.LibraryFilter
import ntu.ntth.grandsoniq.feature.library.viewmodel.LibraryUiState
import ntu.ntth.grandsoniq.feature.library.viewmodel.LibraryViewModel
import ntu.ntth.grandsoniq.feature.player.service.PlaybackUiSnapshot
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqArtworkBg
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqDivider
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqElevated
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqOnAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@Composable
fun LibraryScreen(
    playbackSnapshot: PlaybackUiSnapshot,
    onPlaylistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        LibraryContent(
            state = state,
            currentPlayingSongId = playbackSnapshot.currentSong?.id,
            onToggleSearch = viewModel::toggleSearch,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onFilterSelect = viewModel::selectFilter,
            onOpenCreatePlaylist = { viewModel.setCreatePlaylistDialogVisible(true) },
            onRetry = viewModel::refresh,
            onPlayFavorites = viewModel::playFavorites,
            onPlaySong = viewModel::playSong,
            onPlayLocalSong = viewModel::playLocalSong,
            onToggleFavoriteSong = viewModel::toggleFavoriteSong,
            onScanLocal = viewModel::scanLocalMusic,
            onPlaylistClick = onPlaylistClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }

    if (state.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.setCreatePlaylistDialogVisible(false) },
            onConfirm = viewModel::createPlaylist
        )
    }
}

@Composable
internal fun LibraryContent(
    state: LibraryUiState,
    currentPlayingSongId: String?,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelect: (LibraryFilter) -> Unit,
    onOpenCreatePlaylist: () -> Unit,
    onRetry: () -> Unit,
    onPlayFavorites: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayLocalSong: (LocalDeviceSong) -> Unit,
    onToggleFavoriteSong: (Song) -> Unit,
    onScanLocal: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query = state.searchQuery.trim().lowercase()
    val filteredPlaylists = remember(state.playlists, query) {
        if (query.isEmpty()) state.playlists
        else state.playlists.filter {
            it.name.lowercase().contains(query) || it.ownerName.lowercase().contains(query)
        }
    }
    val filteredAlbums = remember(state.savedAlbums, query) {
        if (query.isEmpty()) state.savedAlbums
        else state.savedAlbums.filter {
            it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }
    }
    val filteredArtists = remember(state.followingArtists, query) {
        if (query.isEmpty()) state.followingArtists
        else state.followingArtists.filter { it.name.lowercase().contains(query) }
    }
    val filteredFavorites = remember(state.favoriteSongs, query) {
        if (query.isEmpty()) state.favoriteSongs
        else state.favoriteSongs.filter {
            it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }
    }
    val filteredRecent = remember(state.recentSongs, query) {
        if (query.isEmpty()) state.recentSongs
        else state.recentSongs.filter {
            it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }
    }
    val filteredLocal = remember(state.localDeviceSongs, query) {
        if (query.isEmpty()) state.localDeviceSongs
        else state.localDeviceSongs.filter {
            it.title.lowercase().contains(query) || it.artistName.lowercase().contains(query)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LibraryTopBar(
                avatarUrl = state.user.avatarUrl,
                isSearchActive = state.isSearchActive,
                onToggleSearch = onToggleSearch,
                onCreateClick = onOpenCreatePlaylist
            )
        }

        if (state.isSearchActive) {
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Tìm trong thư viện...", color = GrandSoniqTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = GrandSoniqTextMuted)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Xóa", tint = GrandSoniqTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrandSoniqAccent,
                        unfocusedBorderColor = GrandSoniqDivider,
                        focusedContainerColor = GrandSoniqPanel,
                        unfocusedContainerColor = GrandSoniqPanel,
                        focusedTextColor = GrandSoniqTextPrimary,
                        unfocusedTextColor = GrandSoniqTextPrimary,
                        cursorColor = GrandSoniqAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryFilter.entries) { filter ->
                    val selected = filter == state.selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) GrandSoniqAccent else GrandSoniqPanel)
                            .clickable { onFilterSelect(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.label,
                            color = if (selected) GrandSoniqOnAccent else GrandSoniqTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            item {
                LoadingStateView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                )
            }
            return@LazyColumn
        }

        if (state.errorMessage != null) {
            item {
                ErrorStateView(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return@LazyColumn
        }

        val showAll = state.selectedFilter == LibraryFilter.ALL

        if (showAll || state.selectedFilter == LibraryFilter.FAVORITES) {
            item {
                LikedSongsCard(
                    count = state.favoriteSongs.size,
                    onClick = {
                        if (showAll) onFilterSelect(LibraryFilter.FAVORITES) else onPlayFavorites()
                    },
                    onPlay = onPlayFavorites
                )
            }
        }

        if ((showAll || state.selectedFilter == LibraryFilter.PLAYLISTS) && filteredPlaylists.isNotEmpty()) {
            item {
                LibrarySectionHeader(
                    title = "Playlist",
                    trailingAction = "Tạo mới",
                    onTrailingClick = onOpenCreatePlaylist
                )
            }
            items(filteredPlaylists, key = { "pl_${it.id}" }) { playlist ->
                PlaylistRowItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist.id) }
                )
            }
        }

        if ((showAll || state.selectedFilter == LibraryFilter.ALBUMS) && filteredAlbums.isNotEmpty()) {
            item { LibrarySectionHeader(title = "Album đã lưu") }
            items(filteredAlbums, key = { "alb_${it.id}" }) { album ->
                AlbumRowItem(
                    album = album,
                    onClick = { onAlbumClick(album.id) }
                )
            }
        }

        if ((showAll || state.selectedFilter == LibraryFilter.ARTISTS) && filteredArtists.isNotEmpty()) {
            item { LibrarySectionHeader(title = "Nghệ sĩ đang theo dõi") }
            items(filteredArtists, key = { "art_${it.id}" }) { artist ->
                ArtistRowItem(
                    artist = artist,
                    onClick = { onArtistClick(artist.id) }
                )
            }
        }

        if (state.selectedFilter == LibraryFilter.FAVORITES) {
            if (filteredFavorites.isEmpty()) {
                item {
                    EmptyFilterMessage("Chưa có bài hát yêu thích nào.")
                }
            } else {
                items(filteredFavorites, key = { "fav_${it.id}" }) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = song.id == currentPlayingSongId,
                        onClick = { onPlaySong(song, filteredFavorites) },
                        onMoreClick = { onToggleFavoriteSong(song) }
                    )
                }
            }
        }

        if (showAll && filteredRecent.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = GrandSoniqAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Nghe gần đây",
                        color = GrandSoniqTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(filteredRecent.take(8), key = { "rec_${it.id}" }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { onPlaySong(song, filteredRecent) },
                    onMoreClick = { onToggleFavoriteSong(song) }
                )
            }
        }

        if (state.selectedFilter == LibraryFilter.LOCAL) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = GrandSoniqAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nhạc trên thiết bị (${filteredLocal.size})",
                            color = GrandSoniqTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onScanLocal,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrandSoniqElevated,
                            contentColor = GrandSoniqAccent
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Quét máy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (state.isScanningLocal) {
                item { LoadingStateView(Modifier.fillMaxWidth().padding(24.dp)) }
            } else if (filteredLocal.isEmpty()) {
                item {
                    EmptyFilterMessage("Không tìm thấy tệp âm thanh trên bộ nhớ hoặc chưa cấp quyền đọc nhạc.")
                }
            } else {
                items(filteredLocal, key = { "loc_${it.localId}" }) { localSong ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayLocalSong(localSong) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GrandSoniqArtworkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = GrandSoniqAccent
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = localSong.title,
                                color = GrandSoniqTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${localSong.artistName} · ${DateTimeUtils.formatTrackDuration(localSong.durationMs)}",
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
private fun LibraryTopBar(
    avatarUrl: String,
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    onCreateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GrandSoniqArtwork(
            url = avatarUrl,
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            fallbackIcon = Icons.Rounded.Person,
            iconSizeFraction = 0.55f
        )
        Text(
            text = "Thư viện",
            color = GrandSoniqTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleSearch) {
            Icon(
                imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Outlined.Search,
                contentDescription = "Tìm trong thư viện",
                tint = GrandSoniqTextPrimary
            )
        }
        IconButton(onClick = onCreateClick) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tạo playlist mới",
                tint = GrandSoniqAccent,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun LikedSongsCard(
    count: Int,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GrandSoniqPanel)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GrandSoniqElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = GrandSoniqAccent,
                modifier = Modifier.size(26.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bài hát đã thích",
                color = GrandSoniqTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$count bài hát",
                color = GrandSoniqTextMuted,
                fontSize = 13.sp
            )
        }
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GrandSoniqAccent)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Phát nhạc đã thích",
                    tint = GrandSoniqOnAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun LibrarySectionHeader(
    title: String,
    trailingAction: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = GrandSoniqTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (trailingAction != null && onTrailingClick != null) {
            Text(
                text = trailingAction,
                color = GrandSoniqAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTrailingClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun PlaylistRowItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GrandSoniqArtwork(
            url = playlist.coverUrl,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(10.dp),
            fallbackIcon = Icons.AutoMirrored.Rounded.QueueMusic
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = playlist.name,
                color = GrandSoniqTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (playlist.isCollaborative) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = "Cộng tác",
                        tint = GrandSoniqAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "Playlist · ${playlist.ownerName} · ${playlist.songIds.size} bài",
                    color = GrandSoniqTextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = GrandSoniqTextMuted
        )
    }
}

@Composable
private fun AlbumRowItem(
    album: Album,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GrandSoniqArtwork(
            url = album.coverUrl,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(10.dp),
            fallbackIcon = Icons.Rounded.Album
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = album.title,
                color = GrandSoniqTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Album · ${album.artistName} · ${album.songIds.size} bài",
                color = GrandSoniqTextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = GrandSoniqTextMuted
        )
    }
}

@Composable
private fun ArtistRowItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GrandSoniqArtwork(
            url = artist.avatarUrl,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            fallbackIcon = Icons.Rounded.Person
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = artist.name,
                    color = GrandSoniqTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Nghệ sĩ xác minh",
                        tint = GrandSoniqAccent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = "Nghệ sĩ · ${DateTimeUtils.formatFollowerCount(artist.followerCount)} người theo dõi",
                color = GrandSoniqTextMuted,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = GrandSoniqTextMuted
        )
    }
}

@Composable
private fun EmptyFilterMessage(text: String) {
    Text(
        text = text,
        color = GrandSoniqTextMuted,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111514, heightDp = 820)
@Composable
private fun LibraryScreenPreview() {
    val sampleSongs = listOf(
        Song(id = "s1", title = "Sau cơn mưa", artistName = "An Nhiên", duration = 225_000L),
        Song(id = "s2", title = "Đèn phố", artistName = "Hải Nam", duration = 210_000L)
    )
    val sampleState = LibraryUiState(
        isLoading = false,
        user = User(uid = "u1", displayName = "Minh Anh"),
        playlists = listOf(
            Playlist(
                id = "pl_1",
                name = "Đường về nhà",
                ownerName = "Minh Anh",
                songIds = List(14) { "s$it" },
                isCollaborative = true
            ),
            Playlist(
                id = "pl_2",
                name = "Nhạc chill code đêm",
                ownerName = "GrandSoniq",
                songIds = List(20) { "s$it" }
            )
        ),
        savedAlbums = listOf(
            Album(id = "alb_1", title = "Phố đêm", artistName = "Hải Nam", songIds = List(8) { "s$it" })
        ),
        followingArtists = listOf(
            Artist(id = "art_1", name = "An Nhiên", followerCount = 124_500L, isVerified = true)
        ),
        favoriteSongs = sampleSongs,
        recentSongs = sampleSongs
    )
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrandSoniqBackground)
        ) {
            LibraryContent(
                state = sampleState,
                currentPlayingSongId = "s1",
                onToggleSearch = {},
                onSearchQueryChange = {},
                onFilterSelect = {},
                onOpenCreatePlaylist = {},
                onRetry = {},
                onPlayFavorites = {},
                onPlaySong = { _, _ -> },
                onPlayLocalSong = {},
                onToggleFavoriteSong = {},
                onScanLocal = {},
                onPlaylistClick = {},
                onAlbumClick = {},
                onArtistClick = {},
                modifier = Modifier.weight(1f)
            )
            MiniPlayerBar(
                song = sampleSongs.first(),
                isPlaying = true,
                progress = 0.32f,
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
