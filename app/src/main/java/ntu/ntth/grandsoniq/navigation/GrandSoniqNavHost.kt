package ntu.ntth.grandsoniq.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ntu.ntth.grandsoniq.core.ui.GrandSoniqBottomNavBar
import ntu.ntth.grandsoniq.core.ui.MainNavTab
import ntu.ntth.grandsoniq.core.ui.MiniPlayerBar
import ntu.ntth.grandsoniq.feature.discover.ui.AlbumDetailScreen
import ntu.ntth.grandsoniq.feature.discover.ui.ArtistDetailScreen
import ntu.ntth.grandsoniq.feature.discover.ui.HomeScreen
import ntu.ntth.grandsoniq.feature.discover.viewmodel.HomeViewModel
import ntu.ntth.grandsoniq.feature.library.ui.LibraryScreen
import ntu.ntth.grandsoniq.feature.library.ui.PlaylistDetailScreen
import ntu.ntth.grandsoniq.feature.player.service.PlaybackController
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqBackground
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@Composable
fun GrandSoniqNavHost(playbackController: PlaybackController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playbackSnapshot by playbackController.snapshot.collectAsStateWithLifecycle()

    val selectedTab = when {
        currentRoute == AppDestination.Home.route -> MainNavTab.HOME
        currentRoute == AppDestination.Discover.route -> MainNavTab.DISCOVER
        currentRoute == AppDestination.Friends.route -> MainNavTab.FRIENDS
        currentRoute == AppDestination.Library.route ||
            currentRoute?.startsWith("playlist/") == true ||
            currentRoute?.startsWith("album/") == true ||
            currentRoute?.startsWith("artist/") == true -> MainNavTab.LIBRARY
        else -> MainNavTab.HOME
    }

    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val homeViewModel = ViewModelProvider(owner)[HomeViewModel::class.java]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrandSoniqBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppDestination.Home.route) {
                    HomeScreen(
                        state = homeViewModel.state,
                        activeSongId = playbackSnapshot.currentSong?.id,
                        onRetry = homeViewModel::reload,
                        onSongClick = { song ->
                            playbackController.playSong(song, homeViewModel.state.songs)
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
                    TabPlaceholderScreen(
                        title = "Khám phá",
                        subtitle = "Tìm kiếm, bảng xếp hạng, thể loại và nhận diện bài hát."
                    )
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

                composable(AppDestination.Friends.route) {
                    TabPlaceholderScreen(
                        title = "Bạn bè",
                        subtitle = "Hoạt động bạn bè, phòng nghe chung và playlist cộng tác."
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

        GrandSoniqBottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                val targetRoute = when (tab) {
                    MainNavTab.HOME -> AppDestination.Home.route
                    MainNavTab.DISCOVER -> AppDestination.Discover.route
                    MainNavTab.LIBRARY -> AppDestination.Library.route
                    MainNavTab.FRIENDS -> AppDestination.Friends.route
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

@Composable
private fun TabPlaceholderScreen(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrandSoniqBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = GrandSoniqTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            text = subtitle,
            color = GrandSoniqTextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111514)
@Composable
private fun TabPlaceholderScreenPreview() {
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        TabPlaceholderScreen(
            title = "Khám phá",
            subtitle = "Tìm kiếm, bảng xếp hạng, thể loại và nhận diện bài hát."
        )
    }
}
