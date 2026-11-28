package ntu.ntth.grandsoniq.feature.library.viewmodel

import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song

data class PlaylistDetailUiState(
    val isLoading: Boolean = true,
    val playlist: Playlist = Playlist(),
    val songs: List<Song> = emptyList(),
    val availableSongsToAdd: List<Song> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val downloadedSongIds: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val isOwner: Boolean = false,
    val canEdit: Boolean = false,
    val showInviteDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showAddSongsSheet: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null
)
