package com.thenamlit.emotesonmymind.core.presentation.components

import android.util.Log
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}DefaultFloatingActionButton"

@Composable
fun DefaultFloatingActionButton(
    onFabClicked: () -> Unit,
    icon: ImageVector,
    iconContentDescription: String,
) {
    Log.d(
        tag,
        "DefaultFloatingActionButton | icon: $icon, " +
                "iconContentDescription: $iconContentDescription"
    )

    FloatingActionButton(
        onClick = {
            onFabClicked()
        },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
        )
    }
}
