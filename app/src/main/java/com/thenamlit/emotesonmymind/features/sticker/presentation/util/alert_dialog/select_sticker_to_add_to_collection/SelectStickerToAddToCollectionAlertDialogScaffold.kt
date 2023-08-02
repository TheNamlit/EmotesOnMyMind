package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}SelectStickerToAddToCollectionAlertDialogScaffold"

@Composable
fun SelectStickerToAddToCollectionAlertDialogScaffold(
    onCloseClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(tag, "SelectStickerToAddToCollectionAlertDialogScaffold")

    Scaffold(
        topBar = {
            SelectStickerToAddToCollectionAlertDialogTopAppBar(
                titleText = "Select Sticker",
                onCloseClicked = onCloseClicked,
                onSaveClicked = onSaveClicked
            )
        }
    ) { paddingValues: PaddingValues ->
        content(paddingValues)
    }
}
