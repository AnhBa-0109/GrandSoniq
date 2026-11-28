package ntu.ntth.grandsoniq.feature.library.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ntu.ntth.grandsoniq.core.model.LocalDeviceSong
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.feature.library.data.LibraryRepository
import ntu.ntth.grandsoniq.feature.library.data.LocalMusicScanner
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    private val localMusicScanner: LocalMusicScanner,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadLibraryOverview() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = data.user,
                            playlists = data.playlists,
                            favoriteSongs = data.favoriteSongs,
                            recentSongs = data.recentSongs,
                            savedAlbums = data.savedAlbums,
                            followingArtists = data.followingArtists
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Không thể tải thư viện từ Firestore"
                        )
                    }
                }
        }
    }

    fun selectFilter(filter: LibraryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        if (filter == LibraryFilter.LOCAL && _uiState.value.localDeviceSongs.isEmpty()) {
            scanLocalMusic()
        }
    }

    fun toggleSearch() {
        _uiState.update { state ->
            val nextActive = !state.isSearchActive
            state.copy(
                isSearchActive = nextActive,
                searchQuery = if (!nextActive) "" else state.searchQuery
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCreatePlaylistDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showCreatePlaylistDialog = visible) }
    }

    fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        isCollaborative: Boolean
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.createPlaylist(name, description, isPublic, isCollaborative)
            }.onSuccess { created ->
                _uiState.update {
                    it.copy(
                        playlists = listOf(created) + it.playlists,
                        showCreatePlaylistDialog = false,
                        feedbackMessage = "Đã tạo playlist \"${created.name}\""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(feedbackMessage = err.localizedMessage ?: "Không thể tạo playlist")
                }
            }
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        playbackController.playSong(song, queue)
        viewModelScope.launch { repository.recordSongPlayed(song.id) }
    }

    fun playFavorites() {
        val favs = _uiState.value.favoriteSongs
        if (favs.isNotEmpty()) {
            playbackController.playList(favs, shuffle = false)
            viewModelScope.launch { repository.recordSongPlayed(favs.first().id) }
        }
    }

    fun playLocalSong(localSong: LocalDeviceSong) {
        val mapped = Song(
            id = "local_${localSong.localId}",
            title = localSong.title,
            artistName = localSong.artistName,
            audioUrl = localSong.filePath,
            duration = localSong.durationMs
        )
        playbackController.playSong(mapped, listOf(mapped))
    }

    fun toggleFavoriteSong(song: Song) {
        val isFav = _uiState.value.favoriteSongs.any { it.id == song.id }
        viewModelScope.launch {
            runCatching { repository.toggleFavoriteSong(song.id, isFav) }
                .onSuccess { nowFav ->
                    _uiState.update { state ->
                        val updatedFavs = if (nowFav) {
                            listOf(song) + state.favoriteSongs.filterNot { it.id == song.id }
                        } else {
                            state.favoriteSongs.filterNot { it.id == song.id }
                        }
                        state.copy(
                            favoriteSongs = updatedFavs,
                            feedbackMessage = if (nowFav) "Đã thêm vào Yêu thích" else "Đã bỏ khỏi Yêu thích"
                        )
                    }
                }
        }
    }

    fun scanLocalMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningLocal = true) }
            val songs = localMusicScanner.scanDeviceSongs()
            _uiState.update {
                it.copy(
                    isScanningLocal = false,
                    localDeviceSongs = songs,
                    feedbackMessage = "Tìm thấy ${songs.size} bài nhạc trên thiết bị"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
