package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.components.DefaultCoilImage
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


private const val tag = "${Logging.loggingPrefix}EmoteDetailsScreen"

@Destination
@Composable
fun EmoteDetailsScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: EmoteDetailsScreenViewModel = hiltViewModel(),
    emoteId: String,
) {
    Log.d(
        tag,
        "EmoteDetailsScreen | navigator: $navigator, " +
                "navController: $navController, " +
                "viewModel: $viewModel, " +
                "emoteId: $emoteId"
    )

    val emoteDetailsState by viewModel.emoteDetailsStateFlow.collectAsState()

    // TODO: Implement different versions with GraphQl:
    //  High res (and most recent): https://7tv.app/emotes/6309e73ffe72a7a37ff476f5
    //  Older/original version: https://7tv.app/emotes/62d95f5bfd736ba230c05b57

    CollectEmoteDetailsScreenEvents(
        emoteDetailsScreenEventFlow = viewModel.emoteDetailsScreenEventFlow,
        onNavigate = { direction: Direction ->
            Log.d(tag, "onNavigate-direction: $direction")

            navigator.navigate(direction = direction)
        },
    )

    ObserveWorkerState(
        workerState = emoteDetailsState.workerState,
        checkIfEmoteIsAlreadySavedAsSticker = {
            viewModel.isEmoteAlreadySavedAsSticker(emoteId = emoteId)
        }
    )

    EmoteDetailsScreenScaffold(
        emoteDetails = emoteDetailsState.emoteDetails,
        imageLoader = viewModel.getImageLoader(),
        onNavigationIconClicked = { navigator.navigateUp() },
        onTopAppBarUserProfileIconClicked = {
            viewModel.setShowEmoteUserAlertDialog(showAlertDialog = true)
        },
    ) { paddingValues: PaddingValues ->
        EmoteDetailsScreenContent(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
            emoteDetails = emoteDetailsState.emoteDetails,
            imageLoader = viewModel.getImageLoader(),
            emoteToStickerButtonState = emoteDetailsState.emoteToStickerButtonState,
            onDownloadButtonClick = viewModel::saveEmoteAsSticker,
            onNavigateToStickerDetailsButtonClick = { viewModel.navigateToStickerDetailsScreen() }
        )

        if (emoteDetailsState.showEmoteUserAlertDialog) {
            EmoteUserDialog(
                emoteDetails = emoteDetailsState.emoteDetails,
                imageLoader = viewModel.getImageLoader(),
                onEmoteUserAlertDialogDismissed = {
                    viewModel.setShowEmoteUserAlertDialog(showAlertDialog = false)
                },
                onEmoteUserAlertDialogConfirmButtonClicked = {
                    viewModel.setShowEmoteUserAlertDialog(showAlertDialog = false)

                    // TODO: EmoteUserProfile and not own Profile
                    viewModel.navigateToEmoteUserProfile()
                },
                onEmoteUserAlertDialogDismissButtonClicked = {
                    viewModel.setShowEmoteUserAlertDialog(showAlertDialog = false)
                }
            )
        }
    }
}

@Composable
private fun EmoteUserDialog(
    emoteDetails: EmoteDetails,
    imageLoader: ImageLoader,
    onEmoteUserAlertDialogDismissed: () -> Unit,
    onEmoteUserAlertDialogConfirmButtonClicked: () -> Unit,
    onEmoteUserAlertDialogDismissButtonClicked: () -> Unit,
) {
    Log.d(tag, "EmoteUserDialog | emoteDetails: $emoteDetails, imageLoader: $imageLoader")

    AlertDialog(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        onDismissRequest = { onEmoteUserAlertDialogDismissed() },
        icon = {
            DefaultCoilImage(
                url = emoteDetails.owner.avatarUrl,
                imageLoader = imageLoader
            )
        },
        title = {
            Text(text = emoteDetails.owner.username)
        },
        text = {
            // https://developer.android.com/reference/kotlin/java/time/format/DateTimeFormatter
            // https://stackoverflow.com/a/41430483
            val dateTime = LocalDateTime.ofEpochSecond(
                emoteDetails.createdAt,
                0,
                OffsetDateTime.now().offset
            )
            val dateTimeFormatter = if (DateFormat.is24HourFormat(LocalContext.current)) {
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")
            } else {
                DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a")
            }

            Text(text = "Emote created: ${dateTime.format(dateTimeFormatter)}")
            // TODO: Add Link to 7TV-Website? With Intent-Integration
        },
        confirmButton = {
            Button(onClick = { onEmoteUserAlertDialogConfirmButtonClicked() }) {
                Text(text = "Profile")
            }
        },
        dismissButton = {
            Button(onClick = { onEmoteUserAlertDialogDismissButtonClicked() }) {
                Text(text = "Cheers!")
            }
        }
    )
}

@Composable
private fun CollectEmoteDetailsScreenEvents(
    emoteDetailsScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
) {
    Log.d(
        tag,
        "CollectEmoteDetailsScreenEvents | emoteDetailsScreenEventFlow: $emoteDetailsScreenEventFlow"
    )

    LaunchedEffect(key1 = true) {
        emoteDetailsScreenEventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectEmoteDetailsScreenEvents | Navigate")

                    onNavigate(event.destination)
                }

                else -> {
                    Log.d(tag, "CollectEmoteDetailsScreenEvents | Other Event")
                }
            }
        }
    }
}

@Composable
private fun ObserveWorkerState(
    workerState: WorkerState,
    checkIfEmoteIsAlreadySavedAsSticker: () -> Unit,
) {
    Log.d(tag, "ObserveWorkerState | workState: $workerState")

    when (workerState) {
        is WorkerState.Idle -> {
            Log.d(tag, "EmoteDetailsScreen | WorkerState.Idle")
        }

        is WorkerState.Processing -> {
            Log.d(tag, "EmoteDetailsScreen | WorkerState.Processing: ${workerState.workId}")
        }

        is WorkerState.Success -> {
            Log.d(tag, "EmoteDetailsScreen | WorkerState.Success")

            checkIfEmoteIsAlreadySavedAsSticker()
        }

        is WorkerState.Failed -> {
            Log.e(tag, "EmoteDetailsScreen | WorkerState.Failed: ${workerState.message}")
        }
    }
}
