package ntu.ntth.grandsoniq.feature.library.viewmodel

import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.LocalDeviceSong
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.model.User

enum class LibraryFilter(val label: String) {
    ALL("Tất cả"),
    PLAYLISTS("Playlist"),
    ALBUMS("Album"),
    ARTISTS("Nghệ sĩ"),
    FAVORITES("Yêu thích"),
    LOCAL("Trên máy")
}

data class LibraryUiState(
    val isLoading: Boolean = true,
    val user: User = User(),
    val selectedFilter: LibraryFilter = LibraryFilter.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val savedAlbums: List<Album> = emptyList(),
    val followingArtists: List<Artist> = emptyList(),
    val localDeviceSongs: List<LocalDeviceSong> = emptyList(),
    val isScanningLocal: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null
)
