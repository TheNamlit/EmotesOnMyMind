package com.thenamlit.emotesonmymind.core.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.thenamlit.emotesonmymind.core.util.UiText


@Composable
fun DefaultMultipleErrorsAlertDialog(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    title: String,  // TODO: Use StringResources instead
    textList: List<String>,  // TODO: Use StringResources instead
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        properties = dialogProperties,
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(
                text = title
            )
        },
        text = {
            LazyColumn {
                items(textList) {
                    Text(modifier = Modifier.padding(5.dp), text = it)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismissRequest() }) {
                Text(
                    text = UiText.StringResource(
                        id = R.string.can_not_add_to_whats_app_info_alert_dialog_content_confirm_button_text
                    ).asString()
                )
            }
        }
    )
}
