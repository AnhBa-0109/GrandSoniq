package ntu.ntth.grandsoniq.feature.library.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ntu.ntth.grandsoniq.feature.library.viewmodel.PlaylistDetailUiState
import ntu.ntth.grandsoniq.feature.library.viewmodel.PlaylistDetailViewModel
import ntu.ntth.grandsoniq.feature.player.service.PlaybackUiSnapshot
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackgroundGlow
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqDivider
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqElevated
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqOnAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedSongForSheet by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

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
        PlaylistDetailContent(
            state = state,
            playbackSnapshot = playbackSnapshot,
            onBack = onBack,
            onRetry = { viewModel.load() },
            onOpenInvite = { viewModel.setInviteDialogVisible(true) },
            onToggleSave = viewModel::toggleSavePlaylist,
            onToggleDownload = viewModel::toggleDownloadPlaylist,
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Nghe playlist \"${state.playlist.name}\" cùng mình trên GrandSoniq!"
                    )
                }
                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ playlist"))
            },
            onToggleShuffle = viewModel::toggleShuffle,
            onPlayAll = { viewModel.playAll(shuffle = playbackSnapshot.isShuffle) },
            onOpenAddSongs = { viewModel.setAddSongsSheetVisible(true) },
            onOpenEdit = { viewModel.setEditDialogVisible(true) },
            onDeletePlaylist = viewModel::deletePlaylist,
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

    if (state.showInviteDialog) {
        InviteCollaboratorDialog(
            onDismiss = { viewModel.setInviteDialogVisible(false) },
            onInvite = viewModel::inviteCollaborator
        )
    }

    if (state.showEditDialog) {
        CreatePlaylistDialog(
            initialName = state.playlist.name,
            initialDescription = state.playlist.description,
            initialIsPublic = state.playlist.isPublic,
            initialIsCollaborative = state.playlist.isCollaborative,
            isEditing = true,
            onDismiss = { viewModel.setEditDialogVisible(false) },
            onConfirm = viewModel::updatePlaylist
        )
    }

    if (state.showAddSongsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setAddSongsSheetVisible(false) },
            containerColor = GrandSoniqPanel,
            contentColor = GrandSoniqTextPrimary
        ) {
            Text(
                text = "Thêm bài hát vào playlist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            if (state.availableSongsToAdd.isEmpty()) {
                Text(
                    text = "Tất cả bài hát trên Firestore đã nằm trong playlist này.",
                    color = GrandSoniqTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.availableSongsToAdd, key = { it.id }) { song ->
                        ListItem(
                            headlineContent = {
                                Text(song.title, color = GrandSoniqTextPrimary, maxLines = 1)
                            },
                            supportingContent = {
                                Text(song.artistName, color = GrandSoniqTextMuted, maxLines = 1)
                            },
                            leadingContent = {
                                GrandSoniqArtwork(
                                    url = song.coverUrl,
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(7.dp),
                                    fallbackIcon = Icons.Rounded.MusicNote
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.addSongToPlaylist(song) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Thêm", tint = GrandSoniqAccent)
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = GrandSoniqPanel)
                        )
                    }
                }
            }
        }
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
                SheetActionRow(
                    icon = Icons.Filled.PlayArrow,
                    label = "Phát bài hát này",
                    onClick = {
                        viewModel.playSong(song)
                        selectedSongForSheet = null
                    }
                )
                SheetActionRow(
                    icon = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = if (isFav) "Bỏ khỏi bài hát đã thích" else "Thêm vào bài hát đã thích",
                    tint = if (isFav) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleFavoriteSong(song)
                        selectedSongForSheet = null
                    }
                )
                SheetActionRow(
                    icon = if (isDown) Icons.Filled.ArrowCircleDown else Icons.Outlined.DownloadForOffline,
                    label = if (isDown) "Xóa bản tải xuống" else "Tải về nghe offline",
                    tint = if (isDown) GrandSoniqAccent else GrandSoniqTextPrimary,
                    onClick = {
                        viewModel.toggleDownloadSong(song)
                        selectedSongForSheet = null
                    }
                )
                if (song.artistId.isNotBlank()) {
                    SheetActionRow(
                        icon = Icons.Rounded.Person,
                        label = "Xem hồ sơ nghệ sĩ (${song.artistName})",
                        onClick = {
                            selectedSongForSheet = null
                            onArtistClick(song.artistId)
                        }
                    )
                }
                if (song.albumId.isNotBlank()) {
                    SheetActionRow(
                        icon = Icons.Rounded.Album,
                        label = "Xem album",
                        onClick = {
                            selectedSongForSheet = null
                            onAlbumClick(song.albumId)
                        }
                    )
                }
                if (state.canEdit) {
                    SheetActionRow(
                        icon = Icons.Outlined.Delete,
                        label = "Xóa khỏi playlist",
                        tint = Color(0xFFFF7A7A),
                        onClick = {
                            viewModel.removeSongFromPlaylist(song)
                            selectedSongForSheet = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PlaylistDetailContent(
    state: PlaylistDetailUiState,
    playbackSnapshot: PlaybackUiSnapshot,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenInvite: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleDownload: () -> Unit,
    onShare: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPlayAll: () -> Unit,
    onOpenAddSongs: () -> Unit,
    onOpenEdit: () -> Unit,
    onDeletePlaylist: () -> Unit,
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
            PlaylistTopBar(
                onBack = onBack,
                showMore = false,
                menuExpanded = false,
                onToggleMenu = {},
                menuContent = {}
            )
            ErrorStateView(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    val playlist = state.playlist
    val coverUrl = playlist.coverUrl.ifBlank {
        state.songs.firstOrNull { it.coverUrl.isNotBlank() }?.coverUrl.orEmpty()
    }
    val totalMinutes = DateTimeUtils.formatTotalMinutes(state.songs.sumOf { it.duration })
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
                    PlaylistTopBar(
                        onBack = onBack,
                        showMore = true,
                        menuExpanded = showMenu,
                        onToggleMenu = { showMenu = it },
                        menuContent = {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = GrandSoniqPanel
                            ) {
                                if (state.canEdit) {
                                    DropdownMenuItem(
                                        text = { Text("Thêm bài hát", color = GrandSoniqTextPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Add, null, tint = GrandSoniqAccent)
                                        },
                                        onClick = {
                                            showMenu = false
                                            onOpenAddSongs()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Chỉnh sửa playlist", color = GrandSoniqTextPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Edit, null, tint = GrandSoniqTextPrimary)
                                        },
                                        onClick = {
                                            showMenu = false
                                            onOpenEdit()
                                        }
                                    )
                                }
                                if (state.isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Xóa playlist", color = Color(0xFFFF7A7A)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Delete, null, tint = Color(0xFFFF7A7A))
                                        },
                                        onClick = {
                                            showMenu = false
                                            onDeletePlaylist()
                                        }
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(6.dp))

                    GrandSoniqArtwork(
                        url = coverUrl,
                        modifier = Modifier.size(210.dp),
                        shape = RoundedCornerShape(14.dp),
                        fallbackIcon = Icons.AutoMirrored.Rounded.QueueMusic,
                        iconSizeFraction = 0.36f
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = playlist.name,
                        color = GrandSoniqTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = buildString {
                            append("Tạo bởi ${playlist.ownerName.ifBlank { "GrandSoniq" }}")
                            append(" · ${state.songs.size} bài hát")
                            if (totalMinutes > 0L) append(" · $totalMinutes phút")
                        },
                        color = GrandSoniqTextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    if (playlist.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = playlist.description,
                            color = GrandSoniqTextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(GrandSoniqElevated)
                            .clickable(onClick = onOpenInvite)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = "Mời bạn",
                            tint = GrandSoniqTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (playlist.collaboratorIds.isEmpty()) {
                                "Mời bạn"
                            } else {
                                "Mời bạn (${playlist.collaboratorIds.size})"
                            },
                            color = GrandSoniqTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                                    contentDescription = "Lưu playlist",
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
                                    contentDescription = "Tải playlist",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Playlist chưa có bài hát nào.",
                        color = GrandSoniqTextMuted,
                        fontSize = 14.sp
                    )
                    if (state.canEdit) {
                        Button(
                            onClick = onOpenAddSongs,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrandSoniqElevated,
                                contentColor = GrandSoniqAccent
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Thêm bài hát")
                        }
                    }
                }
            }
        } else {
            items(state.songs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackSnapshot.currentSong?.id == song.id,
                    isDownloaded = song.id in state.downloadedSongIds,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistTopBar(
    onBack: () -> Unit,
    showMore: Boolean,
    menuExpanded: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    menuContent: @Composable () -> Unit
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
        if (showMore) {
            Box {
                IconButton(onClick = { onToggleMenu(!menuExpanded) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = GrandSoniqTextPrimary
                    )
                }
                menuContent()
            }
        }
    }
}

@Composable
private fun SheetActionRow(
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

@Composable
private fun InviteCollaboratorDialog(
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrandSoniqPanel,
        titleContentColor = GrandSoniqTextPrimary,
        title = { Text("Mời bạn cộng tác", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Nhập email hoặc UID tài khoản trên Firestore để cấp quyền thêm/xóa bài hát trong playlist này.",
                    color = GrandSoniqTextMuted,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Email hoặc UID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrandSoniqAccent,
                        unfocusedBorderColor = GrandSoniqDivider,
                        focusedTextColor = GrandSoniqTextPrimary,
                        unfocusedTextColor = GrandSoniqTextPrimary,
                        cursorColor = GrandSoniqAccent,
                        focusedLabelColor = GrandSoniqAccent,
                        unfocusedLabelColor = GrandSoniqTextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onInvite(input) },
                enabled = input.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrandSoniqAccent,
                    contentColor = GrandSoniqOnAccent
                )
            ) {
                Text("Mời", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = GrandSoniqTextMuted)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111514, heightDp = 820)
@Composable
private fun PlaylistDetailScreenPreview() {
    val sampleSongs = listOf(
        Song(id = "s1", title = "Sau cơn mưa", artistName = "An Nhiên", duration = 225_000L),
        Song(id = "s2", title = "Đèn phố", artistName = "Hải Nam", duration = 215_000L),
        Song(id = "s3", title = "Một ngày nhẹ tênh", artistName = "Mây Xanh", duration = 240_000L),
        Song(id = "s4", title = "Chạm mùa hè", artistName = "Vệt Nắng", duration = 198_000L),
        Song(id = "s5", title = "Đi qua tháng mười", artistName = "An Nhiên", duration = 230_000L)
    )
    val state = PlaylistDetailUiState(
        isLoading = false,
        playlist = Playlist(
            id = "pl_1",
            name = "Đường về nhà",
            ownerName = "Minh Anh",
            songIds = sampleSongs.map { it.id },
            isCollaborative = true
        ),
        songs = sampleSongs,
        downloadedSongIds = setOf("s1", "s3"),
        isSaved = true,
        isOwner = true,
        canEdit = true
    )
    val playback = PlaybackUiSnapshot(
        currentSong = sampleSongs.first(),
        queue = sampleSongs,
        currentIndex = 0,
        isPlaying = true,
        progress = 0.32f
    )
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrandSoniqBackground)
        ) {
            PlaylistDetailContent(
                state = state,
                playbackSnapshot = playback,
                onBack = {},
                onRetry = {},
                onOpenInvite = {},
                onToggleSave = {},
                onToggleDownload = {},
                onShare = {},
                onToggleShuffle = {},
                onPlayAll = {},
                onOpenAddSongs = {},
                onOpenEdit = {},
                onDeletePlaylist = {},
                onSongClick = {},
                onSongMoreClick = {},
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
