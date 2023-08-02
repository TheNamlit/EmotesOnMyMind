package com.thenamlit.emotesonmymind.features.sticker.presentation.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.destinations.StickerCollectionDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.GetStickerUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection.GetCollectionsUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.GetLocalStickerImageFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val getStickerUseCase: GetStickerUseCase,
    private val getLocalStickerImageFileUseCase: GetLocalStickerImageFileUseCase,
) : ViewModel() {
    private val tag =
        Logging.loggingPrefix + LibraryScreenViewModel::class.java.simpleName

    private val _libraryScreenStateFlow = MutableStateFlow(LibraryScreenState())
    val libraryScreenStateFlow: StateFlow<LibraryScreenState> =
        _libraryScreenStateFlow.asStateFlow()

    private val _libraryScreenEventFlow = MutableSharedFlow<UiEvent>()
    val libraryScreenEventFlow = _libraryScreenEventFlow.asSharedFlow()


    /*
     *
     * BASE FUNCTIONS & INIT
     *
     */


    init {
        Log.d(tag, "init")

        initializeFilterChipItems()
        selectCollectionsTab()
    }

    fun getImageLoader(): ImageLoader {
        Log.d(tag, "getImageLoader")

        return imageLoader
    }

    fun getLocalStickerImageFile(path: String): File? {
        Log.d(tag, "getLocalStickerImageFile | path: $path")

        val localStickerImageFileResult =
            getLocalStickerImageFileUseCase(path = path)

        return when (localStickerImageFileResult) {
            is Resource.Success -> {
                localStickerImageFileResult.data
            }

            is Resource.Error -> {
                Log.e(tag, "getLocalStickerImageFile | ${localStickerImageFileResult.uiText}")
                null
            }
        }
    }

    private fun initializeFilterChipItems() {
        Log.d(tag, "initializeFilterChipItems")

        val filterChipItems = listOf(
            LibraryScreenStateFilterChipItem(
                type = LibraryScreenStateFilterChipItemType.Animated,
                selected = true,
                onClick = { filterChipItem: LibraryScreenStateFilterChipItem ->
                    toggleLibraryScreenStateFilterChipItemActive(filterChipItem = filterChipItem)
                }
            ),
            LibraryScreenStateFilterChipItem(
                type = LibraryScreenStateFilterChipItemType.NotAnimated,
                selected = true,
                onClick = { filterChipItem: LibraryScreenStateFilterChipItem ->
                    toggleLibraryScreenStateFilterChipItemActive(filterChipItem = filterChipItem)
                }
            ),
        )

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            filterChipItems = filterChipItems,
        )
    }

    private fun toggleLibraryScreenStateFilterChipItemActive(
        filterChipItem: LibraryScreenStateFilterChipItem,
    ) {
        Log.d(
            tag,
            "toggleLibraryScreenStateFilterChipItemActive | filterChipItem: $filterChipItem"
        )

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            filterChipItems = _libraryScreenStateFlow.value.filterChipItems.map {
                if (it == filterChipItem) {
                    LibraryScreenStateFilterChipItem(
                        type = it.type,
                        selected = !it.selected,
                        onClick = it.onClick
                    )
                } else {
                    it
                }
            },
        )

        getResults()
    }

    private fun getResults() {
        Log.d(tag, "getResults")

        if (_libraryScreenStateFlow.value.selectedTabCollections) {
            getCollectionResults()
        } else if (_libraryScreenStateFlow.value.selectedTabStickers) {
            getStickerResults()
        }
    }

    fun setIsSwipeToTheLeft(isSwipeToTheLeft: Boolean) {
        Log.d(tag, "setIsSwipeToTheLeft | isSwipeToTheLeft: $isSwipeToTheLeft")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            isSwipeToTheLeft = isSwipeToTheLeft
        )
    }

    fun updateTabIndexBasedOnSwipe() {
        Log.d(tag, "updateTabIndexBasedOnSwipe")

        // Don't allow looping, only left and right
        if (!_libraryScreenStateFlow.value.isSwipeToTheLeft
            && _libraryScreenStateFlow.value.selectedTabIndex == 0
        ) {
            selectStickersTab()
        } else if (_libraryScreenStateFlow.value.isSwipeToTheLeft
            && _libraryScreenStateFlow.value.selectedTabIndex == 1
        ) {
            selectCollectionsTab()
        }
    }


    /*
     *
     * COLLECTIONS TAB
     *
     */


    fun selectCollectionsTab() {
        Log.d(tag, "selectCollectionsTab")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            selectedTabIndex = 0,
            selectedTabCollections = true,
            selectedTabStickers = false,
        )

        getResults()
    }

    private fun getCollectionResults() {
        Log.d(tag, "getCollectionResults")

        resetStickerCollections()

        _libraryScreenStateFlow.value.filterChipItems.forEach { filterChipItem: LibraryScreenStateFilterChipItem ->
            when (filterChipItem.type) {
                is LibraryScreenStateFilterChipItemType.Animated -> {
                    if (filterChipItem.selected) {
                        getAllAnimatedStickerCollections()
                    }
                }

                is LibraryScreenStateFilterChipItemType.NotAnimated -> {
                    if (filterChipItem.selected) {
                        getAllNotAnimatedStickerCollections()
                    }
                }
            }
        }
    }

    private fun getAllAnimatedStickerCollections() {
        Log.d(tag, "getAllAnimatedStickerCollections")

        viewModelScope.launch(Dispatchers.IO) {
            getCollectionsUseCase.getAllAnimated()
                .collect { animatedStickerCollections: List<StickerCollection> ->
                    Log.d(
                        tag,
                        "getAllAnimatedStickerCollections | animatedStickerCollections: $animatedStickerCollections"
                    )

                    _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
                        animatedStickerCollections = animatedStickerCollections
                    )

                    buildStickerCollections()
                }
        }
    }

    private fun getAllNotAnimatedStickerCollections() {
        Log.d(tag, "getAllNotAnimatedStickerCollections")

        viewModelScope.launch(Dispatchers.IO) {
            getCollectionsUseCase.getAllNotAnimated()
                .collect { notAnimatedStickerCollections: List<StickerCollection> ->
                    Log.d(
                        tag,
                        "getAllNotAnimatedStickerCollections | " +
                                "notAnimatedStickerCollections: $notAnimatedStickerCollections"
                    )

                    _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
                        notAnimatedStickerCollections = notAnimatedStickerCollections
                    )

                    buildStickerCollections()
                }
        }
    }

    private fun resetStickerCollections() {
        Log.d(tag, "resetStickerCollections")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            stickerCollections = emptyList(),
            animatedStickerCollections = emptyList(),
            notAnimatedStickerCollections = emptyList()
        )
    }

    private fun buildStickerCollections() {
        Log.d(tag, "buildStickerCollections")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            stickerCollections = _libraryScreenStateFlow.value.animatedStickerCollections +
                    _libraryScreenStateFlow.value.notAnimatedStickerCollections
        )
    }

    fun navigateToStickerCollectionDetails(stickerCollectionId: String) {
        Log.d(tag, "navigateToStickerCollectionDetails | stickerCollectionId: $stickerCollectionId")

        viewModelScope.launch(Dispatchers.IO) {
            _libraryScreenEventFlow.emit(
                UiEvent.Navigate(
                    destination = StickerCollectionDetailsScreenDestination(
                        stickerCollectionId = stickerCollectionId
                    )
                )
            )
        }
    }

    fun setShowCreateCollectionAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowCreateCollectionAlertDialog | showAlertDialog: $showAlertDialog")

        _libraryScreenStateFlow.value =
            libraryScreenStateFlow.value.copy(
                showCreateCollectionAlertDialog = showAlertDialog
            )
    }


    /*
     *
     * COLLECTIONS TAB
     *
     */


    fun selectStickersTab() {
        Log.d(tag, "selectStickersTab")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            selectedTabIndex = 1,
            selectedTabCollections = false,
            selectedTabStickers = true,
        )

        getResults()
    }

    private fun getStickerResults() {
        Log.d(tag, "getStickerResults")

        resetStickers()

        _libraryScreenStateFlow.value.filterChipItems
            .forEach { filterChipItem: LibraryScreenStateFilterChipItem ->
                when (filterChipItem.type) {
                    is LibraryScreenStateFilterChipItemType.Animated -> {
                        if (filterChipItem.selected) {
                            getAllAnimatedStickers()
                        }
                    }

                    is LibraryScreenStateFilterChipItemType.NotAnimated -> {
                        if (filterChipItem.selected) {
                            getAllNotAnimatedStickers()
                        }
                    }
                }
            }
    }

    private fun getAllAnimatedStickers() {
        Log.d(tag, "getAllAnimatedStickers")

        viewModelScope.launch(Dispatchers.IO) {
            getStickerUseCase.getAllAnimated()
                .collect { animatedStickers: List<Sticker> ->
                    Log.d(tag, "getAllAnimatedStickers | animatedStickers: $animatedStickers")

                    _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
                        animatedStickers = animatedStickers
                    )

                    buildStickers()
                }
        }
    }

    private fun getAllNotAnimatedStickers() {
        Log.d(tag, "getAllNotAnimatedStickers")

        viewModelScope.launch(Dispatchers.IO) {
            getStickerUseCase.getAllNotAnimated()
                .collect { notAnimatedStickers: List<Sticker> ->
                    Log.d(
                        tag,
                        "getAllNotAnimatedStickers | notAnimatedStickers: $notAnimatedStickers"
                    )

                    _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
                        notAnimatedStickers = notAnimatedStickers
                    )

                    buildStickers()
                }
        }
    }

    private fun buildStickers() {
        Log.d(tag, "buildStickers")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            stickers = _libraryScreenStateFlow.value.animatedStickers +
                    _libraryScreenStateFlow.value.notAnimatedStickers
        )
    }

    private fun resetStickers() {
        Log.d(tag, "resetStickers")

        _libraryScreenStateFlow.value = libraryScreenStateFlow.value.copy(
            stickers = emptyList(),
            animatedStickers = emptyList(),
            notAnimatedStickers = emptyList()
        )
    }
}
