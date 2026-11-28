package ntu.ntth.grandsoniq.feature.discover.viewmodel

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
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val albumId: String = savedStateHandle.get<String>("albumId").orEmpty()

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        if (albumId.isNotBlank()) {
            load(albumId)
        }
    }

    fun load(id: String = albumId) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadAlbumDetail(id) }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            album = data.album,
                            artist = data.artist,
                            songs = data.songs,
                            isSaved = data.isSaved,
                            favoriteSongIds = data.favoriteSongIds
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Không thể tải album từ Firestore"
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

    fun toggleSaveAlbum() {
        val current = _uiState.value
        if (current.album.id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.toggleSaveAlbum(current.album.id, current.isSaved) }
                .onSuccess { saved ->
                    _uiState.update {
                        it.copy(
                            isSaved = saved,
                            feedbackMessage = if (saved) "Đã lưu album vào thư viện" else "Đã bỏ lưu album"
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(feedbackMessage = err.localizedMessage ?: "Lỗi lưu album") }
                }
        }
    }

    fun toggleDownloadAlbum() {
        val current = _uiState.value
        val allIds = current.songs.map { it.id }.toSet()
        val allDownloaded = allIds.isNotEmpty() && current.downloadedSongIds.containsAll(allIds)
        _uiState.update {
            it.copy(
                downloadedSongIds = if (allDownloaded) emptySet() else allIds,
                feedbackMessage = if (allDownloaded) "Đã xóa bản tải của album" else "Đang tải album về để nghe offline"
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

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
