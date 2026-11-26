package ntu.ntth.grandsoniq.core.model

/**
 * Bài hát (Firestore collection: `songs`).
 * Lưu ý: Dùng `toSongSafe()` trong `core.network.FirestoreMappers` khi đọc từ Firestore để chuyển
 * `Timestamp` sang `Long` an toàn, tránh crash khi gọi `toObject()`.
 */
data class Song(
    val id: String = "",
    val title: String = "",
    val titleLowercase: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val albumId: String = "",
    val genre: String = "",
    val mood: String = "",
    val audioUrl: String = "",
    val audioUrls: AudioUrls = AudioUrls(),
    val coverUrl: String = "",
    val mvUrl: String = "",
    val shortVideoUrl: String = "",
    val duration: Long = 0L,
    val playCount: Long = 0L,
    val lyrics: String = "",
    val isPremium: Boolean = false,
    val createdAt: Long = 0L
)

/**
 * Đường dẫn stream âm thanh theo từng mức chất lượng (`songs.audioUrls`).
 */
data class AudioUrls(
    val normal: String = "",
    val high: String = "",
    val lossless: String = ""
)
