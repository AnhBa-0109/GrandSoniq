package ntu.ntth.grandsoniq.feature.discover.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test

class HomePlaybackTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun currentSongStaysMarkedWhenPausedAndClearsWhenStopped() {
        val first = Song(id = "first", title = "Bài đầu")
        val second = Song(id = "second", title = "Bài sau")
        val currentSongId = mutableStateOf<String?>(null)
        val isPlaying = mutableStateOf(false)
        val playing = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Đang phát")
        val paused = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Tạm dừng")

        compose.setContent {
            HomeScreen(
                state = HomeUiState(isLoading = false, songs = listOf(first, second)),
                onRetry = {},
                currentSongId = currentSongId.value,
                isPlaying = isPlaying.value,
                onPlaySong = { song ->
                    currentSongId.value = song.id
                    isPlaying.value = true
                }
            )
        }

        compose.onAllNodes(playing or paused).assertCountEquals(0)
        compose.onNodeWithText(first.title).performClick()
        compose.onNode(playing and hasText(first.title)).assertExists()

        compose.runOnIdle { isPlaying.value = false }
        compose.onAllNodes(playing).assertCountEquals(0)
        compose.onNode(paused and hasText(first.title)).assertExists()

        compose.onNodeWithText(second.title).performClick()
        compose.onAllNodes(paused).assertCountEquals(0)
        compose.onNode(playing and hasText(second.title)).assertExists()

        compose.runOnIdle {
            currentSongId.value = null
            isPlaying.value = false
        }
        compose.onAllNodes(playing or paused).assertCountEquals(0)
    }
}
