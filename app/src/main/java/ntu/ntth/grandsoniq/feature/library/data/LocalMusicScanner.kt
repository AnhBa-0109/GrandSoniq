package ntu.ntth.grandsoniq.feature.library.data

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ntu.ntth.grandsoniq.core.model.LocalDeviceSong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMusicScanner @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun scanDeviceSongs(): List<LocalDeviceSong> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<LocalDeviceSong>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val duration = cursor.getLong(durationCol)
                    if (duration <= 0L) continue
                    songs += LocalDeviceSong(
                        localId = cursor.getLong(idCol).toString(),
                        title = cursor.getString(titleCol).orEmpty().ifBlank { "Không rõ tên" },
                        artistName = cursor.getString(artistCol).orEmpty()
                            .takeUnless { it == "<unknown>" } ?: "Nghệ sĩ không rõ",
                        filePath = cursor.getString(dataCol).orEmpty(),
                        durationMs = duration
                    )
                }
            }
        }
        songs
    }
}
