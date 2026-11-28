package ntu.ntth.grandsoniq.feature.library.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.feature.library.data.LibraryRepository
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val playlistId: String = savedStateHandle.get<String>("playlistId").orEmpty()

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        if (playlistId.isNotBlank()) {
            load(playlistId)
        }
    }

    fun load(id: String = playlistId) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadPlaylistDetail(id) }
                .onSuccess { data ->
                    val uid = repository.currentUid()
                    val isOwner = data.playlist.ownerId == uid
                    val canEdit = isOwner || data.playlist.collaboratorIds.contains(uid)
                    val existingIds = data.songs.map { it.id }.toSet()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            playlist = data.playlist,
                            songs = data.songs,
                            availableSongsToAdd = data.availableSongs.filterNot { s -> s.id in existingIds },
                            favoriteSongIds = data.favoriteSongIds,
                            isSaved = data.isSaved,
                            isOwner = isOwner,
                            canEdit = canEdit
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Không thể tải playlist"
                        )
                    }
                }
        }
    }

    fun playAll(shuffle: Boolean = false) {
        val songs = _uiState.value.songs
        if (songs.isEmpty()) return
        playbackController.playList(songs, shuffle = shuffle)
        viewModelScope.launch { repository.recordSongPlayed(songs.first().id) }
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.songs
        playbackController.playSong(song, songs)
        viewModelScope.launch { repository.recordSongPlayed(song.id) }
    }

    fun toggleShuffle() {
        val songs = _uiState.value.songs
        playbackController.toggleShuffle()
        if (playbackController.snapshot.value.currentSong == null && songs.isNotEmpty()) {
            playAll(shuffle = true)
        }
    }

    fun toggleSavePlaylist() {
        val current = _uiState.value
        if (current.playlist.id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.toggleSavePlaylist(current.playlist.id, current.isSaved) }
                .onSuccess { saved ->
                    _uiState.update {
                        it.copy(
                            isSaved = saved,
                            feedbackMessage = if (saved) "Đã lưu playlist vào thư viện" else "Đã bỏ lưu playlist"
                        )
                    }
                }
        }
    }

    fun toggleDownloadPlaylist() {
        val current = _uiState.value
        val allIds = current.songs.map { it.id }.toSet()
        val allDownloaded = allIds.isNotEmpty() && current.downloadedSongIds.containsAll(allIds)
        _uiState.update {
            it.copy(
                downloadedSongIds = if (allDownloaded) emptySet() else allIds,
                feedbackMessage = if (allDownloaded) "Đã xóa bản tải xuống của playlist" else "Đang tải playlist về để nghe offline"
            )
        }
    }

    fun toggleDownloadSong(song: Song) {
        _uiState.update { state ->
            val exists = song.id in state.downloadedSongIds
            val next = if (exists) state.downloadedSongIds - song.id else state.downloadedSongIds + song.id
            state.copy(
                downloadedSongIds = next,
                feedbackMessage = if (exists) "Đã xóa bản tải của ${song.title}" else "Đã tải ${song.title} để nghe offline"
            )
        }
    }

    fun toggleFavoriteSong(song: Song) {
        val isFav = song.id in _uiState.value.favoriteSongIds
        viewModelScope.launch {
            runCatching { repository.toggleFavoriteSong(song.id, isFav) }
                .onSuccess { nowFav ->
                    _uiState.update { state ->
                        val next = if (nowFav) state.favoriteSongIds + song.id else state.favoriteSongIds - song.id
                        state.copy(
                            favoriteSongIds = next,
                            feedbackMessage = if (nowFav) "Đã thêm vào Yêu thích" else "Đã bỏ khỏi Yêu thích"
                        )
                    }
                }
        }
    }

    fun setInviteDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showInviteDialog = visible) }
    }

    fun inviteCollaborator(emailOrUid: String) {
        val id = _uiState.value.playlist.id
        if (id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.inviteCollaborator(id, emailOrUid) }
                .onSuccess { invitedName ->
                    _uiState.update {
                        it.copy(
                            showInviteDialog = false,
                            feedbackMessage = "Đã mời $invitedName cộng tác playlist"
                        )
                    }
                    load(id)
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(feedbackMessage = err.localizedMessage ?: "Không thể mời bạn")
                    }
                }
        }
    }

    fun setEditDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showEditDialog = visible) }
    }

    fun updatePlaylist(name: String, description: String, isPublic: Boolean, isCollaborative: Boolean) {
        val id = _uiState.value.playlist.id
        if (id.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.updatePlaylist(id, name, description, isPublic, isCollaborative) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            playlist = state.playlist.copy(
                                name = name.trim(),
                                description = description.trim(),
                                isPublic = isPublic,
                                isCollaborative = isCollaborative
                            ),
                            showEditDialog = false,
                            feedbackMessage = "Đã cập nhật playlist"
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(feedbackMessage = err.localizedMessage ?: "Lỗi cập nhật") }
                }
        }
    }

    fun setAddSongsSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showAddSongsSheet = visible) }
    }

    fun addSongToPlaylist(song: Song) {
        val id = _uiState.value.playlist.id
        if (id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.addSongToPlaylist(id, song) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            songs = state.songs + song,
                            availableSongsToAdd = state.availableSongsToAdd.filterNot { it.id == song.id },
                            feedbackMessage = "Đã thêm \"${song.title}\" vào playlist"
                        )
                    }
                }
        }
    }

    fun removeSongFromPlaylist(song: Song) {
        val id = _uiState.value.playlist.id
        if (id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.removeSongFromPlaylist(id, song.id) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            songs = state.songs.filterNot { it.id == song.id },
                            availableSongsToAdd = listOf(song) + state.availableSongsToAdd,
                            feedbackMessage = "Đã xóa \"${song.title}\" khỏi playlist"
                        )
                    }
                }
        }
    }

    fun deletePlaylist() {
        val id = _uiState.value.playlist.id
        if (id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.deletePlaylist(id) }
                .onSuccess {
                    _uiState.update { it.copy(isDeleted = true) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(feedbackMessage = err.localizedMessage ?: "Không thể xóa playlist") }
                }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
