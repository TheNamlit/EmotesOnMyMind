package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.components.BottomNavigationBar
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}MainFeedScreen"


// TODO: Implement Filter to actually only show Animated/NotAnimated here -> Easier to build StickerCollections
//  This doesn't work automatically with the API, so I have to build my own function for this
@Destination
@Composable
fun MainFeedScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: MainFeedScreenViewModel = hiltViewModel(),
) {
    Log.d(
        tag,
        "MainFeedScreen | navigator: $navigator, " +
                "navController: $navController, " +
                "viewModel: $viewModel"
    )

    val mainFeedState by viewModel.mainFeedStateFlow.collectAsState()
    val imageLoader: ImageLoader = viewModel.getImageLoader()

    CollectMainFeedScreenEvents(
        mainFeedScreenEventFlow = viewModel.mainFeedScreenEventFlow,
        onNavigate = { direction: Direction ->
            navigator.navigate(direction = direction)
        }
    )

    MainFeedScreenScaffold(
        mainFeedState = mainFeedState,
        onFabClicked = {
            viewModel.setSearchActive(isActive = true)
            viewModel.requestSearchBarFocus()
        },
        navigationBar = {
            BottomNavigationBar(
                navController = navController,
                navigator = navigator,
            )
        },
    ) { paddingValues ->
        MainFeedScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            mainFeedState = mainFeedState,
            imageLoader = imageLoader,
            onSearchActiveChanged = { isActive -> viewModel.setSearchActive(isActive = isActive) },
            onSearchQueryChange = { query: String -> viewModel.setQuery(query = query) },
            onSearch = {
                viewModel.setSearchActive(isActive = false)
                viewModel.searchEmotes()
            },
            onSearchBarCloseIconClicked = {
                if (mainFeedState.query.isNotEmpty()) {
                    viewModel.setQuery(query = "")
                } else {
                    viewModel.setSearchActive(isActive = false)
                }
            },
            onEmoteClicked = { mainFeedEmote: MainFeedEmote ->
                viewModel.navigateToDetails(
                    mainFeedEmote = mainFeedEmote
                )
            },
            checkForLoadNextEmotes = { index: Int ->
                // https://www.youtube.com/watch?v=D6Eus3f6U9I
                // Checking if at last Emote - 1 to load next ones
                if (index >= mainFeedState.displayedEmotes.size - 1 &&
                    !mainFeedState.endReached &&
                    !mainFeedState.isLoading
                ) {
                    viewModel.loadNextEmotes()
                }
            },
            isLoading = mainFeedState.isLoading,
            filterChipItems = mainFeedState.filterChipItems
        )
    }
}

@Composable
private fun CollectMainFeedScreenEvents(
    mainFeedScreenEventFlow: SharedFlow<UiEvent>,
    onNavigate: (Direction) -> Unit,
) {
    Log.d(tag, "CollectMainFeedScreenEvents | mainFeedScreenEventFlow: $mainFeedScreenEventFlow")

    LaunchedEffect(key1 = true) {
        mainFeedScreenEventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    Log.d(tag, "CollectMainFeedScreenEvents | Navigate")

                    onNavigate(event.destination)
                }

                else -> {
                    Log.d(tag, "CollectMainFeedScreenEvents | Other Event")
                }
            }
        }
    }
}
