package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}CreateStickerCollectionAlertDialogContent"

@Composable
fun CreateStickerCollectionAlertDialogContent(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    createCollectionNameValue: String,
    onCreateCollectionNameValueChanged: (String) -> Unit,
    createCollectionAnimatedValue: Boolean,
    onCreateCollectionAnimatedValueChanged: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateCollectionButtonClick: () -> Unit,
    onCancelCreateCollectionButtonClick: () -> Unit,
) {
    Log.d(
        tag,
        "CreateStickerCollectionAlertDialogContent | modifier: $modifier, " +
                "createCollectionNameValue: $createCollectionNameValue, " +
                "createCollectionAnimatedValue: $createCollectionAnimatedValue"
    )

    AlertDialog(
        modifier = modifier,
        properties = dialogProperties,
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "Create Collection") },
        confirmButton = {
            Button(onClick = {
                onCreateCollectionButtonClick()
            }) {
                Text(text = "Create")
            }
        },
        dismissButton = {
            Button(onClick = {
                onCancelCreateCollectionButtonClick()
            }) {
                Text(text = "Cancel")
            }
        },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = createCollectionNameValue,
                        onValueChange = { newValue: String ->
                            onCreateCollectionNameValueChanged(newValue)
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(modifier = Modifier.padding(end = 25.dp), text = "Animated")

                    Switch(
                        checked = createCollectionAnimatedValue,
                        onCheckedChange = { animated: Boolean ->
                            onCreateCollectionAnimatedValueChanged(animated)
                        }
                    )
                }
            }
        }
    )
}
