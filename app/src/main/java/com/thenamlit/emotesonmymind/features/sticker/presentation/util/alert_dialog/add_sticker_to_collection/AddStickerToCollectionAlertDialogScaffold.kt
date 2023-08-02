package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}AddStickerToCollectionAlertDialogScaffold"

@Composable
fun AddStickerToCollectionAlertDialogScaffold(
    topAppBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(tag, "AddStickerToCollectionAlertDialogScaffold")

    Scaffold(
        topBar = {
            topAppBar()
        }
    ) { paddingValues: PaddingValues ->
        content(paddingValues)
    }
}
