package ntu.ntth.grandsoniq.core.model

/**
 * Bình luận trên bài hát hoặc playlist (Firestore collection: `comments`).
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
