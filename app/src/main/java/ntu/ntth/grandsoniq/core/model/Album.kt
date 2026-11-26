package ntu.ntth.grandsoniq.core.model

/**
 * Album âm nhạc (Firestore collection: `albums`).
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
