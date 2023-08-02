package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection.AddStickerToCollectionAlertDialog
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection.CreateStickerCollectionAlertDialog
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.delete_sticker.DeleteStickerAlertDialog
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}StickerDetailsScreen"

@Destination
@Composable
fun StickerDetailsScreen(
    navigator: DestinationsNavigator,
    resultNavigator: ResultBackNavigator<Boolean>,
    navController: NavController,
    viewModel: StickerDetailsScreenViewModel = hiltViewModel(),
    sticker: Sticker,
) {
    Log.d(tag, "StickerDetailsScreen | sticker: $sticker, viewModel: $viewModel")

    val stickerDetailsState by viewModel.stickerDetailsStateFlow.collectAsState()

    // TODO: Give the User the option to select 1-3 Emojis that are connected to the Sticker
    //  This is used in WhatsApp, so that when they click the Emoji on their Keyboard, the Sticker
    //  will be used as a recommendation to post instead
    //  Requires at least one for every Sticker -> Set a default for this (which the User can modify)
    //  https://github.com/WhatsApp/stickers/tree/main/Android#modifying-the-contentsjson-file

    CollectStickerDetailsScreenEvents(
        stickerDetailsScreenEventFlow = viewModel.stickerDetailsScreenEventFlow,
        onNavigate = { direction: Direction ->
            Log.d(tag, "onNavigate-direction: $direction")

            navigator.navigate(direction = direction)
        },
        onNavigateUp = {
            navigator.navigateUp()
        }
    )

    StickerDetailsScreenScaffold(
        onNavigationIconClicked = {
            // https://composedestinations.rafaelcosta.xyz/navigation/backresult
            resultNavigator.navigateBack(result = true)
        }
    ) { paddingValues: PaddingValues ->
        StickerDetailsScreenContent(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
            sticker = stickerDetailsState.sticker,
            imageLoader = viewModel.getImageLoader(),
            addToCollectionButtonOnClick = {
                viewModel.showAddStickerToCollectionAlertDialog()
            },
            deleteStickerButtonOnClick = {
                viewModel.showDeleteStickerAlertDialog()
            },
            stickerImageFile = { path: String ->
                viewModel.getLocalStickerImageFile(path = path)
            }
        )
    }

    if (stickerDetailsState.showAddStickerToCollectionAlertDialog) {
        AddStickerToCollectionAlertDialog(
            sticker = sticker,
            onCloseDialog = {
                viewModel.hideAddStickerToCollectionAlertDialog()
            },
            onCreateCollectionClicked = {
                viewModel.showCreateCollectionAlertDialog()
            }
        )

        if (stickerDetailsState.showCreateCollectionAlertDialog) {
            CreateStickerCollectionAlertDialog(
                animated = stickerDetailsState.sticker.stickerImageData.animated,
                onCloseDialog = {
                    viewModel.hideCreateCollectionAlertDialog()
                }
            )
        }
    }

    if (stickerDetailsState.showDeleteStickerAlertDialog) {
        DeleteStickerAlertDialog(
            onCloseDialog = {
                viewModel.hideDeleteStickerAlertDialog()
            },
            onDeleteClick = {
                viewModel.hideDeleteStickerAlertDialog()
                viewModel.deleteSticker()
            }
        )
    }
}

@Composable
private fun CollectStickerDetailsScreenEvents(
    stickerDetailsScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Log.d(
        tag,
        "CollectStickerDetailsScreenEvents | " +
                "stickerDetailsScreenEventFlow: $stickerDetailsScreenEventFlow"
    )

    LaunchedEffect(key1 = true) {
        stickerDetailsScreenEventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectStickerDetailsScreenEvents | Navigate")

                    onNavigate(event.destination)
                }

                is UiEvent.NavigateUp -> {
                    Log.d(tag, "CollectStickerDetailsScreenEvents | NavigateUp")

                    onNavigateUp()
                }

                else -> {
                    Log.d(tag, "CollectStickerDetailsScreenEvents | Other Event")
                }
            }
        }
    }
}
