package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.File


private const val tag = "${Logging.loggingPrefix}DisplaySticker"


@Composable
fun DisplaySticker(
    modifier: Modifier = Modifier,
    sticker: Sticker,
    imageLoader: ImageLoader,
    stickerImageFile: (String) -> File?,
    onClick: () -> Unit,
) {
    Log.d(
        tag,
        "DisplaySticker | modifier: $modifier, sticker: $sticker, imageLoader: $imageLoader"
    )

    val localStickerImageFilePath =
        "${LocalContext.current.filesDir.absolutePath}/stickers/${sticker.remoteEmoteData.id}.webp"

    stickerImageFile(localStickerImageFilePath)?.let { localStickerImageFile: File ->
        DefaultCoilLocalImage(
            modifier = modifier.clickable { onClick() },
            localImageFile = localStickerImageFile,
            imageLoader = imageLoader
        )
    } ?: run {
        Log.e(tag, "Couldn't load Sticker Image File from local storage...getting from Remote")

        DefaultCoilImage(
            modifier = modifier.clickable { onClick() },
            url = sticker.remoteEmoteData.url,
            imageLoader = imageLoader
        )
    }
}
