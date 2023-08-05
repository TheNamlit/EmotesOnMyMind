package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultFloatingActionButton
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBar
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}MainFeedScreenScaffold"

@Composable
fun MainFeedScreenScaffold(
    modifier: Modifier = Modifier,
    mainFeedState: MainFeedState,
    snackbarHostState: SnackbarHostState,
    onFabClicked: () -> Unit,
    navigationBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(tag, "MainFeedScreenScaffold | modifier: $modifier, mainFeedState: $mainFeedState")

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { MainFeedScreenTopAppBar() },
        floatingActionButton = { MainFeedScreenFloatingActionButton(onFabClicked = onFabClicked) },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = { navigationBar() }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun MainFeedScreenTopAppBar() {
    Log.d(tag, "MainFeedScreenTopAppBar")

    DefaultTopAppBar(
        titleText = stringResource(id = R.string.main_feed_screen_top_app_bar_title_text)
    )
}

@Composable
private fun MainFeedScreenFloatingActionButton(
    onFabClicked: () -> Unit,
) {
    Log.d(tag, "MainFeedScreenFloatingActionButton")

    DefaultFloatingActionButton(
        onFabClicked = onFabClicked,
        icon = Icons.Filled.Search,
        iconContentDescription = stringResource(
            id = R.string.main_feed_screen_fab_icon_content_description
        )
    )
}
