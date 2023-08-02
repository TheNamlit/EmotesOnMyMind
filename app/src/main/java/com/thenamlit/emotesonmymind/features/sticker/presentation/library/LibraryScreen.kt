package com.thenamlit.emotesonmymind.features.sticker.presentation.library

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
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.components.BottomNavigationBar
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.destinations.StickerDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection.CreateStickerCollectionAlertDialog
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}LibraryScreen"

@Destination
@Composable
fun LibraryScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: LibraryScreenViewModel = hiltViewModel(),
) {
    Log.d(tag, "LibraryScreen | viewModel: $viewModel")

    val state by viewModel.libraryScreenStateFlow.collectAsState()

    // TODO-BUG:
    //  1. Have Animated & NotAnimated off
    //  2. Create a collection
    //  -> Will show all AnimatedCollections

    // TODO: Display StickerCollection-Icon (Not yet implemented -> User can choose this one)
    //  Must be PNG afaik

    CollectLibraryScreenEvents(
        libraryScreenEventFlow = viewModel.libraryScreenEventFlow,
        onNavigate = { direction: Direction ->
            Log.d(tag, "onNavigate-direction: $direction")

            navigator.navigate(direction = direction)
        }
    )

    LibraryScreenScaffold(
        showFloatingActionButton = state.selectedTabCollections,
        onFabClicked = {
            viewModel.setShowCreateCollectionAlertDialog(showAlertDialog = true)
        },
        navigationBar = {
            BottomNavigationBar(
                navController = navController,
                navigator = navigator
            )
        }
    ) { paddingValues: PaddingValues ->
        LibraryScreenContent(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
            imageLoader = viewModel.getImageLoader(),
            stickerCollections = state.stickerCollections,
            onStickerCollectionRowClicked = { stickerCollectionId: String ->
                Log.d(
                    tag,
                    "onStickerCollectionRowClicked, stickerCollectionId: $stickerCollectionId"
                )

                viewModel.navigateToStickerCollectionDetails(stickerCollectionId = stickerCollectionId)
            },
            filterChipItems = state.filterChipItems,
            selectedTabIndex = state.selectedTabIndex,
            selectedTabCollections = state.selectedTabCollections,
            selectedTabStickers = state.selectedTabStickers,
            onCollectionsTabClicked = {
                Log.d(tag, "onCollectionsTabClicked")

                viewModel.selectCollectionsTab()
            },
            onStickersTabClicked = {
                Log.d(tag, "onStickersTabClicked")

                viewModel.selectStickersTab()
            },
            stickers = state.stickers,
            onStickerClicked = { sticker: Sticker ->
                Log.d(tag, "onStickerClicked | $sticker")

                navigator.navigate(StickerDetailsScreenDestination(sticker = sticker))
            },
            stickerImageFile = { path: String ->
                viewModel.getLocalStickerImageFile(path = path)
            },
            setIsSwipeToTheLeft = { isSwipeToTheLeft: Boolean ->
                viewModel.setIsSwipeToTheLeft(isSwipeToTheLeft = isSwipeToTheLeft)
            },
            updateTabIndexBasedOnSwipe = { viewModel.updateTabIndexBasedOnSwipe() }
        )
    }

    if (state.showCreateCollectionAlertDialog) {
        CreateStickerCollectionAlertDialog(
            onCloseDialog = {
                viewModel.setShowCreateCollectionAlertDialog(showAlertDialog = false)
            }
        )
    }
}

@Composable
private fun CollectLibraryScreenEvents(
    libraryScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
) {
    Log.d(tag, "CollectLibraryScreenEvents")

    LaunchedEffect(key1 = true) {
        libraryScreenEventFlow.collect { event ->
            Log.d(tag, "CollectLibraryScreenEvents | Event: $event")

            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectLibraryScreenEvents | Navigate")

                    onNavigate(event.destination)
                }

                else -> {
                    Log.d(tag, "CollectLibraryScreenEvents | Other Event")
                }
            }
        }
    }
}
