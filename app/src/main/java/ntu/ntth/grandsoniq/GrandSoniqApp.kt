package ntu.ntth.grandsoniq

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Lớp Application khởi tạo toàn cục (DI, notification channels, cấu hình ứng dụng).
 */
@HiltAndroidApp
class GrandSoniqApp : Application()
