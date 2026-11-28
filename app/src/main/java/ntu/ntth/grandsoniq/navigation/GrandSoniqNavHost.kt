package ntu.ntth.grandsoniq.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomBar
import ntu.ntth.grandsoniq.core.ui.MainTab
import ntu.ntth.grandsoniq.core.ui.MiniPlayerBar
import ntu.ntth.grandsoniq.feature.discover.ui.AlbumDetailScreen
import ntu.ntth.grandsoniq.feature.discover.ui.ArtistDetailScreen
import ntu.ntth.grandsoniq.feature.discover.ui.DiscoverScreen
import ntu.ntth.grandsoniq.feature.discover.ui.HomeScreen
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverViewModel
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeViewModel
import ntu.ntth.grandsoniq.feature.library.ui.LibraryScreen
import ntu.ntth.grandsoniq.feature.library.ui.PlaylistDetailScreen
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController

private val AvailableTabs = setOf(MainTab.Home, MainTab.Discover, MainTab.Library)

@Composable
internal fun GrandSoniqNavHost(
    home: HomeViewModel,
    discover: () -> DiscoverViewModel,
    playbackController: PlaybackController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playbackSnapshot by playbackController.snapshot.collectAsStateWithLifecycle()

    val selectedTab = when {
        currentRoute == AppDestination.Home.route -> MainTab.Home
        currentRoute == AppDestination.Discover.route -> MainTab.Discover
        currentRoute == AppDestination.Friends.route -> MainTab.Friends
        currentRoute == AppDestination.Library.route ||
            currentRoute?.startsWith("playlist/") == true ||
            currentRoute?.startsWith("album/") == true ||
            currentRoute?.startsWith("artist/") == true -> MainTab.Library
        else -> MainTab.Home
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF101311)).safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppDestination.Home.route) {
                    HomeScreen(
                        state = home.state,
                        onRetry = home::reload,
                        currentSongId = playbackSnapshot.currentSong?.id,
                        isPlaying = playbackSnapshot.isPlaying,
                        onPlaySong = { song ->
                            playbackController.playSong(song, home.state.songs)
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate(AppDestination.AlbumDetail.createRoute(albumId))
                        },
                        onArtistClick = { artistId ->
                            navController.navigate(AppDestination.ArtistDetail.createRoute(artistId))
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate(AppDestination.PlaylistDetail.createRoute(playlistId))
                        }
                    )
                }

                composable(AppDestination.Discover.route) {
                    DiscoverScreen(discover())
                }

                composable(AppDestination.Library.route) {
                    LibraryScreen(
                        playbackSnapshot = playbackSnapshot,
                        onPlaylistClick = { playlistId ->
                            navController.navigate(AppDestination.PlaylistDetail.createRoute(playlistId))
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate(AppDestination.AlbumDetail.createRoute(albumId))
                        },
                        onArtistClick = { artistId ->
                            navController.navigate(AppDestination.ArtistDetail.createRoute(artistId))
                        }
                    )
                }

                composable(
                    route = AppDestination.PlaylistDetail.route,
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) {
                    PlaylistDetailScreen(
                        playbackSnapshot = playbackSnapshot,
                        onBack = { navController.popBackStack() },
                        onArtistClick = { artistId ->
                            navController.navigate(AppDestination.ArtistDetail.createRoute(artistId))
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate(AppDestination.AlbumDetail.createRoute(albumId))
                        }
                    )
                }

                composable(
                    route = AppDestination.AlbumDetail.route,
                    arguments = listOf(navArgument("albumId") { type = NavType.StringType })
                ) {
                    AlbumDetailScreen(
                        playbackSnapshot = playbackSnapshot,
                        onBack = { navController.popBackStack() },
                        onArtistClick = { artistId ->
                            navController.navigate(AppDestination.ArtistDetail.createRoute(artistId))
                        }
                    )
                }

                composable(
                    route = AppDestination.ArtistDetail.route,
                    arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                ) {
                    ArtistDetailScreen(
                        playbackSnapshot = playbackSnapshot,
                        onBack = { navController.popBackStack() },
                        onAlbumClick = { albumId ->
                            navController.navigate(AppDestination.AlbumDetail.createRoute(albumId))
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate(AppDestination.PlaylistDetail.createRoute(playlistId))
                        }
                    )
                }
            }
        }

        playbackSnapshot.currentSong?.let { activeSong ->
            MiniPlayerBar(
                song = activeSong,
                isPlaying = playbackSnapshot.isPlaying,
                progress = playbackSnapshot.progress,
                onTogglePlayPause = playbackController::togglePlayPause,
                onNext = playbackController::skipNext,
                onClick = playbackController::togglePlayPause
            )
        }

        GrandSoniqBottomBar(
            selectedTab = selectedTab,
            enabledTabs = AvailableTabs,
            onTabSelected = { tab ->
                val targetRoute = when (tab) {
                    MainTab.Home -> AppDestination.Home.route
                    MainTab.Discover -> AppDestination.Discover.route
                    MainTab.Library -> AppDestination.Library.route
                    MainTab.Friends -> return@GrandSoniqBottomBar
                }
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
