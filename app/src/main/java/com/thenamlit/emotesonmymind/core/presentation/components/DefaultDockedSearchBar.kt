package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem


private const val tag = "${Logging.loggingPrefix}DefaultDockedSearchBar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDockedSearchBar(
    modifier: Modifier,
    query: String,
    searchHistory: List<MainFeedEmoteSearchHistoryItem>,
    searchActive: Boolean,
    placeholderText: String,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchBarCloseIconClicked: () -> Unit,
) {
    Log.d(
        tag,
        "DefaultDockedSearchBar | modifier: $modifier, " +
                "query: $query, " +
                "searchHistory: $searchHistory, " +
                "searchActive: $searchActive, " +
                "placeholderText: $placeholderText"
    )

    DockedSearchBar(
        modifier = modifier,
        query = query,
        onQueryChange = { changedSearchQuery: String -> onSearchQueryChange(changedSearchQuery) },
        onSearch = { onSearch() },
        active = searchActive,
        onActiveChange = { isActive: Boolean -> onSearchActiveChanged(isActive) },
        placeholder = { Text(text = placeholderText) },
        leadingIcon = { DefaultDockedSearchBarLeadingIcon() },
        trailingIcon = {
            if (searchActive) {
                DefaultDockerSearchBarTrailingIcon(
                    modifier = Modifier.clickable { onSearchBarCloseIconClicked() }
                )
            }
        }
    ) {
        DefaultDockedSearchBarContent(
            searchedQuery = query,
            searchHistory = searchHistory,
            onSearchQueryChange = onSearchQueryChange,
            onSearch = onSearch
        )
    }
}

@Composable
private fun DefaultDockedSearchBarContent(
    searchedQuery: String,
    searchHistory: List<MainFeedEmoteSearchHistoryItem> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Log.d(tag, "SearchBarContent | searchedQuery: $searchedQuery, searchHistory: $searchHistory")

    if (searchedQuery != "") {
        SearchBarContentRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable {
                    onSearchQueryChange("")
                    onSearch()
                },
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = stringResource(id = R.string.main_feed_screen_search_bar_content_reset_text))
        }

        Divider()
    }

    LazyColumn {
        items(searchHistory) {
            SearchBarContentRow(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable {
                        onSearchQueryChange(it.value)
                        onSearch()
                    },
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    modifier = Modifier.padding(end = 10.dp),
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(
                        id = R.string.main_feed_screen_search_bar_content_leading_icon_content_description
                    )
                )
                Text(text = it.value)
            }
        }
    }
}

@Composable
private fun SearchBarContentRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    content: @Composable () -> Unit,
) {
    Log.d(
        tag,
        "SearchBarContentRow | modifier: $modifier, " +
                "horizontalArrangement: $horizontalArrangement, " +
                "content: $content"
    )

    Row(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        content()
    }
}

@Composable
private fun DefaultDockedSearchBarLeadingIcon(modifier: Modifier = Modifier) {
    Log.d(tag, "DefaultDockedSearchBarLeadingIcon | modifier: $modifier")

    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Search,
        contentDescription = stringResource(
            id = R.string.default_docked_search_bar_leading_icon_content_description
        ),
    )
}

@Composable
private fun DefaultDockerSearchBarTrailingIcon(modifier: Modifier = Modifier) {
    Log.d(tag, "DefaultDockerSearchBarTrailingIcon | modifier: $modifier")

    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(
            id = R.string.default_docked_search_bar_trailing_icon_content_description
        ),
    )
}
