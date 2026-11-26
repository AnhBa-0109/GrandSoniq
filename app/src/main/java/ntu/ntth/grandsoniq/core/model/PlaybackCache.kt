package ntu.ntth.grandsoniq.core.model

/**
 * Bộ đệm khôi phục phiên phát gần nhất cho Widget / Android Auto (Local table: `playback_cache`).
 */
data class PlaybackCache(
    val lastSongId: String = "",
    val lastPositionMs: Long = 0L,
    val queueJson: String = "[]"
)
