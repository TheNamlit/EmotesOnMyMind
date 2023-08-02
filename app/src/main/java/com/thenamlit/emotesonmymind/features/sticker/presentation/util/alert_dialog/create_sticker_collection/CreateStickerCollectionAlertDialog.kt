package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection

import android.util.Log
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}CreateStickerCollectionAlertDialog"

@Composable
fun CreateStickerCollectionAlertDialog(
    animated: Boolean = false,
    onCloseDialog: () -> Unit,
    viewModel: CreateStickerCollectionAlertDialogViewModel = hiltViewModel(),
) {
    Log.d(
        tag, "CreateStickerCollectionAlertDialog | animated: $animated, " +
                "viewModel: $viewModel"
    )

    val stickerDetailsState by viewModel.createStickerCollectionAlertDialogStateFlow.collectAsState()

    // Initially set animated value
    // This will be true if CreateStickerCollectionAlertDialog is called from an animated Sticker
    viewModel.setAlertDialogCreateCollectionAnimatedValue(animated = animated)

    CreateStickerCollectionAlertDialogContent(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        createCollectionNameValue = stickerDetailsState.alertDialogCreateCollectionNameValue,
        onCreateCollectionNameValueChanged = { newValue: String ->
            Log.d(tag, "onCreateCollectionNameValueChanged | newValue: $newValue")

            viewModel.setAlertDialogCreateCollectionNameValue(newValue = newValue)
        },
        createCollectionAnimatedValue = stickerDetailsState.alertDialogCreateCollectionAnimatedValue,
        onCreateCollectionAnimatedValueChanged = { newAnimatedValue: Boolean ->
            Log.d(
                tag,
                "onCreateCollectionAnimatedValueChanged | newAnimatedValue: $newAnimatedValue"
            )

            viewModel.setAlertDialogCreateCollectionAnimatedValue(animated = newAnimatedValue)
        },
        onDismissRequest = {
            Log.d(tag, "onDismissRequest")

            viewModel.resetInputValues()
            onCloseDialog()
        },
        onCreateCollectionButtonClick = {
            Log.d(tag, "onCreateCollectionButtonClick")

            viewModel.createCollection()
            viewModel.resetInputValues()
            onCloseDialog()
        },
        onCancelCreateCollectionButtonClick = {
            Log.d(tag, "onCancelCreateCollectionButtonClick")

            viewModel.resetInputValues()
            onCloseDialog()
        }
    )
}
