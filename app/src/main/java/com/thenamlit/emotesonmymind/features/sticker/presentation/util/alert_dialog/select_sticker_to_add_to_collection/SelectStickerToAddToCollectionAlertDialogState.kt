package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection


data class SelectStickerToAddToCollectionAlertDialogState(
    val collection: StickerCollection = getDefaultStickerCollection(),

    val notSelectedStickerList: List<Sticker> = emptyList(),
    val selectedStickerList: List<Sticker> = emptyList(),
)

private fun getDefaultStickerCollection(): StickerCollection {
    return StickerCollection(
        name = "",
        animated = false,
        stickers = emptyList()
    )
}
