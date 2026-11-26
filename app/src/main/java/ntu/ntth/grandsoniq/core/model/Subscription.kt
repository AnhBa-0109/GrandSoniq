package ntu.ntth.grandsoniq.core.model

/**
 * Giao dịch và gói đăng ký Premium (Firestore collection: `subscriptions`).
 */
data class Subscription(
    val id: String = "",
    val userId: String = "",
    val planType: String = "monthly",
    val amount: Long = 0L,
    val currency: String = "VND",
    val provider: String = "google_play",
    val status: String = "active",
    val startDate: Long = 0L,
    val endDate: Long = 0L
)
