package ntu.ntth.grandsoniq.core.model

/**
 * Bình luận trên bài hát hoặc playlist (Firestore collection: `comments`).
 * Dùng `toCommentSafe()` trong `core.network.FirestoreMappers` khi đọc từ Firestore để tránh lỗi `Timestamp -> Long`.
 */
data class Comment(
    val id: String = "",
    val targetId: String = "",
    val targetType: String = "song",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val content: String = "",
    val songPositionMs: Long? = null,
    val createdAt: Long = 0L
)
