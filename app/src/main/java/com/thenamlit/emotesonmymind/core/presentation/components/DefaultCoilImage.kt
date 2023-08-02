package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import java.io.File


private const val tag = "${Logging.loggingPrefix}DefaultCoilImage"

@Composable
fun DefaultCoilImage(modifier: Modifier = Modifier, url: String, imageLoader: ImageLoader) {
    Log.d(tag, "DefaultCoilImage | modifier: $modifier, url: $url imageLoader: $imageLoader")

    // https://stackoverflow.com/questions/60229555/adding-gif-into-jetpack-compose
    val context = LocalContext.current
    val model = ImageRequest.Builder(context)
        .data(data = url)
        .apply(block = { size(Size.ORIGINAL) })
        .build()

    val painter = rememberAsyncImagePainter(
        model = model,
        imageLoader = imageLoader,
    )

    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = stringResource(id = R.string.default_coil_image_content_description)
    )
}

@Composable
fun DefaultCoilLocalImage(
    modifier: Modifier = Modifier,
    localImageFile: File,
    imageLoader: ImageLoader,
) {
    Log.d(
        tag,
        "DefaultCoilLocalImage | modifier: $modifier, " +
                "localImageFile: $localImageFile " +
                "imageLoader: $imageLoader"
    )

    // https://stackoverflow.com/questions/60229555/adding-gif-into-jetpack-compose
    val context = LocalContext.current
    val model = ImageRequest.Builder(context)
        .data(data = localImageFile)
        .apply(block = { size(Size.ORIGINAL) })
        .build()

    val painter = rememberAsyncImagePainter(
        model = model,
        imageLoader = imageLoader,
    )

    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = stringResource(id = R.string.default_coil_local_image_content_description)
    )
}
