package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.can_not_add_to_whats_app_info

import android.util.Log
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.UiText


private const val tag = "${Logging.loggingPrefix}CanNotAddToWhatsAppInfoAlertDialog"

@Composable
fun CanNotAddToWhatsAppInfoAlertDialog(
    onCloseDialog: () -> Unit,
    onConfirmClick: () -> Unit,
    errors: List<UiText>,
) {
    Log.d(tag, "CanNotAddToWhatsAppInfoAlertDialog | errors: $errors")

    CanNotAddToWhatsAppInfoAlertDialogContent(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        onDismissRequest = {
            onCloseDialog()
        },
        onConfirmClick = {
            onConfirmClick()
        },
        errors = errors
    )
}
