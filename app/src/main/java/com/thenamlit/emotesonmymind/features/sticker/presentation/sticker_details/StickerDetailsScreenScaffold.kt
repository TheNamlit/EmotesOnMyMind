package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBar
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBarNavigationIcon
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}StickerDetailsScreenScaffold"

@Composable
fun StickerDetailsScreenScaffold(
    modifier: Modifier = Modifier,
    onNavigationIconClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(tag, "StickerDetailsScreenScaffold | modifier: $modifier")

    Scaffold(
        modifier = modifier,
        topBar = {
            DefaultTopAppBar(
                titleText = "Sticker Details",
                navigationIcon = {
                    DefaultTopAppBarNavigationIcon(
                        onClick = { onNavigationIconClicked() },
                        icon = Icons.Default.ArrowBack,
                        iconContentDescription = R.string.sticker_details_screen_top_app_bar_navigation_icon_content_description
                    )
                }
            )
        },
    ) { paddingValues ->
        content(paddingValues)
    }
}
