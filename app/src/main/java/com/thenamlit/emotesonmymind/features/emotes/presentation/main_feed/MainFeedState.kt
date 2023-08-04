package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import androidx.compose.ui.focus.FocusRequester
import com.apollographql.apollo3.api.Optional
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.type.EmoteSearchFilter
import com.thenamlit.emotesonmymind.type.ImageFormat
import com.thenamlit.emotesonmymind.type.Sort
import com.thenamlit.emotesonmymind.type.SortOrder


data class MainFeedState(
    val query: String = "",
    val page: Int = 1,
    val limit: Int = 20,
    val sort: Sort = Sort(order = SortOrder.DESCENDING, value = "popularity"),
    val formats: List<ImageFormat> = listOf(ImageFormat.WEBP),
    val filter: EmoteSearchFilter = EmoteSearchFilter(
        // Only decides whether NotAnimated are visible or not, doesn't result in only showing NotAnimated
        animated = Optional.present(value = false),
        zero_width = Optional.present(value = false)    // TODO: Check if they are okay to use
    ),

    val isLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,

    val filterChipItems: List<MainFeedScreenStateFilterChipItem> = emptyList(),

    // TODO: Outsource to Variable | Doesn't work in landscape
    val searchBarHeight: Float = 0.08f,
    val searchedQuery: String = "",
    val searchBarFocusRequester: FocusRequester = FocusRequester(),
    val searchActive: Boolean = false,

    val count: Int = 0,
    val emotes: List<MainFeedEmote> = emptyList(),
    val displayedEmotes: List<MainFeedEmote> = emptyList(),

    val searchHistory: List<MainFeedEmoteSearchHistoryItem> = emptyList(),
)

data class MainFeedScreenStateFilterChipItem(
    val selected: Boolean,
    val onClick: (MainFeedScreenStateFilterChipItem) -> Unit,
    val type: MainFeedScreenStateFilterChipItemType,
)

// TODO: Use StringResources instead of hardcoded String
sealed class MainFeedScreenStateFilterChipItemType(val label: String) {
    object Animated : MainFeedScreenStateFilterChipItemType(label = "Animated")
    object NotAnimated : MainFeedScreenStateFilterChipItemType(label = "Not Animated")
}
