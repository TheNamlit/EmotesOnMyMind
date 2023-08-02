package com.thenamlit.emotesonmymind.features.sticker.presentation.library

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.presentation.components.DisplaySticker
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import java.io.File


private const val tag = "${Logging.loggingPrefix}LibraryScreenContent"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    stickerCollections: List<StickerCollection>,
    onStickerCollectionRowClicked: (String) -> Unit,
    filterChipItems: List<LibraryScreenStateFilterChipItem>,
    selectedTabIndex: Int,
    selectedTabCollections: Boolean,
    selectedTabStickers: Boolean,
    onCollectionsTabClicked: () -> Unit,
    onStickersTabClicked: () -> Unit,
    stickers: List<Sticker>,
    onStickerClicked: (Sticker) -> Unit,
    stickerImageFile: (String) -> File?,
    setIsSwipeToTheLeft: (Boolean) -> Unit,
    updateTabIndexBasedOnSwipe: () -> Unit,
) {
    Log.d(
        tag,
        "LibraryScreenContent | modifier: $modifier, " +
                "imageLoader: $imageLoader, " +
                "selectedTabIndex: $selectedTabIndex, " +
                "selectedTabCollections: $selectedTabCollections, " +
                "selectedTabStickers: $selectedTabStickers, " +
                "filterChipItems: $filterChipItems, " +
                "stickerCollections: $stickerCollections, " +
                "stickers: $stickers"
    )

    Column(modifier = modifier) {
        LazyRow(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        ) {
            items(filterChipItems) { filterChipItem: LibraryScreenStateFilterChipItem ->
                FilterChip(
                    modifier = Modifier.padding(4.dp),
                    selected = filterChipItem.selected,
                    onClick = { filterChipItem.onClick(filterChipItem) },
                    label = { Text(text = filterChipItem.type.label) }
                )
            }
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabCollections,
                onClick = { onCollectionsTabClicked() },
                text = { Text(text = "Collections") }
            )
            Tab(
                selected = selectedTabStickers,
                onClick = { onStickersTabClicked() },
                text = { Text(text = "Stickers") }
            )
        }

        // https://www.droidcon.com/2023/03/02/tabs-in-jetpack-compose/
        // TODO: Implement a smoother transition, where the content of the other side is loaded
        //  See Twitter-Feed-Implementation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = DraggableState(
                        onDelta = { delta ->
                            setIsSwipeToTheLeft(delta > 0)
                        }
                    ),
                    orientation = Orientation.Horizontal,
                    onDragStarted = {},
                    onDragStopped = { updateTabIndexBasedOnSwipe() }
                )
        ) {
            // TODO: Implement pagination

            // TODO: Display StickerCollection-Icon (Not yet implemented -> User can choose this one)
            //  Must be PNG afaik

            if (selectedTabCollections) {
                LazyColumn {
                    items(items = stickerCollections) { stickerCollection: StickerCollection ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { onStickerCollectionRowClicked(stickerCollection.id) },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stickerCollection.name} " +
                                        "(${stickerCollection.stickers.size}/" +
                                        "${WhatsAppSettings.MAXIMUM_STICKER_AMOUNT})",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Divider()
                    }
                }
            }

            if (selectedTabStickers) {
                LazyVerticalGrid(
                    modifier = modifier,
                    columns = GridCells.Fixed(3),
//                    columns = GridCells.Adaptive(minSize = 128.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(stickers) { sticker: Sticker ->
                        DisplaySticker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            sticker = sticker,
                            imageLoader = imageLoader,
                            stickerImageFile = stickerImageFile,
                            onClick = { onStickerClicked(sticker) }
                        )
                    }
                }
            }
        }
    }
}
