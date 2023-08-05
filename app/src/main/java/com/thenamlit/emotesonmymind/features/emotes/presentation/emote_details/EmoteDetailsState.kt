package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import androidx.compose.material3.SnackbarHostState
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsHost
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsOwner
import com.thenamlit.emotesonmymind.type.ImageFormat
import java.util.UUID


data class EmoteDetailsState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),

    val formats: List<ImageFormat> = listOf(ImageFormat.WEBP),
    val emoteDetails: EmoteDetails = getDefaultEmoteDetails(),

    val showEmoteUserAlertDialog: Boolean = false,
    val emoteToStickerButtonState: EmoteToStickerButtonState = EmoteToStickerButtonState.Loading,

    val sticker: Sticker? = null,

    val workerState: WorkerState = WorkerState.Idle,
)

sealed class WorkerState {
    object Idle : WorkerState()
    class Processing(val workId: UUID) : WorkerState()
    object Success : WorkerState()
    class Failed(val message: String) : WorkerState()
}

// TODO: Use StringResources instead of hardcoded Strings
sealed class EmoteToStickerButtonState(val buttonText: String, val enabled: Boolean) {
    object Loading : EmoteToStickerButtonState(buttonText = "Loading...", enabled = false)
    object SaveEmoteAsSticker :
        EmoteToStickerButtonState(buttonText = "Save as Sticker", enabled = true)

    object DownloadInProgress :
        EmoteToStickerButtonState(buttonText = "Downloading...", enabled = false)

    object DownloadFailed :
        EmoteToStickerButtonState(buttonText = "Failed downloading - Try again", enabled = true)

    object GoToStickerDetails :
        EmoteToStickerButtonState(buttonText = "Sticker Details", enabled = true)
}

private fun getDefaultEmoteDetails(): EmoteDetails {
    return EmoteDetails(
        id = "",
        name = "",
        createdAt = 0L,
        listed = false,
        personalUse = false,
        animated = false,
        trending = null,
        lifecycle = 0,
        host = EmoteDetailsHost(
            url = "",
            defaultFileName = "",
            files = emptyList()
        ),
        owner = EmoteDetailsOwner(
            id = "",
            username = "",
            displayName = "",
            avatarUrl = "",
        ),
        tags = emptyList()
    )
}
