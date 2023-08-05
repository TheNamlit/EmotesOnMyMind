package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultCoilImage
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultDockedSearchBar
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem


private const val tag = "${Logging.loggingPrefix}MainFeedScreenContent"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedScreenContent(
    modifier: Modifier,
    searchBarFocusRequester: FocusRequester,
    imageLoader: ImageLoader,
    query: String,
    isLoading: Boolean,
    filterChipItems: List<MainFeedScreenStateFilterChipItem>,
    searchBarHeight: Float,
    searchActive: Boolean,
    searchHistory: List<MainFeedEmoteSearchHistoryItem>,
    displayedEmotes: List<MainFeedEmote>,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchBarCloseIconClicked: () -> Unit,
    onEmoteClicked: (MainFeedEmote) -> Unit,
    checkForLoadNextEmotes: (Int) -> Unit,
) {
    Log.d(
        tag,
        "MainFeedScreenContent | modifier: $modifier, " +
                "searchBarFocusRequester: $searchBarFocusRequester, " +
                "imageLoader: $imageLoader, " +
                "query: $query, " +
                "isLoading: $isLoading, " +
                "filterChipItems: $filterChipItems, " +
                "searchBarHeight: $searchBarHeight, " +
                "searchActive: $searchActive, " +
                "searchHistory: $searchHistory, " +
                "displayedEmotes: $displayedEmotes"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DefaultDockedSearchBar(
            modifier = Modifier
                .focusRequester(focusRequester = searchBarFocusRequester)
                .fillMaxHeight(searchBarHeight),
            query = query,
            searchHistory = searchHistory,
            searchActive = searchActive,
            placeholderText = stringResource(id = R.string.main_feed_screen_search_bar_placeholder),
            onSearchActiveChanged = onSearchActiveChanged,
            onSearch = onSearch,
            onSearchQueryChange = onSearchQueryChange,
            onSearchBarCloseIconClicked = onSearchBarCloseIconClicked,
        )

        LazyRow(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        ) {
            items(filterChipItems) { filterChipItem: MainFeedScreenStateFilterChipItem ->
                FilterChip(
                    modifier = Modifier.padding(4.dp),
                    selected = filterChipItem.selected,
                    onClick = { filterChipItem.onClick(filterChipItem) },
                    label = { Text(text = filterChipItem.type.label) }
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 20.dp))

        EmoteGrid(
            modifier = Modifier.fillMaxSize(),
            mainFeedEmotes = displayedEmotes,
            imageLoader = imageLoader,
            onEmoteClicked = { mainFeedEmote: MainFeedEmote ->
                onEmoteClicked(mainFeedEmote)
            },
            checkForLoadNextEmotes = checkForLoadNextEmotes,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun EmoteGrid(
    modifier: Modifier = Modifier,
    mainFeedEmotes: List<MainFeedEmote>,
    onEmoteClicked: (MainFeedEmote) -> Unit,
    imageLoader: ImageLoader,
    checkForLoadNextEmotes: (Int) -> Unit,
    isLoading: Boolean,
) {
    Log.d(
        tag,
        "EmoteGrid | modifier: $modifier, " +
                "mainFeedEmotes: $mainFeedEmotes, " +
                "imageLoader: $imageLoader"
    )

    // https://developer.android.com/jetpack/compose/lists#lazy-grids
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
//        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        items(mainFeedEmotes.size) { index: Int ->
            val mainFeedEmote: MainFeedEmote = mainFeedEmotes[index]
            checkForLoadNextEmotes(index)

            DisplayEmote(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .clickable {
                        onEmoteClicked(mainFeedEmote)
                    },
                mainFeedEmote = mainFeedEmote,
                imageLoader = imageLoader
            )
        }

        item {
            if (isLoading) {
                // TODO: Fix the size of the indicator
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DisplayEmote(
    modifier: Modifier = Modifier,
    mainFeedEmote: MainFeedEmote,
    imageLoader: ImageLoader,
) {
    Log.d(
        tag,
        "DisplayEmote | modifier: $modifier, " +
                "mainFeedEmote: $mainFeedEmote, " +
                "imageLoader: $imageLoader"
    )

    DefaultCoilImage(
        modifier = modifier,
        url = "${mainFeedEmote.host.url}/${mainFeedEmote.host.defaultFileName}",
        imageLoader = imageLoader
    )
}
