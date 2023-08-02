package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.delete_collection

import android.util.Log
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}DeleteStickerCollectionAlertDialog"

@Composable
fun DeleteStickerCollectionAlertDialog(
    onCloseDialog: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Log.d(tag, "DeleteStickerCollectionAlertDialog")

    DeleteStickerCollectionAlertDialogContent(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        onDismissRequest = {
            onCloseDialog()
        },
        onDeleteClick = {
            onDeleteClick()
        },
        onCancelClick = {
            onCloseDialog()
        }
    )
}
