package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.domain.models.StickerImageData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataHostFile
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataOwner


data class AddStickerToCollectionAlertDialogState(
    val sticker: Sticker = getDefaultSticker(),

    val selectedStickerCollectionStates: List<StickerCollectionSelectedState> = emptyList(),
    val notSelectedStickerCollectionStates: List<StickerCollectionSelectedState> = emptyList(),

    val stickerCollectionSelectedStates: List<StickerCollectionSelectedState> = emptyList(),

    val selectAllCollectionsButtonState: SelectAllCollectionsButtonState =
        SelectAllCollectionsButtonState.NoCollectionsAvailable,
)

data class StickerCollectionSelectedState(
    val stickerCollection: StickerCollection,
    val initiallySelected: Boolean = false,
    val currentlySelected: Boolean = false,
)

sealed class SelectAllCollectionsButtonState {
    class AddAll(val buttonText: String = "Add All") : SelectAllCollectionsButtonState()
    class RemoveAll(val buttonText: String = "Remove All") : SelectAllCollectionsButtonState()
    object NoCollectionsAvailable : SelectAllCollectionsButtonState()
}

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
