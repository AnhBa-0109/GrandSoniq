package ntu.ntth.grandsoniq.feature.discover.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Song
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

        Tasks.whenAllComplete(songs, albums, artists).addOnSuccessListener {
            if (cleared) return@addOnSuccessListener
            state = HomeUiState(
                isLoading = false,
                songs = songs.documents().map { doc ->
                    val fields = doc.data
                    Song(
                        id = doc.id,
                        title = fields["title"] as? String ?: doc.id,
                        artistName = (fields["artistName"] as? String).orEmpty(),
                        coverUrl = (fields["coverUrl"] as? String).orEmpty(),
                        genre = (fields["genre"] as? String).orEmpty()
                    )
                },
                albums = albums.documents().map { doc ->
                    val fields = doc.data
                    Album(
                        id = doc.id,
                        title = fields["title"] as? String ?: doc.id,
                        artistName = (fields["artistName"] as? String).orEmpty(),
                        coverUrl = (fields["coverUrl"] as? String).orEmpty()
                    )
                },
                artists = artists.documents().map { doc ->
                    val fields = doc.data
                    Artist(
                        id = doc.id,
                        name = fields["name"] as? String ?: doc.id,
                        avatarUrl = (fields["avatarUrl"] as? String).orEmpty()
                    )
                },
                errorCodes = listOf(songs, albums, artists).filterNot { it.isSuccessful }
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
