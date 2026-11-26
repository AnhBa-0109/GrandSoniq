package ntu.ntth.grandsoniq.core.network

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.AudioUrls
import ntu.ntth.grandsoniq.core.model.Comment
import ntu.ntth.grandsoniq.core.model.ListeningRoom
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.model.Subscription
import ntu.ntth.grandsoniq.core.model.User
import ntu.ntth.grandsoniq.core.model.UserSettings
import java.util.Date

/**
 * Chuyển đổi an toàn giá trị thời gian từ Firestore (`Timestamp`, `Date`, hoặc `Number` epoch ms)
 * sang epoch milliseconds (`Long`), tránh crash `CustomClassMapper` khi gọi `toObject()`.
 */
internal fun Any?.toEpochMillis(default: Long = 0L): Long =
    toEpochMillisOrNull() ?: default

internal fun Any?.toEpochMillisOrNull(): Long? = when (this) {
    is Timestamp -> seconds * 1000L + nanoseconds / 1_000_000L
    is Date -> time
    is Number -> toLong()
    else -> null
}

private fun Map<String, Any?>.str(key: String, default: String = ""): String =
    (this[key] as? String) ?: default

private fun Map<String, Any?>.numLong(key: String, default: Long = 0L): Long =
    (this[key] as? Number)?.toLong() ?: default

private fun Map<String, Any?>.bool(key: String, default: Boolean = false): Boolean =
    (this[key] as? Boolean) ?: default

private fun Map<String, Any?>.strList(key: String): List<String> =
    (this[key] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.subMap(key: String): Map<String, Any?> =
    (this[key] as? Map<String, Any?>) ?: emptyMap()

private inline fun <T> DocumentSnapshot.mapSafely(
    mapper: (docId: String, data: Map<String, Any?>) -> T
): T? {
    if (!exists()) return null
    val rawData = data ?: return null
    return mapper(id, rawData)
}

fun DocumentSnapshot.toUserSafe(): User? = mapSafely(::mapToUser)
fun DocumentSnapshot.toSongSafe(): Song? = mapSafely(::mapToSong)
fun DocumentSnapshot.toPlaylistSafe(): Playlist? = mapSafely(::mapToPlaylist)
fun DocumentSnapshot.toArtistSafe(): Artist? = mapSafely(::mapToArtist)
fun DocumentSnapshot.toAlbumSafe(): Album? = mapSafely(::mapToAlbum)
fun DocumentSnapshot.toCommentSafe(): Comment? = mapSafely(::mapToComment)
fun DocumentSnapshot.toListeningRoomSafe(): ListeningRoom? = mapSafely(::mapToListeningRoom)
fun DocumentSnapshot.toSubscriptionSafe(): Subscription? = mapSafely(::mapToSubscription)

internal fun mapToUser(docId: String, data: Map<String, Any?>): User {
    val rawSettings = data.subMap("settings")
    return User(
        uid = data.str("uid", docId),
        displayName = data.str("displayName"),
        email = data.str("email"),
        avatarUrl = data.str("avatarUrl"),
        favoriteSongIds = data.strList("favoriteSongIds"),
        recentSongIds = data.strList("recentSongIds"),
        followingArtistIds = data.strList("followingArtistIds"),
        followingUserIds = data.strList("followingUserIds"),
        savedAlbumIds = data.strList("savedAlbumIds"),
        isPremium = data.bool("isPremium"),
        premiumUntil = data["premiumUntil"].toEpochMillisOrNull(),
        settings = UserSettings(
            audioQuality = rawSettings.str("audioQuality", "high"),
            wifiOnlyDownload = rawSettings.bool("wifiOnlyDownload", true),
            dataSaver = rawSettings.bool("dataSaver", false)
        ),
        createdAt = data["createdAt"].toEpochMillis()
    )
}

internal fun mapToSong(docId: String, data: Map<String, Any?>): Song {
    val rawUrls = data.subMap("audioUrls")
    return Song(
        id = data.str("id", docId),
        title = data.str("title"),
        titleLowercase = data.str("titleLowercase"),
        artistId = data.str("artistId"),
        artistName = data.str("artistName"),
        albumId = data.str("albumId"),
        genre = data.str("genre"),
        mood = data.str("mood"),
        audioUrl = data.str("audioUrl"),
        audioUrls = AudioUrls(
            normal = rawUrls.str("normal"),
            high = rawUrls.str("high"),
            lossless = rawUrls.str("lossless")
        ),
        coverUrl = data.str("coverUrl"),
        mvUrl = data.str("mvUrl"),
        shortVideoUrl = data.str("shortVideoUrl"),
        duration = data.numLong("duration"),
        playCount = data.numLong("playCount"),
        lyrics = data.str("lyrics"),
        isPremium = data.bool("isPremium"),
        createdAt = data["createdAt"].toEpochMillis()
    )
}

internal fun mapToPlaylist(docId: String, data: Map<String, Any?>): Playlist = Playlist(
    id = data.str("id", docId),
    name = data.str("name"),
    description = data.str("description"),
    ownerId = data.str("ownerId"),
    ownerName = data.str("ownerName"),
    coverUrl = data.str("coverUrl"),
    category = data.str("category", "user"),
    songIds = data.strList("songIds"),
    isPublic = data.bool("isPublic", true),
    isCollaborative = data.bool("isCollaborative", false),
    collaboratorIds = data.strList("collaboratorIds"),
    createdAt = data["createdAt"].toEpochMillis()
)

internal fun mapToArtist(docId: String, data: Map<String, Any?>): Artist = Artist(
    id = data.str("id", docId),
    name = data.str("name"),
    nameLowercase = data.str("nameLowercase"),
    avatarUrl = data.str("avatarUrl"),
    coverUrl = data.str("coverUrl"),
    bio = data.str("bio"),
    followerCount = data.numLong("followerCount"),
    isVerified = data.bool("isVerified")
)

internal fun mapToAlbum(docId: String, data: Map<String, Any?>): Album = Album(
    id = data.str("id", docId),
    title = data.str("title"),
    titleLowercase = data.str("titleLowercase"),
    artistId = data.str("artistId"),
    artistName = data.str("artistName"),
    coverUrl = data.str("coverUrl"),
    songIds = data.strList("songIds"),
    releaseDate = data["releaseDate"].toEpochMillis()
)

internal fun mapToComment(docId: String, data: Map<String, Any?>): Comment = Comment(
    id = data.str("id", docId),
    targetId = data.str("targetId"),
    targetType = data.str("targetType", "song"),
    userId = data.str("userId"),
    userName = data.str("userName"),
    userAvatar = data.str("userAvatar"),
    content = data.str("content"),
    songPositionMs = (data["songPositionMs"] as? Number)?.toLong(),
    createdAt = data["createdAt"].toEpochMillis()
)

internal fun mapToListeningRoom(docId: String, data: Map<String, Any?>): ListeningRoom = ListeningRoom(
    id = data.str("id", docId),
    hostId = data.str("hostId"),
    roomName = data.str("roomName"),
    isActive = data.bool("isActive", true),
    memberIds = data.strList("memberIds"),
    currentSongId = data.str("currentSongId"),
    isPlaying = data.bool("isPlaying"),
    positionMs = data.numLong("positionMs"),
    queueSongIds = data.strList("queueSongIds"),
    updatedAt = data["updatedAt"].toEpochMillis()
)

internal fun mapToSubscription(docId: String, data: Map<String, Any?>): Subscription = Subscription(
    id = data.str("id", docId),
    userId = data.str("userId"),
    planType = data.str("planType", "monthly"),
    amount = data.numLong("amount"),
    currency = data.str("currency", "VND"),
    provider = data.str("provider", "google_play"),
    status = data.str("status", "active"),
    startDate = data["startDate"].toEpochMillis(),
    endDate = data["endDate"].toEpochMillis()
)
