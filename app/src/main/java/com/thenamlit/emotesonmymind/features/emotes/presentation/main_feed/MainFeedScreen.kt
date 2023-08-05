package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import android.util.Log
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
import com.thenamlit.emotesonmymind.core.presentation.components.BottomNavigationBar
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import kotlinx.coroutines.flow.SharedFlow


private const val tag = "${Logging.loggingPrefix}MainFeedScreen"

@Destination
@Composable
fun MainFeedScreen(
    navigator: DestinationsNavigator,
    navController: NavController,
    viewModel: MainFeedScreenViewModel = hiltViewModel(),
) {
    Log.d(tag, "MainFeedScreen | viewModel: $viewModel")

    val mainFeedState by viewModel.mainFeedStateFlow.collectAsState()

    val context = LocalContext.current
    CollectMainFeedScreenEvents(
        mainFeedScreenEventFlow = viewModel.mainFeedScreenEventFlow,
        onNavigate = { direction: Direction ->
            navigator.navigate(direction = direction)
        },
        onSingleError = { _: UiText?, text: UiText ->
            viewModel.showSnackbar(text = text.asString(context = context))
        },
    )

    MainFeedScreenScaffold(
        snackbarHostState = mainFeedState.snackbarHostState,
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
            searchBarFocusRequester = mainFeedState.searchBarFocusRequester,
            imageLoader = viewModel.getImageLoader(),
            query = mainFeedState.query,
            isLoading = mainFeedState.isLoading,
            filterChipItems = mainFeedState.filterChipItems,
            searchBarHeight = mainFeedState.searchBarHeight,
            searchActive = mainFeedState.searchActive,
            searchHistory = mainFeedState.searchHistory,
            displayedEmotes = mainFeedState.displayedEmotes,
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
        )
    }
}

@Composable
private fun CollectMainFeedScreenEvents(
    mainFeedScreenEventFlow: SharedFlow<MainFeedScreenEvent>,
    onNavigate: (Direction) -> Unit,
    onSingleError: (title: UiText?, text: UiText) -> Unit,
) {
    Log.d(tag, "CollectMainFeedScreenEvents | mainFeedScreenEventFlow: $mainFeedScreenEventFlow")

    LaunchedEffect(key1 = true) {
        mainFeedScreenEventFlow.collect { event ->
            event.handleEvents(
                event = event,
                onNavigate = onNavigate,
                onSingleError = onSingleError
            )
        }
    }
}
