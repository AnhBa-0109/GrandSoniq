package ntu.ntth.grandsoniq.navigation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ntu.ntth.grandsoniq.MainActivity
import org.junit.Rule
import org.junit.Test

class GrandSoniqNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun sharedBarKeepsSelectionInSyncWithHomeAndDiscover() {
        val home = compose.onNode(hasText("Trang chủ") and isSelectable())
        val discover = compose.onNode(hasText("Khám phá") and isSelectable())

        home.assertIsSelected()
        discover.assertIsNotSelected().performClick()
        discover.assertIsSelected()
        home.assertIsNotSelected()
        compose.onNodeWithText("Bài hát, nghệ sĩ, album…").assertExists()
        compose.onNodeWithContentDescription("Thông báo").assertExists()
        compose.onNode(hasText("Thư viện") and isSelectable()).assertIsNotEnabled()

        home.performClick()
        home.assertIsSelected()
        discover.assertIsNotSelected()
        compose.onNodeWithText("Nhạc cho hôm nay").assertExists()
        compose.onNodeWithText("Chào bạn").assertDoesNotExist()
        compose.onNodeWithContentDescription("Thông báo").assertExists()
    }
}
