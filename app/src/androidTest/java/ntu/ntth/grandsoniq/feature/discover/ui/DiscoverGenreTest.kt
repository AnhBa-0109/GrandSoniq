package ntu.ntth.grandsoniq.feature.discover.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverGenre
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DiscoverGenreTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun genreTileUsesTheProvidedNameAndPassesItsDocumentId() {
        val genre = DiscoverGenre(id = "genre_005", name = "Jazz Việt")
        var selectedId: String? = null
        compose.setContent {
            DiscoverGenreGrid(
                state = DiscoverList(items = listOf(genre), isLoading = false),
                onRetry = {},
                onSelect = { selectedId = it.id }
            )
        }

        compose.onNodeWithText("Nhạc Việt").assertDoesNotExist()
        compose.onNodeWithText(genre.name).performClick()
        compose.runOnIdle { assertEquals("genre_005", selectedId) }
    }
}
