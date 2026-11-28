package ntu.ntth.grandsoniq.feature.player.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ntu.ntth.grandsoniq.core.model.Song
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackUiSnapshot(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val progress: Float = 0.25f
)

@Singleton
class PlaybackController @Inject constructor() {
    private val _snapshot = MutableStateFlow(PlaybackUiSnapshot())
    val snapshot: StateFlow<PlaybackUiSnapshot> = _snapshot.asStateFlow()

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val effectiveQueue = if (queue.any { it.id == song.id }) queue else listOf(song) + queue
        val index = effectiveQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _snapshot.update {
            it.copy(
                currentSong = song,
                queue = effectiveQueue,
                currentIndex = index,
                isPlaying = true,
                progress = 0.15f
            )
        }
    }

    fun playList(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val playQueue = if (shuffle) songs.shuffled() else songs
        _snapshot.update {
            it.copy(
                currentSong = playQueue.first(),
                queue = playQueue,
                currentIndex = 0,
                isPlaying = true,
                isShuffle = shuffle,
                progress = 0.1f
            )
        }
    }

    fun togglePlayPause() {
        _snapshot.update { current ->
            if (current.currentSong == null) current
            else current.copy(isPlaying = !current.isPlaying)
        }
    }

    fun toggleShuffle() {
        _snapshot.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun skipNext() {
        _snapshot.update { current ->
            val q = current.queue
            if (q.isEmpty()) return@update current
            val nextIdx = if (current.isShuffle && q.size > 1) {
                q.indices.filter { it != current.currentIndex }.random()
            } else {
                (current.currentIndex + 1) % q.size
            }
            current.copy(
                currentSong = q[nextIdx],
                currentIndex = nextIdx,
                isPlaying = true,
                progress = 0.08f
            )
        }
    }

    fun skipPrevious() {
        _snapshot.update { current ->
            val q = current.queue
            if (q.isEmpty()) return@update current
            val prevIdx = if (current.currentIndex - 1 < 0) q.lastIndex else current.currentIndex - 1
            current.copy(
                currentSong = q[prevIdx],
                currentIndex = prevIdx,
                isPlaying = true,
                progress = 0.08f
            )
        }
    }
}
