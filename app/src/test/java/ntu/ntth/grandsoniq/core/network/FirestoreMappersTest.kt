package ntu.ntth.grandsoniq.core.network

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class FirestoreMappersTest {

    @Test
    fun `toEpochMillis handles Timestamp Date Long and null safely`() {
        val ts = Timestamp(1_700_000_000L, 500_000_000)
        assertEquals(1_700_000_000_500L, ts.toEpochMillis())

        val date = Date(1_700_000_123_000L)
        assertEquals(1_700_000_123_000L, date.toEpochMillis())

        assertEquals(123456L, 123456L.toEpochMillis())
        assertEquals(0L, null.toEpochMillis())
        assertNull(null.toEpochMillisOrNull())
    }

    @Test
    fun `mapToSong converts Firestore Timestamp and nested audioUrls without crashing`() {
        val ts = Timestamp(1_732_600_000L, 0)
        val raw = mapOf(
            "title" to "Bài Hát Mẫu",
            "artistName" to "Nghệ sĩ Mẫu",
            "audioUrl" to "https://example.com/song.mp3",
            "audioUrls" to mapOf(
                "normal" to "https://example.com/normal.mp3",
                "high" to "https://example.com/high.mp3",
                "lossless" to ""
            ),
            "duration" to 210_000L,
            "createdAt" to ts
        )

        val song = mapToSong(docId = "song_001", data = raw)

        assertEquals("song_001", song.id)
        assertEquals("Bài Hát Mẫu", song.title)
        assertEquals("https://example.com/high.mp3", song.audioUrls.high)
        assertEquals(210_000L, song.duration)
        assertEquals(1_732_600_000_000L, song.createdAt)
    }

    @Test
    fun `mapToUser converts Timestamp and nested settings with safe defaults`() {
        val created = Timestamp(1_732_600_000L, 250_000_000)
        val raw = mapOf(
            "displayName" to "Nguyễn Văn A",
            "email" to "demo@grandsoniq.app",
            "favoriteSongIds" to listOf("song_001"),
            "premiumUntil" to null,
            "settings" to mapOf(
                "audioQuality" to "lossless",
                "wifiOnlyDownload" to false
            ),
            "createdAt" to created
        )

        val user = mapToUser(docId = "user_demo_01", data = raw)

        assertEquals("user_demo_01", user.uid)
        assertNull(user.premiumUntil)
        assertEquals("lossless", user.settings.audioQuality)
        assertEquals(false, user.settings.wifiOnlyDownload)
        assertEquals(false, user.settings.dataSaver)
        assertEquals(1_732_600_000_250L, user.createdAt)
    }
}
