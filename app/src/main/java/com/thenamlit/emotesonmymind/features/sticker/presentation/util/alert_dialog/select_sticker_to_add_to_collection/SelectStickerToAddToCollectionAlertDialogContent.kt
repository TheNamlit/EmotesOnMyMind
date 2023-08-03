package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.components.DisplaySticker
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.File


private const val tag = "${Logging.loggingPrefix}SelectStickerToAddToCollectionAlertDialogContent"

@Composable
fun SelectStickerToAddToCollectionAlertDialogContent(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    notSelectedStickerList: List<Sticker>,
    onNotSelectedStickerClicked: (Sticker) -> Unit,
    selectedStickerList: List<Sticker>,
    onSelectedStickerClicked: (Sticker) -> Unit,
    stickerImageFile: (String) -> File?,
) {
    Log.d(
        tag, "SelectStickerToAddToCollectionAlertDialogContent | modifier: $modifier, " +
                "imageLoader: $imageLoader, " +
                "notSelectedStickerList: $notSelectedStickerList, " +
                "selectedStickerList: $selectedStickerList"
    )

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Selected to add to collection")

            DisplaySelectedStickerGrid(
                imageLoader = imageLoader,
                selectedStickerList = selectedStickerList,
                onSelectedStickerClicked = onSelectedStickerClicked,
                stickerImageFile = stickerImageFile
            )
        }

        Divider()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "All not included in collection")

            DisplayNotSelectedStickerGrid(
                imageLoader = imageLoader,
                notSelectedStickerList = notSelectedStickerList,
                onNotSelectedStickerClicked = onNotSelectedStickerClicked,
                stickerImageFile = stickerImageFile
            )
        }
    }
}

@Composable
private fun DisplaySelectedStickerGrid(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    selectedStickerList: List<Sticker>,
    onSelectedStickerClicked: (Sticker) -> Unit,
    stickerImageFile: (String) -> File?,
) {
    Log.d(
        tag, "DisplaySelectedStickerGrid | modifier: $modifier, " +
                "imageLoader: $imageLoader, " +
                "selectedStickerList: $selectedStickerList"
    )

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        items(selectedStickerList) { sticker: Sticker ->
            DisplaySticker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                sticker = sticker,
                imageLoader = imageLoader,
                stickerImageFile = stickerImageFile,
                onClick = { onSelectedStickerClicked(sticker) }
            )
        }
    }
}

@Composable
private fun DisplayNotSelectedStickerGrid(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    notSelectedStickerList: List<Sticker>,
    onNotSelectedStickerClicked: (Sticker) -> Unit,
    stickerImageFile: (String) -> File?,
) {
    Log.d(
        tag, "DisplayNotSelectedStickerGrid | modifier: $modifier, " +
                "imageLoader: $imageLoader, " +
                "notSelectedStickerList: $notSelectedStickerList"
    )

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        items(notSelectedStickerList) { sticker: Sticker ->
            DisplaySticker(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                sticker = sticker,
                imageLoader = imageLoader,
                stickerImageFile = stickerImageFile,
                onClick = { onNotSelectedStickerClicked(sticker) }
            )
        }
    }
}
