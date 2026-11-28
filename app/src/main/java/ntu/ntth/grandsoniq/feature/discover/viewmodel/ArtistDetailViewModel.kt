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
import kotlin.math.max

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val artistId: String = savedStateHandle.get<String>("artistId").orEmpty()

    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        if (artistId.isNotBlank()) {
            load(artistId)
        }
    }

    fun load(id: String = artistId) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadArtistProfile(id) }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            artist = data.artist,
                            topSongs = data.topSongs,
                            albums = data.albums,
                            relatedPlaylists = data.relatedPlaylists,
                            isFollowing = data.isFollowing,
                            favoriteSongIds = data.favoriteSongIds
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "Không thể tải hồ sơ nghệ sĩ"
                        )
                    }
                }
        }
    }

    fun toggleFollow() {
        val current = _uiState.value
        if (current.artist.id.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.toggleFollowArtist(current.artist.id, current.isFollowing) }
                .onSuccess { nowFollowing ->
                    _uiState.update { state ->
                        val delta = if (nowFollowing) 1L else -1L
                        val updatedCount = max(0L, state.artist.followerCount + delta)
                        state.copy(
                            isFollowing = nowFollowing,
                            artist = state.artist.copy(followerCount = updatedCount),
                            feedbackMessage = if (nowFollowing) {
                                "Đã theo dõi ${state.artist.name}"
                            } else {
                                "Đã bỏ theo dõi ${state.artist.name}"
                            }
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(feedbackMessage = err.localizedMessage ?: "Không thể cập nhật theo dõi")
                    }
                }
        }
    }

    fun playAll(shuffle: Boolean = false) {
        val songs = _uiState.value.topSongs
        if (songs.isEmpty()) return
        playbackController.playList(songs, shuffle = shuffle)
        viewModelScope.launch { repository.recordSongPlayed(songs.first().id) }
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.topSongs
        playbackController.playSong(song, songs)
        viewModelScope.launch { repository.recordSongPlayed(song.id) }
    }

    fun toggleShuffle() {
        val songs = _uiState.value.topSongs
        playbackController.toggleShuffle()
        if (playbackController.snapshot.value.currentSong == null && songs.isNotEmpty()) {
            playAll(shuffle = true)
        }
    }

    fun toggleBioExpanded() {
        _uiState.update { it.copy(isBioExpanded = !it.isBioExpanded) }
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

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
