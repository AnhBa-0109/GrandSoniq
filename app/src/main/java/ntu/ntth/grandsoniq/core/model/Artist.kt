package ntu.ntth.grandsoniq.core.model

/**
 * Nghệ sĩ (Firestore collection: `artists`).
 */
data class Artist(
    val id: String = "",
    val name: String = "",
    val nameLowercase: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val bio: String = "",
    val followerCount: Long = 0L,
    val isVerified: Boolean = false
)
