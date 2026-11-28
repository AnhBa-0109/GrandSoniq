package ntu.ntth.grandsoniq.feature.discover.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

internal class DiscoverRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun songs() = read("songs")
    fun albums() = read("albums")
    fun artists() = read("artists")
    fun playlists() = read("playlists")

    private fun read(collection: String): Task<QuerySnapshot> =
        firestore.collection(collection).limit(24).get(Source.SERVER)
}
