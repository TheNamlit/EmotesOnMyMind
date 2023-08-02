package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.thenamlit.emotesonmymind.core.presentation.components.AutoResizedText
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultCoilImage
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails


private const val tag = "${Logging.loggingPrefix}EmoteDetailsScreenContent"

@Composable
fun EmoteDetailsScreenContent(
    modifier: Modifier = Modifier,
    emoteDetails: EmoteDetails,
    imageLoader: ImageLoader,
    emoteToStickerButtonState: EmoteToStickerButtonState,
    onDownloadButtonClick: (EmoteDetails) -> Unit,
    onNavigateToStickerDetailsButtonClick: () -> Unit,
) {
    Log.d(
        tag,
        "EmoteDetailsScreenContent | modifier: $modifier, " +
                "emoteDetails: $emoteDetails, " +
                "imageLoader: $imageLoader, " +
                "emoteToStickerButtonState: $emoteToStickerButtonState"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmoteName(modifier = Modifier.fillMaxWidth(), emoteName = emoteDetails.name)

        Spacer(modifier = Modifier.padding(top = 5.dp))

        EmoteImage(
            imageUrl = "${emoteDetails.host.url}/${emoteDetails.host.defaultFileName}",
            imageLoader = imageLoader
        )

        Divider(modifier = Modifier.fillMaxWidth(0.85f))

        // TODO: Display Width, Height, Size

        // TODO: Display Tags

        when (emoteToStickerButtonState) {
            is EmoteToStickerButtonState.Loading -> {
                EmoteToStickerButton(
                    text = emoteToStickerButtonState.buttonText,
                    enabled = emoteToStickerButtonState.enabled,
                    onClick = {}
                )
            }

            is EmoteToStickerButtonState.SaveEmoteAsSticker -> {
                EmoteToStickerButton(
                    text = emoteToStickerButtonState.buttonText,
                    enabled = emoteToStickerButtonState.enabled,
                    onClick = {
                        onDownloadButtonClick(emoteDetails)
                    }
                )
            }

            is EmoteToStickerButtonState.DownloadInProgress -> {
                EmoteToStickerButton(
                    text = emoteToStickerButtonState.buttonText,
                    enabled = emoteToStickerButtonState.enabled,
                    onClick = { }
                )
            }

            is EmoteToStickerButtonState.DownloadFailed -> {
                EmoteToStickerButton(
                    text = emoteToStickerButtonState.buttonText,
                    enabled = emoteToStickerButtonState.enabled,
                    onClick = {
                        // TODO: Build separate function for Retry Downloading?
                        onDownloadButtonClick(emoteDetails)
                    }
                )
            }

            is EmoteToStickerButtonState.GoToStickerDetails -> {
                EmoteToStickerButton(
                    text = emoteToStickerButtonState.buttonText,
                    enabled = emoteToStickerButtonState.enabled,
                    onClick = {
                        onNavigateToStickerDetailsButtonClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun EmoteName(modifier: Modifier = Modifier, emoteName: String) {
    Log.d(tag, "EmoteName | modifier: $modifier, emoteName: $emoteName")

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        AutoResizedText(
            titleText = emoteName,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
private fun EmoteImage(imageUrl: String, imageLoader: ImageLoader) {
    Log.d(tag, "EmoteImage | imageUrl: $imageUrl, imageLoader: $imageLoader")

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        DefaultCoilImage(
            modifier = Modifier.fillMaxSize(0.33f),
            url = imageUrl,
            imageLoader = imageLoader
        )
    }
}

@Composable
private fun EmoteToStickerButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Log.d(tag, "EmoteToStickerButton | text: $text, enabled: $enabled")

    Button(
        enabled = enabled,
        onClick = { onClick() }
    ) {
        Text(text = text)
    }
}
