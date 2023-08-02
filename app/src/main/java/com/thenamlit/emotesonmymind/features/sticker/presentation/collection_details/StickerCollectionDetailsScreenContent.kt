package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.components.DisplaySticker
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.File


private const val tag = "${Logging.loggingPrefix}StickerCollectionDetailsScreenContent"

@Composable
fun StickerCollectionDetailsScreenContent(
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    mode: StickerCollectionDetailsMode,
    selectedStickerIdsInDeleteMode: List<String>,
    stickers: List<Sticker>,
    onStickerClicked: (Sticker) -> Unit,
    stickerImageFile: (String) -> File?,
) {
    Log.d(
        tag, "StickerCollectionDetailsScreenContent | modifier: $modifier, " +
                "itemModifier: $itemModifier, " +
                "imageLoader: $imageLoader, " +
                "mode: $mode, " +
                "selectedStickerIdsInDeleteMode: $selectedStickerIdsInDeleteMode" +
                "stickers: $stickers"
    )

    Column(modifier = modifier) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(count = 3),
//            columns = GridCells.Adaptive(minSize = 128.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            items(stickers) { sticker: Sticker ->
                DisplaySticker(
                    modifier = getModifierByMode(
                        itemModifier = itemModifier,
                        mode = mode,
                        sticker = sticker,
                        selectedStickerIdsInDeleteMode = selectedStickerIdsInDeleteMode
                    ),
                    sticker = sticker,
                    imageLoader = imageLoader,
                    stickerImageFile = stickerImageFile,
                    onClick = { onStickerClicked(sticker) }
                )
            }
        }
    }
}

private fun getModifierByMode(
    itemModifier: Modifier,
    mode: StickerCollectionDetailsMode,
    sticker: Sticker,
    selectedStickerIdsInDeleteMode: List<String>,
): Modifier {
    Log.d(
        tag, "getModifierByMode | itemModifier: $itemModifier, " +
                "mode: $mode, " +
                "sticker: $sticker, " +
                "selectedStickerIdsInDeleteMode: $selectedStickerIdsInDeleteMode"
    )

    return when (mode) {
        is StickerCollectionDetailsMode.Normal -> {
            itemModifier
        }

        is StickerCollectionDetailsMode.Edit -> {
            itemModifier
        }

        is StickerCollectionDetailsMode.DeleteSticker -> {
            if (sticker.id in selectedStickerIdsInDeleteMode) {
                itemModifier.border(3.dp, color = Color.Red)
            } else {
                itemModifier
            }
        }
    }
}
