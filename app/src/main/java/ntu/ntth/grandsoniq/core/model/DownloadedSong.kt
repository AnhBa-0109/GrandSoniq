package ntu.ntth.grandsoniq.core.model

/**
 * Bài hát đã tải về để nghe offline (Local table: `downloaded_songs`).
 */
data class DownloadedSong(
    val songId: String = "",
    val title: String = "",
    val artistName: String = "",
    val localAudioPath: String = "",
    val localCoverPath: String = "",
    val fileSizeBytes: Long = 0L,
    val downloadedAt: Long = 0L
)
