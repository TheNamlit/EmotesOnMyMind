package com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}DefaultTopAppBarNavigationIcon"

@Composable
fun DefaultTopAppBarNavigationIcon(
    onClick: () -> Unit,
    icon: ImageVector,
    @StringRes iconContentDescription: Int,
) {
    Log.d(tag, "DefaultTopAppBarNavigationIcon")

    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = iconContentDescription),
        )
    }
}
