package ntu.ntth.grandsoniq.feature.library.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import ntu.ntth.grandsoniq.core.model.Album
import ntu.ntth.grandsoniq.core.model.Artist
import ntu.ntth.grandsoniq.core.model.Playlist
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.model.User
import ntu.ntth.grandsoniq.core.network.toAlbumSafe
import ntu.ntth.grandsoniq.core.network.toArtistSafe
import ntu.ntth.grandsoniq.core.network.toPlaylistSafe
import ntu.ntth.grandsoniq.core.network.toSongSafe
import ntu.ntth.grandsoniq.core.network.toUserSafe
import javax.inject.Inject
import javax.inject.Singleton

data class LibraryOverviewData(
    val user: User,
    val playlists: List<Playlist>,
    val favoriteSongs: List<Song>,
    val recentSongs: List<Song>,
    val savedAlbums: List<Album>,
    val followingArtists: List<Artist>,
    val savedPlaylistIds: Set<String>
)

data class PlaylistDetailData(
    val playlist: Playlist,
    val songs: List<Song>,
    val isSaved: Boolean,
    val favoriteSongIds: Set<String>,
    val availableSongs: List<Song>
)

data class AlbumDetailData(
    val album: Album,
    val artist: Artist?,
    val songs: List<Song>,
    val isSaved: Boolean,
    val favoriteSongIds: Set<String>
)

data class ArtistProfileData(
    val artist: Artist,
    val topSongs: List<Song>,
    val albums: List<Album>,
    val relatedPlaylists: List<Playlist>,
    val isFollowing: Boolean,
    val favoriteSongIds: Set<String>
)

@Singleton
class LibraryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun currentUid(): String = auth.currentUser?.uid ?: DEMO_UID

    suspend fun loadLibraryOverview(): LibraryOverviewData = coroutineScope {
        val uid = currentUid()
        val userDocDeferred = async {
            firestore.collection("users").document(uid).get().await()
        }
        val allPlaylistsDeferred = async {
            firestore.collection("playlists").limit(50).get().await()
        }

        val userDoc = userDocDeferred.await()
        val user = userDoc.toUserSafe() ?: User(uid = uid, displayName = "Người dùng")
        @Suppress("UNCHECKED_CAST")
        val savedPlaylistIds = ((userDoc.get("savedPlaylistIds") as? List<*>)
            ?.mapNotNull { it as? String } ?: emptyList()).toSet()

        val allPlaylists = allPlaylistsDeferred.await().documents.mapNotNull { it.toPlaylistSafe() }
        val userPlaylists = allPlaylists.filter { playlist ->
            playlist.ownerId == uid ||
                playlist.collaboratorIds.contains(uid) ||
                savedPlaylistIds.contains(playlist.id) ||
                playlist.isPublic
        }.sortedWith(
            compareByDescending<Playlist> { it.ownerId == uid || it.collaboratorIds.contains(uid) }
                .thenByDescending { it.createdAt }
        )

        val favDeferred = async { fetchSongsByIds(user.favoriteSongIds) }
        val recentDeferred = async { fetchSongsByIds(user.recentSongIds) }
        val albumsDeferred = async { fetchAlbumsByIds(user.savedAlbumIds) }
        val artistsDeferred = async { fetchArtistsByIds(user.followingArtistIds) }

        LibraryOverviewData(
            user = user,
            playlists = userPlaylists,
            favoriteSongs = favDeferred.await(),
            recentSongs = recentDeferred.await(),
            savedAlbums = albumsDeferred.await(),
            followingArtists = artistsDeferred.await(),
            savedPlaylistIds = savedPlaylistIds
        )
    }

    suspend fun loadPlaylistDetail(playlistId: String): PlaylistDetailData = coroutineScope {
        val uid = currentUid()
        val playlistDeferred = async {
            firestore.collection("playlists").document(playlistId).get().await().toPlaylistSafe()
        }
        val userDeferred = async {
            firestore.collection("users").document(uid).get().await()
        }
        val catalogDeferred = async {
            firestore.collection("songs").limit(50).get().await().documents.mapNotNull { it.toSongSafe() }
        }

        val playlist = playlistDeferred.await()
            ?: throw IllegalStateException("Không tìm thấy playlist")
        val userDoc = userDeferred.await()
        val user = userDoc.toUserSafe() ?: User(uid = uid)
        @Suppress("UNCHECKED_CAST")
        val savedPlaylistIds = ((userDoc.get("savedPlaylistIds") as? List<*>)
            ?.mapNotNull { it as? String } ?: emptyList()).toSet()

        val songs = if (playlist.songIds.isNotEmpty()) {
            fetchSongsByIds(playlist.songIds)
        } else {
            emptyList()
        }

        val isSaved = playlist.ownerId == uid ||
            playlist.collaboratorIds.contains(uid) ||
            savedPlaylistIds.contains(playlist.id)

        PlaylistDetailData(
            playlist = playlist,
            songs = songs,
            isSaved = isSaved,
            favoriteSongIds = user.favoriteSongIds.toSet(),
            availableSongs = catalogDeferred.await()
        )
    }

    suspend fun loadAlbumDetail(albumId: String): AlbumDetailData = coroutineScope {
        val uid = currentUid()
        val albumDeferred = async {
            firestore.collection("albums").document(albumId).get().await().toAlbumSafe()
        }
        val userDeferred = async {
            firestore.collection("users").document(uid).get().await().toUserSafe() ?: User(uid = uid)
        }

        val album = albumDeferred.await()
            ?: throw IllegalStateException("Không tìm thấy album")
        val user = userDeferred.await()

        val artistDeferred = async {
            if (album.artistId.isNotBlank()) {
                firestore.collection("artists").document(album.artistId).get().await().toArtistSafe()
            } else {
                null
            }
        }
        val songsDeferred = async {
            if (album.songIds.isNotEmpty()) {
                fetchSongsByIds(album.songIds)
            } else {
                firestore.collection("songs")
                    .whereEqualTo("albumId", albumId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toSongSafe() }
            }
        }

        AlbumDetailData(
            album = album,
            artist = artistDeferred.await(),
            songs = songsDeferred.await(),
            isSaved = user.savedAlbumIds.contains(albumId),
            favoriteSongIds = user.favoriteSongIds.toSet()
        )
    }

    suspend fun loadArtistProfile(artistId: String): ArtistProfileData = coroutineScope {
        val uid = currentUid()
        val artistDeferred = async {
            firestore.collection("artists").document(artistId).get().await().toArtistSafe()
        }
        val userDeferred = async {
            firestore.collection("users").document(uid).get().await().toUserSafe() ?: User(uid = uid)
        }
        val songsDeferred = async {
            firestore.collection("songs")
                .whereEqualTo("artistId", artistId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toSongSafe() }
                .sortedByDescending { it.playCount }
        }
        val albumsDeferred = async {
            firestore.collection("albums")
                .whereEqualTo("artistId", artistId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toAlbumSafe() }
                .sortedByDescending { it.releaseDate }
        }
        val playlistsDeferred = async {
            firestore.collection("playlists")
                .whereEqualTo("isPublic", true)
                .limit(30)
                .get()
                .await()
                .documents
                .mapNotNull { it.toPlaylistSafe() }
        }

        val artist = artistDeferred.await()
            ?: throw IllegalStateException("Không tìm thấy nghệ sĩ")
        val user = userDeferred.await()
        val songs = songsDeferred.await()
        val artistSongIds = songs.map { it.id }.toSet()
        val relatedPlaylists = playlistsDeferred.await().filter { pl ->
            pl.ownerId == artistId || pl.songIds.any { it in artistSongIds }
        }

        ArtistProfileData(
            artist = artist,
            topSongs = songs,
            albums = albumsDeferred.await(),
            relatedPlaylists = relatedPlaylists,
            isFollowing = user.followingArtistIds.contains(artistId),
            favoriteSongIds = user.favoriteSongIds.toSet()
        )
    }

    suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        isCollaborative: Boolean
    ): Playlist {
        val uid = currentUid()
        val user = firestore.collection("users").document(uid).get().await().toUserSafe()
        val ownerName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Bạn"
        val docRef = firestore.collection("playlists").document()
        val now = Timestamp.now()

        val payload = hashMapOf(
            "id" to docRef.id,
            "name" to name.trim(),
            "description" to description.trim(),
            "ownerId" to uid,
            "ownerName" to ownerName,
            "coverUrl" to "",
            "category" to "user",
            "songIds" to emptyList<String>(),
            "isPublic" to isPublic,
            "isCollaborative" to isCollaborative,
            "collaboratorIds" to emptyList<String>(),
            "createdAt" to now
        )
        docRef.set(payload).await()
        return Playlist(
            id = docRef.id,
            name = name.trim(),
            description = description.trim(),
            ownerId = uid,
            ownerName = ownerName,
            coverUrl = "",
            category = "user",
            songIds = emptyList(),
            isPublic = isPublic,
            isCollaborative = isCollaborative,
            collaboratorIds = emptyList(),
            createdAt = now.toDate().time
        )
    }

    suspend fun updatePlaylist(
        playlistId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        isCollaborative: Boolean
    ) {
        firestore.collection("playlists").document(playlistId).update(
            mapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "isPublic" to isPublic,
                "isCollaborative" to isCollaborative
            )
        ).await()
    }

    suspend fun deletePlaylist(playlistId: String) {
        firestore.collection("playlists").document(playlistId).delete().await()
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        val docRef = firestore.collection("playlists").document(playlistId)
        val existing = docRef.get().await().toPlaylistSafe()
        val updates = mutableMapOf<String, Any>(
            "songIds" to FieldValue.arrayUnion(song.id)
        )
        if (existing != null && existing.coverUrl.isBlank() && song.coverUrl.isNotBlank()) {
            updates["coverUrl"] = song.coverUrl
        }
        docRef.update(updates).await()
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        firestore.collection("playlists").document(playlistId)
            .update("songIds", FieldValue.arrayRemove(songId))
            .await()
    }

    suspend fun inviteCollaborator(playlistId: String, emailOrUid: String): String {
        val trimmed = emailOrUid.trim()
        require(trimmed.isNotEmpty()) { "Vui lòng nhập email hoặc UID bạn bè" }

        val targetUser = if (trimmed.contains("@")) {
            firestore.collection("users")
                .whereEqualTo("email", trimmed)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toUserSafe()
                ?: throw IllegalArgumentException("Không tìm thấy tài khoản với email $trimmed")
        } else {
            firestore.collection("users")
                .document(trimmed)
                .get()
                .await()
                .toUserSafe()
                ?: throw IllegalArgumentException("Không tìm thấy người dùng có UID $trimmed")
        }

        firestore.collection("playlists").document(playlistId).update(
            mapOf(
                "isCollaborative" to true,
                "collaboratorIds" to FieldValue.arrayUnion(targetUser.uid)
            )
        ).await()

        return targetUser.displayName.ifBlank { targetUser.email.ifBlank { targetUser.uid } }
    }

    suspend fun toggleSavePlaylist(playlistId: String, currentlySaved: Boolean): Boolean {
        val uid = currentUid()
        val userRef = firestore.collection("users").document(uid)
        val op = if (currentlySaved) {
            FieldValue.arrayRemove(playlistId)
        } else {
            FieldValue.arrayUnion(playlistId)
        }
        userRef.set(mapOf("savedPlaylistIds" to op), SetOptions.merge()).await()
        return !currentlySaved
    }

    suspend fun toggleSaveAlbum(albumId: String, currentlySaved: Boolean): Boolean {
        val uid = currentUid()
        val userRef = firestore.collection("users").document(uid)
        val op = if (currentlySaved) {
            FieldValue.arrayRemove(albumId)
        } else {
            FieldValue.arrayUnion(albumId)
        }
        userRef.set(mapOf("savedAlbumIds" to op), SetOptions.merge()).await()
        return !currentlySaved
    }

    suspend fun toggleFavoriteSong(songId: String, currentlyFavorite: Boolean): Boolean {
        val uid = currentUid()
        val userRef = firestore.collection("users").document(uid)
        val op = if (currentlyFavorite) {
            FieldValue.arrayRemove(songId)
        } else {
            FieldValue.arrayUnion(songId)
        }
        userRef.set(mapOf("favoriteSongIds" to op), SetOptions.merge()).await()
        return !currentlyFavorite
    }

    suspend fun toggleFollowArtist(artistId: String, currentlyFollowing: Boolean): Boolean {
        val uid = currentUid()
        val userRef = firestore.collection("users").document(uid)
        val artistRef = firestore.collection("artists").document(artistId)
        val batch = firestore.batch()
        if (currentlyFollowing) {
            batch.set(userRef, mapOf("followingArtistIds" to FieldValue.arrayRemove(artistId)), SetOptions.merge())
            batch.update(artistRef, "followerCount", FieldValue.increment(-1L))
        } else {
            batch.set(userRef, mapOf("followingArtistIds" to FieldValue.arrayUnion(artistId)), SetOptions.merge())
            batch.update(artistRef, "followerCount", FieldValue.increment(1L))
        }
        batch.commit().await()
        return !currentlyFollowing
    }

    suspend fun recordSongPlayed(songId: String) {
        if (songId.isBlank() || songId.startsWith("local_")) return
        val uid = currentUid()
        runCatching {
            firestore.collection("songs").document(songId)
                .update("playCount", FieldValue.increment(1L))
                .await()
        }
        runCatching {
            val userRef = firestore.collection("users").document(uid)
            val currentRecent = userRef.get().await().toUserSafe()?.recentSongIds ?: emptyList()
            val updated = (listOf(songId) + currentRecent.filterNot { it == songId }).take(25)
            userRef.set(mapOf("recentSongIds" to updated), SetOptions.merge()).await()
        }
    }

    private suspend fun fetchSongsByIds(ids: List<String>): List<Song> {
        if (ids.isEmpty()) return emptyList()
        val map = fetchByDocumentIds("songs", ids) { it.toSongSafe() }
        return ids.mapNotNull { map[it] }
    }

    private suspend fun fetchAlbumsByIds(ids: List<String>): List<Album> {
        if (ids.isEmpty()) return emptyList()
        val map = fetchByDocumentIds("albums", ids) { it.toAlbumSafe() }
        return ids.mapNotNull { map[it] }
    }

    private suspend fun fetchArtistsByIds(ids: List<String>): List<Artist> {
        if (ids.isEmpty()) return emptyList()
        val map = fetchByDocumentIds("artists", ids) { it.toArtistSafe() }
        return ids.mapNotNull { map[it] }
    }

    private suspend fun <T> fetchByDocumentIds(
        collection: String,
        ids: List<String>,
        mapper: (DocumentSnapshot) -> T?
    ): Map<String, T> = coroutineScope {
        val distinct = ids.distinct()
        val chunks = distinct.chunked(10)
        val docs = chunks.map { chunk ->
            async {
                firestore.collection(collection)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
            }
        }.awaitAll().flatten()

        buildMap {
            for (doc in docs) {
                mapper(doc)?.let { put(doc.id, it) }
            }
        }
    }

    companion object {
        private const val DEMO_UID = "user_demo_01"
    }
}
