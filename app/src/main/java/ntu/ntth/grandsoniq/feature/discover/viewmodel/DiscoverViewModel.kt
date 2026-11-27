package ntu.ntth.grandsoniq.feature.discover.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.network.toSongSafe
import ntu.ntth.grandsoniq.feature.discover.data.DiscoverRepository

internal data class DiscoverGenre(val id: String, val name: String)

internal data class DiscoverList<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

internal class DiscoverViewModel : ViewModel() {
    private val repository = DiscoverRepository()
    private var genreLoad: Job? = null

    var chart by mutableStateOf(DiscoverList<Song>())
        private set
    var releases by mutableStateOf(DiscoverList<Song>())
        private set
    var genres by mutableStateOf(DiscoverList<DiscoverGenre>())
        private set
    var genreSongs by mutableStateOf(DiscoverList<Song>())
        private set
    var genreSongsId by mutableStateOf<String?>(null)
        private set

    init {
        loadChart()
        loadReleases()
        loadGenres()
    }

    fun loadChart() {
        chart = DiscoverList()
        viewModelScope.launch { chart = loadList(repository.chartSongs()) { it.toSongSafe() } }
    }

    fun loadReleases() {
        releases = DiscoverList()
        viewModelScope.launch { releases = loadList(repository.newReleases()) { it.toSongSafe() } }
    }

    fun loadGenres() {
        genres = DiscoverList()
        viewModelScope.launch {
            genres = loadList(repository.genres()) { document ->
                val name = (document.data["name"] as? String)?.trim().orEmpty()
                if (name.isBlank()) null else DiscoverGenre(document.id, name)
            }
        }
    }

    fun loadGenre(genreId: String) {
        genreLoad?.cancel()
        genreSongs = DiscoverList()
        genreSongsId = genreId
        genreLoad = viewModelScope.launch {
            genreSongs = loadList(repository.songsByGenre(genreId)) { it.toSongSafe() }
        }
    }

    private suspend fun <T : Any> loadList(
        request: Task<QuerySnapshot>,
        map: (QueryDocumentSnapshot) -> T?
    ): DiscoverList<T> =
        try {
            DiscoverList(items = request.await().mapNotNull(map), isLoading = false)
        } catch (_: FirebaseFirestoreException) {
            DiscoverList(isLoading = false, hasError = true)
        }
}
