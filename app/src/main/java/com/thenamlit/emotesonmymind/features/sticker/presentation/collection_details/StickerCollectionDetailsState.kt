package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.UiText


data class StickerCollectionDetailsState(
    val collection: StickerCollection = getDefaultStickerCollection(),
    val collectionId: String = "",

    val showDeleteStickerCollectionAlertDialog: Boolean = false,
    val showSelectStickerToAddToCollectionAlertDialog: Boolean = false,

    val showCanNotAddToWhatsAppInfoAlertDialog: Boolean = false,
    val canNotAddToWhatsAppInfoAlertErrors: List<UiText> = emptyList(),

    val mode: StickerCollectionDetailsMode = StickerCollectionDetailsMode.Normal,

    val editModeCollectionName: String = "",

    val selectedStickerIdsInDeleteMode: List<String> = emptyList(),
)

sealed class StickerCollectionDetailsMode {
    object Normal : StickerCollectionDetailsMode()
    object Edit : StickerCollectionDetailsMode()
    object DeleteSticker : StickerCollectionDetailsMode()
}

private fun getDefaultStickerCollection(): StickerCollection {
    return StickerCollection(
        name = "",
        animated = false,
        stickers = emptyList()
    )
}
