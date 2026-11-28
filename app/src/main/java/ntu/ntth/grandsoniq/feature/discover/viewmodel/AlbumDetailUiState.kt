package ntu.ntth.grandsoniq.feature.discover.viewmodel

import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Song

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: Album = Album(),
    val artist: Artist? = null,
    val songs: List<Song> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val downloadedSongIds: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null
)
