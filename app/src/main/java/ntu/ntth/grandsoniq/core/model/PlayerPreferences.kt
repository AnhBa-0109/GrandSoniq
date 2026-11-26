package ntu.ntth.grandsoniq.core.model

/**
 * Cài đặt trình phát nhạc cục bộ (Local table: `player_preferences`).
 */
data class PlayerPreferences(
    val eqPreset: String = "Flat",
    val eqBandsJson: String = "[]",
    val crossfadeSeconds: Int = 0,
    val sleepTimerEndMs: Long? = null,
    val repeatMode: String = "off",
    val isShuffle: Boolean = false
)
