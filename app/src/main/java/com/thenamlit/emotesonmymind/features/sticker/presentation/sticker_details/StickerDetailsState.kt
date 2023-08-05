package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import androidx.compose.material3.SnackbarHostState
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerImageData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataHostFile
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataOwner


data class StickerDetailsState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),

    val sticker: Sticker = getDefaultSticker(),

    val showAddStickerToCollectionAlertDialog: Boolean = false,
    val showCreateCollectionAlertDialog: Boolean = false,
    val showDeleteStickerAlertDialog: Boolean = false,
)

private fun getDefaultSticker(): Sticker {
    return Sticker(
        name = "",
        createdAt = 0L,
        lastModified = 0L,
        stickerImageData = StickerImageData(
            width = 0,
            height = 0,
            size = 0,
            frameCount = 0,
            format = "WEBP",
            animated = false
        ),
        remoteEmoteData = StickerRemoteEmoteData(
            id = "",
            url = "",
            createdAt = 0L,
            owner = StickerRemoteEmoteDataOwner(
                id = "",
                username = "",
                avatarUrl = ""
            ),
            hostFile = StickerRemoteEmoteDataHostFile(
                width = 0,
                height = 0,
                size = 0,
                frameCount = 0,
                format = "WEBP",
                animated = false
            )
        )
    )
}
