package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.add_sticker_to_collection

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection.GetCollectionsUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details.AddStickerToCollectionUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details.RemoveStickerFromCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddStickerToCollectionAlertDialogViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val addStickerToCollectionUseCase: AddStickerToCollectionUseCase,
    private val removeStickerFromCollectionUseCase: RemoveStickerFromCollectionUseCase,
) : ViewModel() {
    private val tag =
        Logging.loggingPrefix + AddStickerToCollectionAlertDialogViewModel::class.java.simpleName

    private val _addStickerToCollectionAlertDialogStateFlow =
        MutableStateFlow(AddStickerToCollectionAlertDialogState())
    val addStickerToCollectionAlertDialogStateFlow: StateFlow<AddStickerToCollectionAlertDialogState> =
        _addStickerToCollectionAlertDialogStateFlow.asStateFlow()

    init {
        Log.d(tag, "init")

        getStickerFromSavedState()?.let { sticker: Sticker ->
            setSticker(sticker = sticker)
        } ?: kotlin.run {
            Log.e(tag, "init | Sticker is undefined")
            // TODO
        }
    }

    private fun getStickerFromSavedState(): Sticker? {
        Log.d(tag, "getStickerFromSavedState")

        return savedStateHandle.get<Sticker>("sticker")
    }

    fun resetState() {
        Log.d(tag, "resetState")

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                selectedStickerCollectionStates = emptyList(),
                notSelectedStickerCollectionStates = emptyList(),
                stickerCollectionSelectedStates = emptyList(),
                selectAllCollectionsButtonState = SelectAllCollectionsButtonState.NoCollectionsAvailable,
            )
    }

    private fun setSticker(sticker: Sticker) {
        Log.d(tag, "setSticker | sticker: $sticker")

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(sticker = sticker)
    }

    private fun updateSelectAllCollectionsButtonState() {
        Log.d(tag, "updateSelectAllCollectionsButtonState")

        val allStickerCollections =
            _addStickerToCollectionAlertDialogStateFlow.value.stickerCollectionSelectedStates
        val allSelectedStickerCollections =
            _addStickerToCollectionAlertDialogStateFlow.value.selectedStickerCollectionStates

        if (allStickerCollections.isNotEmpty()) {
            if (allStickerCollections.size == allSelectedStickerCollections.size) {
                setSelectAllCollectionsButtonState(
                    selectAllCollectionsButtonState = SelectAllCollectionsButtonState.RemoveAll()
                )
            } else {
                setSelectAllCollectionsButtonState(
                    selectAllCollectionsButtonState = SelectAllCollectionsButtonState.AddAll()
                )
            }
        } else {
            setSelectAllCollectionsButtonState(
                selectAllCollectionsButtonState = SelectAllCollectionsButtonState.NoCollectionsAvailable
            )
        }
    }

    private fun setSelectAllCollectionsButtonState(selectAllCollectionsButtonState: SelectAllCollectionsButtonState) {
        Log.d(
            tag,
            "setSelectAllCollectionsButtonState | selectAllCollectionsButtonState: $selectAllCollectionsButtonState"
        )

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                selectAllCollectionsButtonState = selectAllCollectionsButtonState
            )
    }

    fun getInitialCollections() {
        Log.d(tag, "getInitialCollections")

        getInitialSelectedStickerCollections()
        getInitialNotSelectedStickerCollections()
    }

    private fun getInitialSelectedStickerCollections() {
        Log.d(tag, "getInitialSelectedStickerCollections")

        viewModelScope.launch(Dispatchers.IO) {
            getCollectionsUseCase.getAllSelectedCollectionsOfSticker(
                stickerId = addStickerToCollectionAlertDialogStateFlow.value.sticker.id,
                animated = addStickerToCollectionAlertDialogStateFlow.value.sticker.stickerImageData.animated,
            ).collect { selectedStickerCollections: List<StickerCollection> ->
                Log.d(
                    tag,
                    "getInitialSelectedStickerCollections | selectedStickerCollections: $selectedStickerCollections"
                )

                _addStickerToCollectionAlertDialogStateFlow.value =
                    addStickerToCollectionAlertDialogStateFlow.value.copy(
                        selectedStickerCollectionStates =
                        selectedStickerCollections
                            .map { selectedStickerCollection: StickerCollection ->
                                StickerCollectionSelectedState(
                                    stickerCollection = selectedStickerCollection,
                                    initiallySelected = true,
                                    currentlySelected = true
                                )
                            }
                    )

                createStickerCollectionSelectedStates()
            }
        }
    }

    private fun getInitialNotSelectedStickerCollections() {
        Log.d(tag, "getInitialNotSelectedStickerCollections")

        viewModelScope.launch(Dispatchers.IO) {
            getCollectionsUseCase.getAllNotSelectedCollectionsOfSticker(
                stickerId = addStickerToCollectionAlertDialogStateFlow.value.sticker.id,
                animated = addStickerToCollectionAlertDialogStateFlow.value.sticker.stickerImageData.animated,
            ).collect { notSelectedStickerCollections: List<StickerCollection> ->
                Log.d(
                    tag,
                    "getInitialNotSelectedStickerCollections | notSelectedStickerCollections: $notSelectedStickerCollections"
                )

                _addStickerToCollectionAlertDialogStateFlow.value =
                    addStickerToCollectionAlertDialogStateFlow.value.copy(
                        notSelectedStickerCollectionStates =
                        notSelectedStickerCollections
                            .map { notSelectedStickerCollection: StickerCollection ->
                                StickerCollectionSelectedState(
                                    stickerCollection = notSelectedStickerCollection,
                                    initiallySelected = false,
                                    currentlySelected = false
                                )
                            }
                    )

                createStickerCollectionSelectedStates()
            }
        }
    }

    private fun createStickerCollectionSelectedStates() {
        Log.d(tag, "createStickerCollectionSelectedStates")

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                stickerCollectionSelectedStates =
                _addStickerToCollectionAlertDialogStateFlow.value.selectedStickerCollectionStates +
                        _addStickerToCollectionAlertDialogStateFlow.value.notSelectedStickerCollectionStates
            )

        updateSelectAllCollectionsButtonState()
    }

    fun setCollectionSelected(stickerCollectionSelectedState: StickerCollectionSelectedState) {
        Log.d(
            tag,
            "setCollectionSelected | stickerCollectionSelectedState: $stickerCollectionSelectedState"
        )

        // Remove from previous List and add to the opposite List
        if (stickerCollectionSelectedState.currentlySelected) {
            removeFromSelectedStickerCollectionStates(
                collectionId = stickerCollectionSelectedState.stickerCollection.id
            )
            addToNotSelectedStickerCollectionStates(
                stickerCollectionSelectedState = stickerCollectionSelectedState
            )
        } else {
            removeFromNotSelectedStickerCollectionStates(
                collectionId = stickerCollectionSelectedState.stickerCollection.id
            )
            addToSelectedStickerCollectionStates(
                stickerCollectionSelectedState = stickerCollectionSelectedState
            )
        }

        createStickerCollectionSelectedStates()
    }

    private fun addToSelectedStickerCollectionStates(
        stickerCollectionSelectedState: StickerCollectionSelectedState,
    ) {
        Log.d(
            tag,
            "addToSelectedStickerCollectionStates | stickerCollectionSelectedState: $stickerCollectionSelectedState"
        )

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                selectedStickerCollectionStates =
                _addStickerToCollectionAlertDialogStateFlow.value.selectedStickerCollectionStates +
                        StickerCollectionSelectedState(
                            stickerCollection = stickerCollectionSelectedState.stickerCollection,
                            initiallySelected = stickerCollectionSelectedState.initiallySelected,
                            currentlySelected = !stickerCollectionSelectedState.currentlySelected
                        )
            )
    }

    private fun addToNotSelectedStickerCollectionStates(
        stickerCollectionSelectedState: StickerCollectionSelectedState,
    ) {
        Log.d(
            tag,
            "addToNotSelectedStickerCollectionStates | stickerCollectionSelectedState: $stickerCollectionSelectedState"
        )

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                notSelectedStickerCollectionStates =
                _addStickerToCollectionAlertDialogStateFlow.value.notSelectedStickerCollectionStates +
                        StickerCollectionSelectedState(
                            stickerCollection = stickerCollectionSelectedState.stickerCollection,
                            initiallySelected = stickerCollectionSelectedState.initiallySelected,
                            currentlySelected = !stickerCollectionSelectedState.currentlySelected
                        )
            )
    }


    private fun removeFromSelectedStickerCollectionStates(collectionId: String) {
        Log.d(tag, "removeFromSelectedStickerCollectionStates | collectionId: $collectionId")

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                selectedStickerCollectionStates =
                _addStickerToCollectionAlertDialogStateFlow.value.selectedStickerCollectionStates
                    .filterNot { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                        stickerCollectionSelectedState.stickerCollection.id == collectionId
                    }
            )
    }

    private fun removeFromNotSelectedStickerCollectionStates(collectionId: String) {
        Log.d(tag, "removeFromNotSelectedStickerCollectionStates | collectionId: $collectionId")

        _addStickerToCollectionAlertDialogStateFlow.value =
            addStickerToCollectionAlertDialogStateFlow.value.copy(
                notSelectedStickerCollectionStates =
                _addStickerToCollectionAlertDialogStateFlow.value.notSelectedStickerCollectionStates
                    .filterNot { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                        stickerCollectionSelectedState.stickerCollection.id == collectionId
                    }
            )
    }

    fun saveSelectedStickerCollections() {
        Log.d(tag, "saveSelectedStickerCollections")

        _addStickerToCollectionAlertDialogStateFlow.value.stickerCollectionSelectedStates
            .forEach { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                if (stickerCollectionSelectedState.initiallySelected !=
                    stickerCollectionSelectedState.currentlySelected
                ) {
                    if (stickerCollectionSelectedState.currentlySelected) {
                        addStickerToCollection(
                            stickerCollectionId = stickerCollectionSelectedState.stickerCollection.id
                        )
                    } else {
                        removeStickerFromCollection(
                            stickerCollectionId = stickerCollectionSelectedState.stickerCollection.id
                        )
                    }
                }
            }
    }

    private fun addStickerToCollection(stickerCollectionId: String) {
        Log.d(tag, "addStickerToCollection | stickerCollectionId: $stickerCollectionId")

        viewModelScope.launch(Dispatchers.IO) {
            val addStickerToCollectionResult = addStickerToCollectionUseCase(
                stickerCollectionId = stickerCollectionId,
                stickerId = _addStickerToCollectionAlertDialogStateFlow.value.sticker.id
            )

            when (addStickerToCollectionResult) {
                is Resource.Success -> {
                    Log.d(
                        tag,
                        "addStickerToCollection | Successfully added Sticker to Collection $stickerCollectionId"
                    )
                }

                is Resource.Error -> {
                    Log.e(tag, "addStickerToCollection | ${addStickerToCollectionResult.uiText}")
                }
            }
        }
    }

    private fun removeStickerFromCollection(stickerCollectionId: String) {
        Log.d(tag, "removeStickerFromCollection | stickerCollectionId: $stickerCollectionId")

        viewModelScope.launch(Dispatchers.IO) {
            val removeStickerFromCollectionResult = removeStickerFromCollectionUseCase(
                stickerCollectionId = stickerCollectionId,
                stickerId = _addStickerToCollectionAlertDialogStateFlow.value.sticker.id
            )

            when (removeStickerFromCollectionResult) {
                is Resource.Success -> {
                    Log.d(
                        tag,
                        "removeStickerFromCollection | Successfully removed Sticker from Collection $stickerCollectionId"
                    )
                }

                is Resource.Error -> {
                    Log.e(
                        tag,
                        "removeStickerFromCollection | ${removeStickerFromCollectionResult.uiText}"
                    )
                }
            }
        }
    }

    fun addToAllCollections() {
        Log.d(tag, "addToAllCollections")

        _addStickerToCollectionAlertDialogStateFlow.value.notSelectedStickerCollectionStates
            .forEach { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                setCollectionSelected(
                    stickerCollectionSelectedState = stickerCollectionSelectedState
                )
            }
    }

    fun removeFromAllCollections() {
        Log.d(tag, "removeFromAllCollections")

        _addStickerToCollectionAlertDialogStateFlow.value.selectedStickerCollectionStates
            .forEach { stickerCollectionSelectedState: StickerCollectionSelectedState ->
                setCollectionSelected(
                    stickerCollectionSelectedState = stickerCollectionSelectedState
                )
            }
    }
}
