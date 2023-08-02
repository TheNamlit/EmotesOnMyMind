package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.components.AutoResizedText
import com.thenamlit.emotesonmymind.core.presentation.components.DisplaySticker
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.File


private const val tag = "${Logging.loggingPrefix}StickerDetailsScreenContent"

@Composable
fun StickerDetailsScreenContent(
    modifier: Modifier = Modifier,
    sticker: Sticker,
    imageLoader: ImageLoader,
    addToCollectionButtonOnClick: () -> Unit,
    deleteStickerButtonOnClick: () -> Unit,
    stickerImageFile: (String) -> File?,
) {
    Log.d(
        tag,
        "StickerDetailsScreenContent | modifier: $modifier, " +
                "sticker: $sticker, " +
                "imageLoader: $imageLoader"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StickerName(modifier = Modifier.fillMaxWidth(), stickerName = sticker.name)

        Spacer(modifier = Modifier.padding(top = 5.dp))

        DisplaySticker(
            sticker = sticker,
            imageLoader = imageLoader,
            stickerImageFile = stickerImageFile,
            onClick = {}
        )

        Divider(modifier = Modifier.fillMaxWidth(0.85f))

        AddToCollectionButton(
            modifier = Modifier.fillMaxWidth(),
            addToCollectionButtonOnClick = addToCollectionButtonOnClick,
        )

        DeleteStickerButton(
            modifier = Modifier.fillMaxWidth(),
            deleteStickerButtonOnClick = deleteStickerButtonOnClick,
        )
    }
}

@Composable
private fun StickerName(modifier: Modifier = Modifier, stickerName: String) {
    Log.d(tag, "StickerName | modifier: $modifier, stickerName: $stickerName")

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        AutoResizedText(
            titleText = stickerName,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
private fun AddToCollectionButton(
    modifier: Modifier = Modifier,
    addToCollectionButtonOnClick: () -> Unit,
) {
    Log.d(tag, "AddToCollectionButton | modifier: $modifier")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { addToCollectionButtonOnClick() }) {
            Text(text = "Add to Collection")
        }
    }
}

@Composable
private fun DeleteStickerButton(
    modifier: Modifier = Modifier,
    deleteStickerButtonOnClick: () -> Unit,
) {
    Log.d(tag, "DeleteStickerButton | modifier: $modifier")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { deleteStickerButtonOnClick() }) {
            Text(text = "Delete")
        }
    }
}
