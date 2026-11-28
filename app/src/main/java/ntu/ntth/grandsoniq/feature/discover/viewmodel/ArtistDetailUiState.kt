package ntu.ntth.grandsoniq.feature.discover.viewmodel

import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val artist: Artist = Artist(),
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val relatedPlaylists: List<Playlist> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val downloadedSongIds: Set<String> = emptySet(),
    val isFollowing: Boolean = false,
    val isBioExpanded: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null
)
