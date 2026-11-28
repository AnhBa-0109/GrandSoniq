package ntu.ntth.grandsoniq.feature.discover.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

internal class DiscoverRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun songs() = read("songs")
    fun albums() = read("albums")
    fun artists() = read("artists")
    fun playlists() = read("playlists")
    fun genres() = firestore.collection("genres").get(Source.SERVER)

    fun chartSongs() = firestore.collection("songs")
        .orderBy("playCount", Query.Direction.DESCENDING)
        .limit(24)
        .get(Source.SERVER)

    fun newReleases() = firestore.collection("songs")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(2)
        .get(Source.SERVER)

    fun songsByGenre(genreId: String) = firestore.collection("songs")
        .whereEqualTo("genreId", genreId)
        .limit(24)
        .get(Source.SERVER)

    private fun read(collection: String): Task<QuerySnapshot> =
        firestore.collection(collection).limit(24).get(Source.SERVER)
}
