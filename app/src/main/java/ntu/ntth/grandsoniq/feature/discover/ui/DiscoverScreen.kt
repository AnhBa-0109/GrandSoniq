package ntu.ntth.grandsoniq.feature.discover.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ntu.ntth.grandsoniq.R
import ntu.ntth.grandsoniq.core.model.Song
import ntu.ntth.grandsoniq.core.ui.AssetSvgIcon
import ntu.ntth.grandsoniq.core.ui.GrandSoniqHeader
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverGenre
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverList
import ntu.ntth.grandsoniq.feature.discover.viewmodel.DiscoverViewModel

private val Background = Color(0xFF101311)
private val Panel = Color(0xFF191E1B)
private val Tile = Color(0xFF242B26)
private val Border = Color(0xFF343D36)
private val Foreground = Color(0xFFF3F5EF)
private val Muted = Color(0xFFB2BBB4)
private val Accent = Color(0xFFD5F47A)
private val SidePadding = 20.dp
private val ChartPreviewSize = 2

@Composable
internal fun DiscoverScreen(viewModel: DiscoverViewModel) {
    var showChart by rememberSaveable { mutableStateOf(false) }
    var genreId by rememberSaveable { mutableStateOf<String?>(null) }
    val overviewScroll = rememberLazyListState()
    val selectedGenre = viewModel.genres.items.firstOrNull { it.id == genreId }
    val showDetail = showChart || selectedGenre != null
    val closeDetail = {
        showChart = false
        genreId = null
    }

    BackHandler(enabled = showDetail, onBack = closeDetail)
    LaunchedEffect(selectedGenre?.id) {
        if (selectedGenre != null && viewModel.genreSongsId != selectedGenre.id) {
            viewModel.loadGenre(selectedGenre.id)
        }
    }

    Box(
        Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            if (showDetail) {
                val state = when {
                    selectedGenre == null -> viewModel.chart
                    viewModel.genreSongsId == selectedGenre.id -> viewModel.genreSongs
                    else -> DiscoverList<Song>()
                }
                SongListDetail(
                    title = selectedGenre?.name ?: stringResource(R.string.discover_chart),
                    state = state,
                    ranked = showChart,
                    onBack = closeDetail,
                    onRetry = {
                        if (selectedGenre == null) viewModel.loadChart()
                        else viewModel.loadGenre(selectedGenre.id)
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                GrandSoniqHeader(title = stringResource(R.string.nav_discover))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = overviewScroll,
                    contentPadding = PaddingValues(start = SidePadding, end = SidePadding, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    item { DiscoverSearchEntry() }
                    item {
                        ChartPreview(
                            state = viewModel.chart,
                            onSeeMore = { showChart = true },
                            onRetry = viewModel::loadChart
                        )
                    }
                    item { NewReleases(viewModel.releases, viewModel::loadReleases) }
                    item {
                        DiscoverGenreGrid(
                            state = viewModel.genres,
                            onRetry = viewModel::loadGenres,
                            onSelect = { genre ->
                                viewModel.loadGenre(genre.id)
                                genreId = genre.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverSearchEntry() {
    val shape = RoundedCornerShape(16.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).heightIn(min = 52.dp).background(Panel, shape)
                .border(1.dp, Border, shape).padding(horizontal = 17.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetSvgIcon("discover/search.svg", 22.dp)
            Text(
                stringResource(R.string.discover_search_hint), color = Muted,
                fontSize = 16.sp, lineHeight = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Box(Modifier.size(52.dp).background(Tile, shape).border(1.dp, Border, shape), Alignment.Center) {
            AssetSvgIcon("discover/recognize.svg", 24.dp, stringResource(R.string.discover_recognize))
        }
    }
}

@Composable
internal fun DiscoverGenreGrid(
    state: DiscoverList<DiscoverGenre>,
    onRetry: () -> Unit,
    onSelect: (DiscoverGenre) -> Unit
) {
    val gradient = Brush.verticalGradient(
        0f to Tile,
        0.5f to Color(0xFF1C211E),
        1f to Color(0xFF121513)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DiscoverHeading(stringResource(R.string.discover_genres))
        DiscoverListStatus(
            state,
            onRetry,
            emptyMessage = R.string.discover_genres_empty,
            errorMessage = R.string.discover_genres_load_failed
        )
        state.items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { genre ->
                    Box(
                        Modifier.weight(1f).height(108.dp).clip(RoundedCornerShape(12.dp))
                            .background(gradient).clickable(role = Role.Button) { onSelect(genre) }
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            genre.name, color = Foreground, fontSize = 18.sp,
                            lineHeight = 26.sp, fontWeight = FontWeight.Medium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChartPreview(state: DiscoverList<Song>, onSeeMore: () -> Unit, onRetry: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscoverHeading(stringResource(R.string.discover_chart), Modifier.weight(1f))
            Box(
                Modifier.heightIn(min = 32.dp).clickable(role = Role.Button, onClick = onSeeMore),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.discover_more), color = Muted, fontSize = 12.sp,
                    lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        DiscoverListStatus(state, onRetry)
        state.items.take(ChartPreviewSize).forEachIndexed { index, song ->
            if (index > 0) SongDivider()
            DiscoverSongRow(song, rank = index + 1)
        }
    }
}

@Composable
private fun NewReleases(state: DiscoverList<Song>, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DiscoverHeading(stringResource(R.string.discover_releases))
        DiscoverListStatus(state, onRetry)
        if (state.items.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.items.forEach { song ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Artwork(
                            song.coverUrl, Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(8.dp)
                        )
                        DiscoverSongText(song)
                    }
                }
                if (state.items.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SongListDetail(
    title: String,
    state: DiscoverList<Song>,
    ranked: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = SidePadding, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.discover_back), tint = Foreground
                )
            }
            DiscoverHeading(title)
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = SidePadding)) {
            if (state.items.isEmpty()) item { DiscoverListStatus(state, onRetry) }
            itemsIndexed(state.items) { index, song ->
                if (index > 0) SongDivider()
                DiscoverSongRow(song, rank = if (ranked) index + 1 else null)
            }
        }
    }
}

@Composable
private fun DiscoverSongRow(song: Song, rank: Int?) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rank != null) {
            Text(
                rank.toString(), Modifier.width(24.dp), color = if (rank == 1) Accent else Muted,
                fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
        Artwork(song.coverUrl, Modifier.size(52.dp), RoundedCornerShape(8.dp))
        DiscoverSongText(song, Modifier.weight(1f), emphasized = rank == null || rank == 1)
    }
}

@Composable
private fun DiscoverSongText(song: Song, modifier: Modifier = Modifier, emphasized: Boolean = true) {
    Column(modifier) {
        Text(
            song.title.ifBlank { song.id }, color = Foreground, fontSize = 16.sp, lineHeight = 24.sp,
            fontWeight = if (emphasized) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        if (song.artistName.isNotBlank()) {
            Text(
                song.artistName, color = Muted, fontSize = 14.sp, lineHeight = 20.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiscoverListStatus(
    state: DiscoverList<*>,
    onRetry: () -> Unit,
    @StringRes emptyMessage: Int = R.string.discover_empty,
    @StringRes errorMessage: Int = R.string.discover_load_failed
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxWidth().padding(12.dp), Alignment.Center) {
            CircularProgressIndicator(Modifier.size(24.dp), color = Accent)
        }
        state.hasError -> Column {
            Text(stringResource(errorMessage), color = Muted, fontSize = 14.sp)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.discover_retry), color = Accent) }
        }
        state.items.isEmpty() -> Text(stringResource(emptyMessage), color = Muted, fontSize = 14.sp)
    }
}

@Composable
private fun DiscoverHeading(title: String, modifier: Modifier = Modifier) {
    Text(
        title, modifier.semantics { heading() }, color = Foreground,
        fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SongDivider() {
    HorizontalDivider(color = Border.copy(alpha = 0.4f))
}
