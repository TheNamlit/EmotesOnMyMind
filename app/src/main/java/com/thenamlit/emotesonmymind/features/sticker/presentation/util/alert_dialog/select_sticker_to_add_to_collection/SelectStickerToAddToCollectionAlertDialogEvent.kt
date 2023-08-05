package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent


sealed class SelectStickerToAddToCollectionAlertDialogEvent {
    class Navigate(val navigationEvent: NavigationEvent) :
        SelectStickerToAddToCollectionAlertDialogEvent()

    object Save : SelectStickerToAddToCollectionAlertDialogEvent()
}
