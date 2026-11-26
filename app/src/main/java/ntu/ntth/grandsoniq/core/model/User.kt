package ntu.ntth.grandsoniq.core.model

/**
 * Tài khoản người dùng (Firestore collection: `users`, Document ID = `uid`).
 * Các trường `Timestamp` lưu dưới dạng epoch milliseconds (`Long`).
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val favoriteSongIds: List<String> = emptyList(),
    val recentSongIds: List<String> = emptyList(),
    val followingArtistIds: List<String> = emptyList(),
    val followingUserIds: List<String> = emptyList(),
    val savedAlbumIds: List<String> = emptyList(),
    val isPremium: Boolean = false,
    val premiumUntil: Long? = null,
    val settings: UserSettings = UserSettings(),
    val createdAt: Long = 0L
)

/**
 * Cấu hình đồng bộ của người dùng (`users.settings`).
 */
data class UserSettings(
    val audioQuality: String = "high",
    val wifiOnlyDownload: Boolean = true,
    val dataSaver: Boolean = false
)
