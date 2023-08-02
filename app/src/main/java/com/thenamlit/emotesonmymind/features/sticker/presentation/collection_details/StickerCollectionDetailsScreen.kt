package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.destinations.StickerDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.can_not_add_to_whats_app_info.CanNotAddToWhatsAppInfoAlertDialog
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.delete_collection.DeleteStickerCollectionAlertDialog
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection.SelectStickerToAddToCollectionAlertDialog
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}StickerCollectionDetailsScreen"

@Destination
@Composable
fun StickerCollectionDetailsScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<StickerDetailsScreenDestination, Boolean>,
    navController: NavController,
    viewModel: StickerCollectionDetailsScreenViewModel = hiltViewModel(),
    stickerCollectionId: String,
) {
    Log.d(
        tag, "StickerCollectionDetailsScreen | viewModel: $viewModel, " +
                "stickerCollectionId: $stickerCollectionId"
    )

    val stickerCollectionDetailsState by viewModel.stickerCollectionDetailsStateFlow.collectAsState()

    // TODO: Display StickerCollection-Icon (Not yet implemented -> User can choose this one)
    //  Must be PNG afaik

    // This is used to reload the Stickers after navigating back from the StickerDetailsScreen
    // Otherwise the Sticker would still be visible as part of the collection when removed
    // via on the StickerDetailsScreen -> AddStickerToCollectionAlertDialog
    // https://composedestinations.rafaelcosta.xyz/navigation/backresult
    resultRecipient.onNavResult { navResult: NavResult<Boolean> ->
        when (navResult) {
            is NavResult.Value -> {
                Log.d(tag, "StickerCollectionDetailsScreen | NavResult: ${navResult.value}")

                if (navResult.value) {
                    viewModel.getStickerCollection()
                }
            }

            is NavResult.Canceled -> {
                Log.d(tag, "StickerCollectionDetailsScreen | NavResult canceled")
            }
        }
    }

    BackHandlerSettings(
        mode = stickerCollectionDetailsState.mode,
        cancelEditMode = { viewModel.cancelEditMode() },
        cancelDeleteStickerMode = { viewModel.cancelDeleteStickerMode() }
    )

    CollectStickerCollectionDetailsScreenEvents(
        stickerCollectionDetailsScreenEventFlow = viewModel.stickerCollectionDetailsScreenEventFlow,
        onNavigate = { direction: Direction ->
            navigator.navigate(direction = direction)
        },
        onNavigateUp = {
            navigator.navigateUp()
        }
    )

    // Have to wait for a response from WhatsApp - otherwise we just get an Error returned
    // https://stackoverflow.com/a/67177845
    val rememberLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(tag, "ResultCode: ${it.resultCode}")
    }

    StickerCollectionDetailsScreenScaffold(
        onNavigationIconClicked = { navigator.navigateUp() },
        onBottomAppBarAddButtonClicked = { viewModel.showSelectStickerToAddToCollectionAlertDialog() },
        onBottomAppBarRemoveButtonClicked = { viewModel.enterDeleteStickerMode() },
        onBottomAppBarEditButtonClicked = { viewModel.enterEditMode() },
        mode = stickerCollectionDetailsState.mode,
        collectionName = stickerCollectionDetailsState.collection.name,
        stickerAmount = stickerCollectionDetailsState.collection.stickers.size,
        editModeCollectionName = stickerCollectionDetailsState.editModeCollectionName,
        onCollectionNameChanged = { newCollectionName: String ->
            viewModel.setEditModeCollectionName(name = newCollectionName)
        },
        onEditModeSaveButtonClicked = {
            viewModel.saveEditMode()
        },
        onEditModeDeleteButtonClicked = {
            viewModel.showDeleteStickerCollectionAlertDialog()
        },
        onEditModeCancelButtonClicked = {
            viewModel.cancelEditMode()
        },
        onAddToWhatsAppButtonClicked = {
            viewModel.tryToAddToWhatsApp(rememberLauncher = rememberLauncher)
        },
        onDeleteStickerModeSaveButtonClicked = {
            viewModel.saveDeleteStickerMode()
        },
        onDeleteStickerModeCancelButtonClicked = {
            viewModel.cancelDeleteStickerMode()
        }
    ) { paddingValues: PaddingValues ->
        StickerCollectionDetailsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            itemModifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            imageLoader = viewModel.getImageLoader(),
            mode = stickerCollectionDetailsState.mode,
            selectedStickerIdsInDeleteMode = stickerCollectionDetailsState.selectedStickerIdsInDeleteMode,
            stickers = stickerCollectionDetailsState.collection.stickers,
            onStickerClicked = { sticker: Sticker ->
                viewModel.onStickerClicked(sticker = sticker)
            },
            stickerImageFile = { path: String ->
                viewModel.getLocalStickerImageFile(path = path)
            }
        )
    }

    if (stickerCollectionDetailsState.showDeleteStickerCollectionAlertDialog) {
        DeleteStickerCollectionAlertDialog(
            onCloseDialog = {
                viewModel.hideDeleteStickerCollectionAlertDialog()
            },
            onDeleteClick = {
                viewModel.hideDeleteStickerCollectionAlertDialog()
                viewModel.deleteStickerCollection()
            }
        )
    }

    if (stickerCollectionDetailsState.showCanNotAddToWhatsAppInfoAlertDialog) {
        CanNotAddToWhatsAppInfoAlertDialog(
            onCloseDialog = {
                viewModel.hideCanNotAddToWhatsAppInfoAlertDialog()
                viewModel.resetCanNotAddToWhatsAppInfoAlertErrors()
            },
            onConfirmClick = {
                viewModel.hideCanNotAddToWhatsAppInfoAlertDialog()
                viewModel.resetCanNotAddToWhatsAppInfoAlertErrors()
            },
            errors = stickerCollectionDetailsState.canNotAddToWhatsAppInfoAlertErrors
        )
    }

    if (stickerCollectionDetailsState.showSelectStickerToAddToCollectionAlertDialog) {
        SelectStickerToAddToCollectionAlertDialog(
            stickerCollection = stickerCollectionDetailsState.collection,
            onCloseDialog = {
                viewModel.hideSelectStickerToAddToCollectionAlertDialog()
            },
            onSave = {
                viewModel.hideSelectStickerToAddToCollectionAlertDialog()
                viewModel.getStickerCollection()
            }
        )
    }
}

@Composable
private fun BackHandlerSettings(
    mode: StickerCollectionDetailsMode,
    cancelEditMode: () -> Unit,
    cancelDeleteStickerMode: () -> Unit,
) {
    Log.d(tag, "BackHandlerSettings | mode: $mode")

    when (mode) {
        is StickerCollectionDetailsMode.Normal -> {}
        is StickerCollectionDetailsMode.Edit -> {
            BackHandler(enabled = true) {
                cancelEditMode()
            }
        }

        is StickerCollectionDetailsMode.DeleteSticker -> {
            BackHandler(enabled = true) {
                cancelDeleteStickerMode()
            }
        }
    }
}

@Composable
private fun CollectStickerCollectionDetailsScreenEvents(
    stickerCollectionDetailsScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Log.d(tag, "CollectStickerCollectionDetailsScreenEvents")

    LaunchedEffect(key1 = true) {
        stickerCollectionDetailsScreenEventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectStickerCollectionDetailsScreenEvents | Navigate")

                    onNavigate(event.destination)
                }

                is UiEvent.NavigateUp -> {
                    Log.d(tag, "CollectStickerCollectionDetailsScreenEvents | NavigateUp")

                    onNavigateUp()
                }

                else -> {
                    Log.d(tag, "CollectStickerCollectionDetailsScreenEvents | Other Event")
                }
            }
        }
    }
}
