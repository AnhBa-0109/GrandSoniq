package ntu.ntth.grandsoniq.navigation

import android.net.Uri

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object Discover : AppDestination("discover")
    data object Library : AppDestination("library")
    data object Friends : AppDestination("friends")

    data object PlaylistDetail : AppDestination("playlist/{playlistId}") {
        fun createRoute(playlistId: String): String = "playlist/${Uri.encode(playlistId)}"
    }

    data object AlbumDetail : AppDestination("album/{albumId}") {
        fun createRoute(albumId: String): String = "album/${Uri.encode(albumId)}"
    }

    data object ArtistDetail : AppDestination("artist/{artistId}") {
        fun createRoute(artistId: String): String = "artist/${Uri.encode(artistId)}"
    }
}
