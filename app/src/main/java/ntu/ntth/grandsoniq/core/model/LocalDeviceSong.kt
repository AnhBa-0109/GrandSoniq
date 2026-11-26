package ntu.ntth.grandsoniq.core.model

/**
 * Tệp nhạc quét từ bộ nhớ thiết bị (Local table: `local_device_songs`).
 */
data class LocalDeviceSong(
    val localId: String = "",
    val title: String = "",
    val artistName: String = "",
    val filePath: String = "",
    val durationMs: Long = 0L
)
