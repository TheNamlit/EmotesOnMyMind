package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.delete_collection

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.UiText


private const val tag = "${Logging.loggingPrefix}DeleteStickerCollectionAlertDialogContent"

@Composable
fun DeleteStickerCollectionAlertDialogContent(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    onDismissRequest: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Log.d(tag, "DeleteStickerCollectionAlertDialogContent | modifier: $modifier")

    AlertDialog(
        modifier = modifier,
        properties = dialogProperties,
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(
                text = UiText.StringResource(
                    id = R.string.delete_sticker_collection_alert_dialog_content_alert_dialog_title_text
                ).asString()
            )
        },
        confirmButton = {
            Button(onClick = { onDeleteClick() }) {
                Text(
                    text = UiText.StringResource(
                        id = R.string.delete_sticker_collection_alert_dialog_content_alert_dialog_confirm_button_text
                    ).asString()
                )
            }
        },
        dismissButton = {
            Button(onClick = { onCancelClick() }) {
                Text(
                    text = UiText.StringResource(
                        id = R.string.delete_sticker_collection_alert_dialog_content_alert_dialog_dismiss_button_text
                    ).asString()
                )
            }
        },
    )
}
