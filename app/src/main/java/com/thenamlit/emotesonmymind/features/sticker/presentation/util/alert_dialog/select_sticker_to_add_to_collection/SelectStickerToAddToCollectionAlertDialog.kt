package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Logging
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}SelectStickerToAddToCollectionAlertDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectStickerToAddToCollectionAlertDialog(
    modifier: Modifier = Modifier,
    dialogProperties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    viewModel: SelectStickerToAddToCollectionAlertDialogViewModel = hiltViewModel(),
    stickerCollection: StickerCollection,
    onCloseDialog: () -> Unit,
    onSave: () -> Unit,
) {
    Log.d(
        tag, "SelectStickerToAddToCollectionAlertDialog | " +
                "stickerCollection: $stickerCollection, " +
                "viewModel: $viewModel"
    )

    viewModel.setCollection(stickerCollection = stickerCollection)
    viewModel.initiateStickerLists()

    val state by viewModel.selectStickerToAddToCollectionAlertDialogStateFlow.collectAsState()

    CollectSelectStickerToAddToCollectionAlertDialogEvents(
        stickerCollectionDetailsScreenEventFlow = viewModel.selectStickerToAddToCollectionAlertDialogEventFlow,
        resetState = { viewModel.resetState() },
        onSave = onSave
    )

    AlertDialog(
        modifier = modifier,
        properties = dialogProperties,
        onDismissRequest = {
            viewModel.resetState()
            onCloseDialog()
        },
    ) {
        SelectStickerToAddToCollectionAlertDialogScaffold(
            onCloseClicked = {
                viewModel.resetState()
                onCloseDialog()
            },
            onSaveClicked = {
                viewModel.saveSelectedStickerListToCollection()
            }
        ) { paddingValues: PaddingValues ->
            SelectStickerToAddToCollectionAlertDialogContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues),
                imageLoader = viewModel.getImageLoader(),
                notSelectedStickerList = state.notSelectedStickerList,
                onNotSelectedStickerClicked = { sticker: Sticker ->
                    viewModel.addToSelectedStickerList(sticker = sticker)
                    viewModel.removeFromNotSelectedStickerList(sticker = sticker)
                },
                selectedStickerList = state.selectedStickerList,
                onSelectedStickerClicked = { sticker: Sticker ->
                    viewModel.removeFromSelectedStickerList(sticker = sticker)
                    viewModel.addToNotSelectedStickerList(sticker = sticker)
                },
                stickerImageFile = { path: String ->
                    viewModel.getLocalStickerImageFile(path = path)
                },
            )
        }
    }
}

@Composable
private fun CollectSelectStickerToAddToCollectionAlertDialogEvents(
    stickerCollectionDetailsScreenEventFlow: SharedFlow<SelectStickerToAddToCollectionAlertDialogEvent>,
    onSave: () -> Unit,
    resetState: () -> Unit,
) {
    Log.d(tag, "CollectSelectStickerToAddToCollectionAlertDialogEvents")

    LaunchedEffect(key1 = true) {
        stickerCollectionDetailsScreenEventFlow.collect { event ->
            when (event) {
                is SelectStickerToAddToCollectionAlertDialogEvent.Save -> {
                    Log.d(
                        tag, "CollectSelectStickerToAddToCollectionAlertDialogEvents | " +
                                "SelectStickerToAddToCollectionAlertDialogEvent.Save"
                    )

                    resetState()
                    onSave()
                }

                else -> {
                    Log.d(
                        tag,
                        "CollectSelectStickerToAddToCollectionAlertDialogEvents | Other Event"
                    )
                }
            }
        }
    }
}
