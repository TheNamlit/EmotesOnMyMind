package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBarNavigationIcon
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}AddStickerToCollectionAlertDialogTopAppBar"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStickerToCollectionAlertDialogTopAppBar(
    titleText: String,
    onCloseClicked: () -> Unit,
    onSaveClicked: () -> Unit,
) {
    Log.d(tag, "AddStickerToCollectionAlertDialogTopAppBar | titleText: $titleText")

    TopAppBar(
        title = {
            Text(text = titleText)
        },
        navigationIcon = {
            DefaultTopAppBarNavigationIcon(
                onClick = { onCloseClicked() },
                icon = Icons.Default.Close,
                iconContentDescription = R.string.add_sticker_to_collection_alert_dialog_top_app_bar_navigation_icon_content_description
            )
        },
        actions = {
            IconButton(onClick = { onSaveClicked() }) {
                Icon(Icons.Default.Save, contentDescription = "Save Icon")
            }
        }
    )
}
