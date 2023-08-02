package com.thenamlit.emotesonmymind.features.sticker.presentation.library

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection


data class LibraryScreenState(
    val stickerCollections: List<StickerCollection> = emptyList(),
    val animatedStickerCollections: List<StickerCollection> = emptyList(),
    val notAnimatedStickerCollections: List<StickerCollection> = emptyList(),

    val stickers: List<Sticker> = emptyList(),
    val animatedStickers: List<Sticker> = emptyList(),
    val notAnimatedStickers: List<Sticker> = emptyList(),

    val filterChipItems: List<LibraryScreenStateFilterChipItem> = emptyList(),

    val selectedTabIndex: Int = 0,
    val selectedTabCollections: Boolean = true,
    val selectedTabStickers: Boolean = false,
    val isSwipeToTheLeft: Boolean = false,

    val showCreateCollectionAlertDialog: Boolean = false,
)

data class LibraryScreenStateFilterChipItem(
    val selected: Boolean,
    val onClick: (LibraryScreenStateFilterChipItem) -> Unit,
    val type: LibraryScreenStateFilterChipItemType,
)

// TODO: Use StringResources instead of hardcoded String
sealed class LibraryScreenStateFilterChipItemType(val label: String) {
    object Animated : LibraryScreenStateFilterChipItemType(label = "Animated")
    object NotAnimated : LibraryScreenStateFilterChipItemType(label = "Not Animated")
}
