package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.can_not_add_to_whats_app_info

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.UiText


private const val tag = "${Logging.loggingPrefix}CanNotAddToWhatsAppInfoAlertDialogContent"

@Composable
fun CanNotAddToWhatsAppInfoAlertDialogContent(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    errors: List<UiText>,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Log.d(
        tag, "CanNotAddToWhatsAppInfoAlertDialogContent | modifier: $modifier, " +
                "errors: $errors"
    )

    AlertDialog(
        modifier = modifier,
        properties = dialogProperties,
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(
                text = UiText.StringResource(
                    id = R.string.can_not_add_to_whats_app_info_alert_dialog_content_title_text
                ).asString()
            )
        },
        text = {
            LazyColumn {
                items(errors) {
                    Text(modifier = Modifier.padding(5.dp), text = it.asString())
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmClick() }) {
                Text(
                    text = UiText.StringResource(
                        id = R.string.can_not_add_to_whats_app_info_alert_dialog_content_confirm_button_text
                    ).asString()
                )
            }
        }
    )
}
