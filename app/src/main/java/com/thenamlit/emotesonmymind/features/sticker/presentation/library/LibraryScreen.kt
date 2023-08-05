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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.components.BottomNavigationBar
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection.CreateStickerCollectionAlertDialog
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest


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

    val context = LocalContext.current

    CollectLibraryScreenEvents(
        libraryScreenEventFlow = viewModel.libraryScreenEventFlow,
        onNavigate = { direction: Direction -> navigator.navigate(direction = direction) },
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
                viewModel.navigateToStickerCollectionDetails(
                    stickerCollectionId = stickerCollectionId
                )
            },
            filterChipItems = state.filterChipItems,
            selectedTabIndex = state.selectedTabIndex,
            selectedTabCollections = state.selectedTabCollections,
            selectedTabStickers = state.selectedTabStickers,
            onCollectionsTabClicked = {
                viewModel.selectCollectionsTab()
            },
            onStickersTabClicked = {
                viewModel.selectStickersTab()
            },
            stickers = state.stickers,
            onStickerClicked = { sticker: Sticker ->
                viewModel.navigateToStickerDetailsScreenDestination(sticker = sticker)
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
            },
            animated = false
        )
    }
}

@Composable
private fun CollectLibraryScreenEvents(
    libraryScreenEventFlow: SharedFlow<LibraryScreenEvent>,
    onNavigate: (Direction) -> Unit,
) {
    Log.d(tag, "CollectLibraryScreenEvents")

    LaunchedEffect(key1 = true) {
        libraryScreenEventFlow.collectLatest { event ->
            Log.d(tag, "CollectLibraryScreenEvents | Event: $event")

            event.handleEvents(
                event = event,
                onNavigate = onNavigate
            )
        }
    }
}
