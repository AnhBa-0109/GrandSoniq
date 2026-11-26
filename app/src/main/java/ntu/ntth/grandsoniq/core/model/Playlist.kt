package ntu.ntth.grandsoniq.core.model

/**
 * Playlist nhạc cá nhân, gợi ý hệ thống hoặc cộng tác (Firestore collection: `playlists`).
 * Dùng `toPlaylistSafe()` trong `core.network.FirestoreMappers` khi đọc từ Firestore để tránh lỗi `Timestamp -> Long`.
 */
data class Playlist(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val coverUrl: String = "",
    val category: String = "user",
    val songIds: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val isCollaborative: Boolean = false,
    val collaboratorIds: List<String> = emptyList(),
    val createdAt: Long = 0L
)
