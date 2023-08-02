package com.thenamlit.emotesonmymind.features.sticker.presentation.library

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultFloatingActionButton
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBar
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}LibraryScreenScaffold"

@Composable
fun LibraryScreenScaffold(
    modifier: Modifier = Modifier,
    navigationBar: @Composable () -> Unit,
    showFloatingActionButton: Boolean,
    onFabClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(
        tag,
        "LibraryScreenScaffold | modifier: $modifier, " +
                "showFloatingActionButton: $showFloatingActionButton"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            DefaultTopAppBar(titleText = "Library")
        },
        floatingActionButton = {
            if (showFloatingActionButton) {
                LibraryScreenFloatingActionButton(onFabClicked = onFabClicked)
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = { navigationBar() }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun LibraryScreenFloatingActionButton(
    onFabClicked: () -> Unit,
) {
    Log.d(tag, "LibraryScreenFloatingActionButton")

    DefaultFloatingActionButton(
        onFabClicked = onFabClicked,
        icon = Icons.Filled.Add,
        iconContentDescription = stringResource(
            id = R.string.sticker_collections_screen_fab_icon_content_description
        )
    )
}
