package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.pagination.PaginationImpl
import com.thenamlit.emotesonmymind.features.destinations.EmoteDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.destinations.StickerDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmote
import com.thenamlit.emotesonmymind.features.emotes.domain.models.MainFeedEmoteSearchHistoryItem
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed.AddMainFeedEmoteSearchHistoryItemUseCase
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed.CheckIfEmoteIsAlreadyDownloadedAsStickerUseCase
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed.GetMainFeedEmotesSearchHistoryUseCase
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.main_feed.GetMainFeedEmotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainFeedScreenViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
    private val getMainFeedEmotesUseCase: GetMainFeedEmotesUseCase,
    private val getMainFeedEmotesSearchHistoryUseCase: GetMainFeedEmotesSearchHistoryUseCase,
    private val addMainFeedEmoteSearchHistoryItemUseCase: AddMainFeedEmoteSearchHistoryItemUseCase,
    private val checkIfEmoteIsAlreadyDownloadedAsStickerUseCase: CheckIfEmoteIsAlreadyDownloadedAsStickerUseCase,
) : ViewModel() {
    private val tag = Logging.loggingPrefix + MainFeedScreenViewModel::class.java.simpleName

    private val _mainFeedStateFlow = MutableStateFlow(MainFeedState())
    val mainFeedStateFlow: StateFlow<MainFeedState> = _mainFeedStateFlow.asStateFlow()

    private val _mainFeedScreenEventFlow = MutableSharedFlow<UiEvent>()
    val mainFeedScreenEventFlow = _mainFeedScreenEventFlow.asSharedFlow()

    private val pagination = PaginationImpl(
        initialKey = _mainFeedStateFlow.value.page,
        onLoadUpdated = { isLoading: Boolean ->
            _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(isLoading = isLoading)
        },
        onRequest = { nextPage: Int ->
            getMainFeedEmotesUseCase(
                query = _mainFeedStateFlow.value.searchedQuery,
                page = nextPage,
                limit = _mainFeedStateFlow.value.limit,
                sort = _mainFeedStateFlow.value.sort,
                formats = _mainFeedStateFlow.value.formats,
                filter = _mainFeedStateFlow.value.filter
            )
        },
        getNextKey = {
            _mainFeedStateFlow.value.page + 1
        },
        onError = {
            _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(error = it.logging)
        },
        onSuccess = { items: List<MainFeedEmote>, newKey: Int ->
            _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(
                emotes = _mainFeedStateFlow.value.emotes + items,
                page = newKey,
                endReached = items.isEmpty()
            )

            filterAndAddToDisplayedEmotes(mainFeedEmotes = items)
        }
    )

    init {
        Log.d(tag, "init")

        initializeFilterChipItems()
        searchEmotes()
        loadNextEmotes()
    }

    fun getImageLoader(): ImageLoader {
        Log.d(tag, "getImageLoader")

        return imageLoader
    }

    private fun initializeFilterChipItems() {
        Log.d(tag, "initializeFilterChipItems")

        val filterChipItems = listOf(
            MainFeedScreenStateFilterChipItem(
                type = MainFeedScreenStateFilterChipItemType.Animated,
                selected = true,
                onClick = { filterChipItem: MainFeedScreenStateFilterChipItem ->
                    toggleMainFeedScreenStateFilterChipItemActive(filterChipItem = filterChipItem)
                }
            ),
            MainFeedScreenStateFilterChipItem(
                type = MainFeedScreenStateFilterChipItemType.NotAnimated,
                selected = true,
                onClick = { filterChipItem: MainFeedScreenStateFilterChipItem ->
                    toggleMainFeedScreenStateFilterChipItemActive(filterChipItem = filterChipItem)
                }
            ),
        )

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(
            filterChipItems = filterChipItems,
        )
    }

    private fun toggleMainFeedScreenStateFilterChipItemActive(
        filterChipItem: MainFeedScreenStateFilterChipItem,
    ) {
        Log.d(
            tag,
            "toggleLibraryScreenStateFilterChipItemActive | filterChipItem: $filterChipItem"
        )

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(
            filterChipItems = _mainFeedStateFlow.value.filterChipItems.map {
                if (it == filterChipItem) {
                    MainFeedScreenStateFilterChipItem(
                        type = it.type,
                        selected = !it.selected,
                        onClick = it.onClick
                    )
                } else {
                    it
                }
            },
        )

        filterMainFeedEmotes()
    }

    private fun filterMainFeedEmotes() {
        Log.d(tag, "filterMainFeedEmotes")

        val filterChipItems = _mainFeedStateFlow.value.filterChipItems
        var showAnimated = false
        var showNotAnimated = false

        filterChipItems.forEach { mainFeedScreenStateFilterChipItem: MainFeedScreenStateFilterChipItem ->
            when (mainFeedScreenStateFilterChipItem.type) {
                is MainFeedScreenStateFilterChipItemType.Animated -> {
                    if (mainFeedScreenStateFilterChipItem.selected) {
                        showAnimated = true
                    }
                }

                is MainFeedScreenStateFilterChipItemType.NotAnimated -> {
                    if (mainFeedScreenStateFilterChipItem.selected) {
                        showNotAnimated = true
                    }
                }
            }
        }

        setDisplayedEmotes(
            mainFeedEmotes = _mainFeedStateFlow.value.emotes.filter { mainFeedEmote: MainFeedEmote ->
                (mainFeedEmote.animated && showAnimated) ||
                        (!mainFeedEmote.animated && showNotAnimated)
            }
        )
    }

    private fun setDisplayedEmotes(mainFeedEmotes: List<MainFeedEmote>) {
        Log.d(tag, "setDisplayedEmotes | mainFeedEmotes: $mainFeedEmotes")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(
            displayedEmotes = mainFeedEmotes
        )
    }

    private fun filterAndAddToDisplayedEmotes(mainFeedEmotes: List<MainFeedEmote>) {
        Log.d(tag, "filterAndAddToDisplayedEmotes | mainFeedEmotes: $mainFeedEmotes")

        val filterChipItems = _mainFeedStateFlow.value.filterChipItems
        var showAnimated = false
        var showNotAnimated = false

        filterChipItems.forEach { mainFeedScreenStateFilterChipItem: MainFeedScreenStateFilterChipItem ->
            when (mainFeedScreenStateFilterChipItem.type) {
                is MainFeedScreenStateFilterChipItemType.Animated -> {
                    if (mainFeedScreenStateFilterChipItem.selected) {
                        showAnimated = true
                    }
                }

                is MainFeedScreenStateFilterChipItemType.NotAnimated -> {
                    if (mainFeedScreenStateFilterChipItem.selected) {
                        showNotAnimated = true
                    }
                }
            }
        }

        mainFeedEmotes.forEach { mainFeedEmote: MainFeedEmote ->
            if (
                (mainFeedEmote.animated && showAnimated) ||
                (!mainFeedEmote.animated && showNotAnimated)
            ) {
                _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(
                    displayedEmotes = _mainFeedStateFlow.value.displayedEmotes + mainFeedEmote
                )
            }
        }
    }

    fun navigateToDetails(mainFeedEmote: MainFeedEmote) {
        Log.d(tag, "navigateToDetails | mainFeedEmote: $mainFeedEmote")

        viewModelScope.launch(Dispatchers.IO) {

            val emoteAlreadyDownloadedAsStickerResult: Resource<Sticker> =
                checkIfEmoteIsAlreadyDownloadedAsStickerUseCase(remoteEmoteId = mainFeedEmote.id)

            when (emoteAlreadyDownloadedAsStickerResult) {
                is Resource.Success -> {
                    emoteAlreadyDownloadedAsStickerResult.data?.let { sticker: Sticker ->
                        Log.d(
                            tag,
                            "navigateToDetails | Found Sticker, will go to StickerDetails " +
                                    "instead of EmoteDetails"
                        )

                        _mainFeedScreenEventFlow.emit(
                            UiEvent.Navigate(
                                destination = StickerDetailsScreenDestination(sticker = sticker)
                            )
                        )
                    } ?: kotlin.run {
                        Log.e(tag, "navigateToDetails | Sticker is undefined")

                        _mainFeedScreenEventFlow.emit(
                            UiEvent.Navigate(
                                destination = EmoteDetailsScreenDestination(
                                    emoteId = mainFeedEmote.id
                                )
                            )
                        )
                    }
                }

                is Resource.Error -> {
                    Log.e(
                        tag,
                        "navigateToDetails | ${emoteAlreadyDownloadedAsStickerResult.logging}"
                    )

                    _mainFeedScreenEventFlow.emit(
                        UiEvent.Navigate(
                            destination = EmoteDetailsScreenDestination(emoteId = mainFeedEmote.id)
                        )
                    )
                }
            }
        }
    }

    fun setQuery(query: String) {
        Log.d(tag, "setQuery | query: $query")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(query = query)
    }

    private fun setCount(count: Int) {
        Log.d(tag, "setCount | count: $count")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(count = count)
    }

    private fun setEmotes(emotes: List<MainFeedEmote>) {
        Log.d(tag, "setEmotes | emotes: $emotes")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(emotes = emotes)
    }

    private fun setSearchedQuery(searchedQuery: String) {
        Log.d(tag, "setSearchedQuery | searchedQuery: $searchedQuery")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(searchedQuery = searchedQuery)
    }

    private fun setSearchHistory(searchHistory: List<MainFeedEmoteSearchHistoryItem>) {
        Log.d(tag, "setSearchHistory | searchHistory: $searchHistory")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(searchHistory = searchHistory)
    }

    fun setSearchActive(isActive: Boolean) {
        Log.d(tag, "setSearchActive | isActive: $isActive")

        if (isActive) {
            getSearchHistory()
            // TODO: Outsource to Variable | Doesn't work in landscape
            setSearchBarHeight(height = 0.5f)
        } else {
            // TODO: Outsource to Variable | Doesn't work in landscape
            setSearchBarHeight(height = 0.08f)
        }

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(searchActive = isActive)
    }

    fun requestSearchBarFocus() {
        Log.d(tag, "requestSearchBarFocus")

        _mainFeedStateFlow.value.searchBarFocusRequester.requestFocus()
    }

    fun loadNextEmotes() {
        Log.d(tag, "loadNextEmotes")

        viewModelScope.launch(Dispatchers.IO) {
            pagination.loadNextItems()
        }
    }

    private fun setPage(page: Int) {
        Log.d(tag, "setPage | page: $page")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(page = page)
    }

    fun searchEmotes() {
        Log.d(tag, "searchEmotes | query: ${_mainFeedStateFlow.value.query}")

        val searchQuery = _mainFeedStateFlow.value.query
        addToSearchHistory(searchQuery = searchQuery)
        setSearchedQuery(searchedQuery = searchQuery)
        setEmotes(emotes = emptyList())
        setDisplayedEmotes(mainFeedEmotes = emptyList())
        setPage(page = 1)
        loadNextEmotes()
    }

    private fun getSearchHistory() {
        Log.d(tag, "getSearchHistory")

        viewModelScope.launch(Dispatchers.IO) {
            getMainFeedEmotesSearchHistoryUseCase()
                .collectLatest { mainFeedEmoteSearchHistoryItems: List<MainFeedEmoteSearchHistoryItem> ->
                    setSearchHistory(searchHistory = mainFeedEmoteSearchHistoryItems)
                }
        }
    }

    private fun addToSearchHistory(searchQuery: String) {
        Log.d(tag, "addToSearchHistory | searchQuery: $searchQuery")

        viewModelScope.launch(Dispatchers.IO) {
            val addSearchQueryToHistoryResult =
                addMainFeedEmoteSearchHistoryItemUseCase(searchQuery = searchQuery)

            when (addSearchQueryToHistoryResult) {
                is Resource.Success -> {
                    Log.d(tag, "addToSearchHistory | Success")
                }

                is Resource.Error -> {
                    Log.e(tag, "addToSearchHistory | ${addSearchQueryToHistoryResult.logging}")
                }
            }
        }
    }

    private fun setSearchBarHeight(height: Float) {
        Log.d(tag, "setSearchBarHeight | height: $height")

        _mainFeedStateFlow.value = mainFeedStateFlow.value.copy(searchBarHeight = height)
    }
}
