package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.util.Logging


private const val tag = "${Logging.loggingPrefix}AddStickerToCollectionAlertDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStickerToCollectionAlertDialog(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    viewModel: AddStickerToCollectionAlertDialogViewModel = hiltViewModel(),
    sticker: Sticker,
    onCloseDialog: () -> Unit,
    onCreateCollectionClicked: () -> Unit,
) {
    Log.d(
        tag, "AddStickerToCollectionAlertDialog | modifier: $modifier, " +
                "sticker: $sticker, " +
                "viewModel: $viewModel"
    )

    viewModel.getInitialCollections()

    val state by viewModel.addStickerToCollectionAlertDialogStateFlow.collectAsState()

    AlertDialog(
        modifier = modifier,
        properties = dialogProperties,
        onDismissRequest = {
            viewModel.resetState()
            onCloseDialog()
        },
    ) {
        AddStickerToCollectionAlertDialogScaffold(
            topAppBar = {
                AddStickerToCollectionAlertDialogTopAppBar(
                    titleText = "Add to Collection",
                    onCloseClicked = {
                        Log.d(tag, "onCloseClicked")

                        viewModel.resetState()
                        onCloseDialog()
                    },
                    onSaveClicked = {
                        Log.d(tag, "onSaveClicked")

                        viewModel.saveSelectedStickerCollections()
                        onCloseDialog()
                    }
                )
            }
        ) { paddingValues: PaddingValues ->
            AddStickerToCollectionAlertDialogContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                stickerCollectionSelectedStates = state.stickerCollectionSelectedStates,
                onStickerCollectionRowClicked = { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                    Log.d(
                        tag,
                        "onStickerCollectionRowClicked | stickerCollectionSelectedState: $stickerCollectionSelectedState"
                    )

                    viewModel.setCollectionSelected(stickerCollectionSelectedState = stickerCollectionSelectedState)
                },
                selectAllCollectionsButtonState = state.selectAllCollectionsButtonState,
                addToAllCollections = {
                    Log.d(tag, "addToAllCollections")

                    viewModel.addToAllCollections()
                },
                removeFromAllCollections = {
                    Log.d(tag, "removeFromAllCollections")

                    viewModel.removeFromAllCollections()
                },
                onCreateCollectionClick = {
                    Log.d(tag, "onCreateCollectionClick")

                    onCreateCollectionClicked()
                },
            )
        }
    }
}
