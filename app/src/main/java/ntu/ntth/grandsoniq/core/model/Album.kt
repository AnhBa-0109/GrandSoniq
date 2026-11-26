package ntu.ntth.grandsoniq.core.model

/**
 * Album âm nhạc (Firestore collection: `albums`).
 * Dùng `toAlbumSafe()` trong `core.network.FirestoreMappers` khi đọc từ Firestore để tránh lỗi `Timestamp -> Long`.
 */
data class Album(
    val id: String = "",
    val title: String = "",
    val titleLowercase: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val coverUrl: String = "",
    val songIds: List<String> = emptyList(),
    val releaseDate: Long = 0L
)
