package ntu.ntth.grandsoniq.core.model

/**
 * Playlist nhạc cá nhân, gợi ý hệ thống hoặc cộng tác (Firestore collection: `playlists`).
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
