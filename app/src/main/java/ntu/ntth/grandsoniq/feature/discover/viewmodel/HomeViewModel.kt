package ntu.ntth.grandsoniq.feature.discover.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import ntu.ntth.grandsoniq.core.network.toAlbumSafe
import ntu.ntth.grandsoniq.core.network.toArtistSafe
import ntu.ntth.grandsoniq.core.network.toPlaylistSafe
import ntu.ntth.grandsoniq.core.network.toSongSafe
import ntu.ntth.grandsoniq.feature.discover.data.DiscoverRepository

internal class HomeViewModel : ViewModel() {
    private val repository = DiscoverRepository()
    var state by mutableStateOf(HomeUiState())
        private set
    private var cleared = false

    init {
        reload()
    }

    fun reload() {
        state = state.copy(isLoading = true, errorCodes = emptyList())
        val songs = repository.songs()
        val albums = repository.albums()
        val artists = repository.artists()
        val playlists = repository.playlists()

        Tasks.whenAllComplete(songs, albums, artists, playlists).addOnSuccessListener {
            if (cleared) return@addOnSuccessListener
            state = HomeUiState(
                isLoading = false,
                songs = songs.documents().mapNotNull { it.toSongSafe() },
                albums = albums.documents().mapNotNull { it.toAlbumSafe() },
                artists = artists.documents().mapNotNull { it.toArtistSafe() },
                playlists = playlists.documents().mapNotNull { it.toPlaylistSafe() }
                    .filter { it.isPublic },
                errorCodes = listOf(songs, albums, artists, playlists)
                    .filterNot { it.isSuccessful }
                    .map { (it.exception as? FirebaseFirestoreException)?.code?.name ?: "UNKNOWN" }
                    .distinct()
            )
        }
    }

    override fun onCleared() {
        cleared = true
    }

    private fun Task<QuerySnapshot>.documents() =
        if (isSuccessful) result.toList() else emptyList()
}
