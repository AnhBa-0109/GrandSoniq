package ntu.ntth.grandsoniq.core.model

/**
 * Phòng nghe nhạc chung đồng bộ thời gian thực (Firestore collection: `listeningRooms`).
 * Dùng `toListeningRoomSafe()` trong `core.network.FirestoreMappers` khi đọc từ Firestore để tránh lỗi `Timestamp -> Long`.
 */
data class ListeningRoom(
    val id: String = "",
    val hostId: String = "",
    val roomName: String = "",
    val isActive: Boolean = true,
    val memberIds: List<String> = emptyList(),
    val currentSongId: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val queueSongIds: List<String> = emptyList(),
    val updatedAt: Long = 0L
)
