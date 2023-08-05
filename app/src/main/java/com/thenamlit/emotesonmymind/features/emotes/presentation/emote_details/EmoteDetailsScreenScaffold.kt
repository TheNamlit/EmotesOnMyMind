package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultCoilImage
import com.thenamlit.emotesonmymind.core.presentation.components.top_app_bar.DefaultTopAppBarNavigationIcon
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails


private const val tag = "${Logging.loggingPrefix}EmoteDetailsScreenScaffold"

@Composable
fun EmoteDetailsScreenScaffold(
    modifier: Modifier = Modifier,
    emoteDetails: EmoteDetails,
    imageLoader: ImageLoader,
    snackbarHostState: SnackbarHostState,
    onNavigationIconClicked: () -> Unit,
    onTopAppBarUserProfileIconClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Log.d(
        tag, "EmoteDetailsScreenScaffold | modifier: $modifier, " +
                "emoteDetails: $emoteDetails, " +
                "imageLoader: $imageLoader"
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            EmoteDetailsScreenTopAppBar(
                emoteDetails = emoteDetails,
                imageLoader = imageLoader,
                onNavigationIconClicked = onNavigationIconClicked,
                onTopAppBarUserProfileIconClicked = onTopAppBarUserProfileIconClicked
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmoteDetailsScreenTopAppBar(
    modifier: Modifier = Modifier,
    emoteDetails: EmoteDetails,
    imageLoader: ImageLoader,
    onNavigationIconClicked: () -> Unit,
    onTopAppBarUserProfileIconClicked: () -> Unit,
) {
    Log.d(
        tag,
        "EmoteDetailsScreenTopAppBar | modifier: $modifier, " +
                "emoteDetails: $emoteDetails, " +
                "imageLoader: $imageLoader"
    )

    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = "Details")
        },
        navigationIcon = {
            DefaultTopAppBarNavigationIcon(
                onClick = { onNavigationIconClicked() },
                icon = Icons.Default.ArrowBack,
                iconContentDescription = R.string.emote_details_screen_top_app_bar_navigation_icon_content_description
            )
        },
        actions = {
            IconButton(
                onClick = {
                    Log.d(
                        tag,
                        "EmoteDetailsScreenTopAppBar | Clicked on UserProfilePicture-Icon"
                    )

                    // https://m3.material.io/components/dialogs/guidelines
                    // https://github.com/material-components/material-components-android/blob/master/docs/components/Dialog.md
                    onTopAppBarUserProfileIconClicked()
                }
            ) {
                DefaultCoilImage(
                    url = emoteDetails.owner.avatarUrl,
                    imageLoader = imageLoader
                )
            }
        }
    )
}
