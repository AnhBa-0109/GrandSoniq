package ntu.ntth.grandsoniq.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Định dạng thời lượng (ms) sang mm:ss hoặc tổng phút cho header Playlist/Album.
 */
object DateTimeUtils {
    fun formatTrackDuration(durationMs: Long): String {
        val totalSeconds = max(0L, durationMs / 1000L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }

    fun formatTotalMinutes(totalDurationMs: Long): Long =
        max(0L, totalDurationMs / 60_000L)

    fun formatYear(epochMs: Long): String {
        if (epochMs <= 0L) return ""
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(epochMs))
    }

    fun formatFollowerCount(count: Long): String = when {
        count >= 1_000_000L -> String.format(Locale.US, "%.1f Tr", count / 1_000_000f)
        count >= 1_000L -> String.format(Locale.US, "%.1f N", count / 1_000f)
        else -> count.toString()
    }
}
