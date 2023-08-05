package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.select_sticker_to_add_to_collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.GetStickerUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.GetLocalStickerImageFileUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details.AddStickerListToCollectionUseCase
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
class SelectStickerToAddToCollectionAlertDialogViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
    private val getStickerUseCase: GetStickerUseCase,
    private val getLocalStickerImageFileUseCase: GetLocalStickerImageFileUseCase,
    private val addStickerListToCollectionUseCase: AddStickerListToCollectionUseCase,
) : ViewModel() {
    private val tag =
        Logging.loggingPrefix +
                SelectStickerToAddToCollectionAlertDialogViewModel::class.java.simpleName

    private val _selectStickerToAddToCollectionAlertDialogStateFlow =
        MutableStateFlow(SelectStickerToAddToCollectionAlertDialogState())
    val selectStickerToAddToCollectionAlertDialogStateFlow:
            StateFlow<SelectStickerToAddToCollectionAlertDialogState> =
        _selectStickerToAddToCollectionAlertDialogStateFlow.asStateFlow()

    private val _selectStickerToAddToCollectionAlertDialogEventFlow =
        MutableSharedFlow<SelectStickerToAddToCollectionAlertDialogEvent>()
    val selectStickerToAddToCollectionAlertDialogEventFlow =
        _selectStickerToAddToCollectionAlertDialogEventFlow.asSharedFlow()

    fun resetState() {
        Log.d(tag, "resetState")

        setSelectedStickerList(stickerList = emptyList())
        setNotSelectedStickerList(stickerList = emptyList())
    }

    fun setCollection(stickerCollection: StickerCollection) {
        Log.d(tag, "setCollection | stickerCollection: $stickerCollection")

        _selectStickerToAddToCollectionAlertDialogStateFlow.value =
            selectStickerToAddToCollectionAlertDialogStateFlow.value.copy(
                collection = stickerCollection
            )
    }

    fun initiateStickerLists() {
        Log.d(tag, "initiateStickerLists")

        viewModelScope.launch(Dispatchers.IO) {
            val stickerCollection =
                _selectStickerToAddToCollectionAlertDialogStateFlow.value.collection

            getStickerUseCase.getAllNotInIdList(
                idList = stickerCollection.stickers.map { sticker: Sticker -> sticker.id },
                animated = stickerCollection.animated
            ).collect { stickerList: List<Sticker> ->
                setNotSelectedStickerList(stickerList = stickerList)
            }
        }
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


    /*
     *
     * selectedStickerList
     *
     */

    private fun setSelectedStickerList(stickerList: List<Sticker>) {
        Log.d(tag, "setSelectedStickerList | stickerList: $stickerList")

        _selectStickerToAddToCollectionAlertDialogStateFlow.value =
            selectStickerToAddToCollectionAlertDialogStateFlow.value.copy(
                selectedStickerList = stickerList
            )
    }

    fun addToSelectedStickerList(sticker: Sticker) {
        Log.d(tag, "addToSelectedStickerList | sticker: $sticker")

        setSelectedStickerList(
            stickerList = _selectStickerToAddToCollectionAlertDialogStateFlow.value.selectedStickerList +
                    sticker
        )
    }

    fun removeFromSelectedStickerList(sticker: Sticker) {
        Log.d(tag, "removeFromSelectedStickerList | sticker: $sticker")

        setSelectedStickerList(
            stickerList = _selectStickerToAddToCollectionAlertDialogStateFlow.value.selectedStickerList
                .filterNot { stickerInList: Sticker ->
                    stickerInList == sticker
                }
        )
    }


    /*
     *
     * notSelectedStickerList
     *
     */

    private fun setNotSelectedStickerList(stickerList: List<Sticker>) {
        Log.d(tag, "setNotSelectedStickerList | stickerList: $stickerList")

        _selectStickerToAddToCollectionAlertDialogStateFlow.value =
            selectStickerToAddToCollectionAlertDialogStateFlow.value.copy(
                notSelectedStickerList = stickerList
            )
    }

    fun addToNotSelectedStickerList(sticker: Sticker) {
        Log.d(tag, "addToNotSelectedStickerList | sticker: $sticker")

        setNotSelectedStickerList(
            stickerList = _selectStickerToAddToCollectionAlertDialogStateFlow.value.notSelectedStickerList +
                    sticker
        )
    }

    fun removeFromNotSelectedStickerList(sticker: Sticker) {
        Log.d(tag, "removeFromNotSelectedStickerList | sticker: $sticker")

        setNotSelectedStickerList(
            stickerList = _selectStickerToAddToCollectionAlertDialogStateFlow.value.notSelectedStickerList
                .filterNot { stickerInList: Sticker ->
                    stickerInList == sticker
                }
        )
    }

    fun saveSelectedStickerListToCollection() {
        Log.d(tag, "saveSelectedStickerListToCollection")

        viewModelScope.launch(Dispatchers.IO) {
            val selectedStickerList =
                _selectStickerToAddToCollectionAlertDialogStateFlow.value.selectedStickerList
            val collectionId =
                _selectStickerToAddToCollectionAlertDialogStateFlow.value.collection.id

            val addedStickerListToCollectionResult = addStickerListToCollectionUseCase(
                stickerCollectionId = collectionId,
                stickerIds = selectedStickerList.map { it.id }
            )

            when (addedStickerListToCollectionResult) {
                is Resource.Success -> {
                    Log.d(
                        tag,
                        "saveSelectedStickerListToCollection | Added selected StickerList to Collection"
                    )

                    viewModelScope.launch {
                        _selectStickerToAddToCollectionAlertDialogEventFlow.emit(
                            value = SelectStickerToAddToCollectionAlertDialogEvent.Save
                        )
                    }
                }

                is Resource.Error -> {
                    Log.e(
                        tag,
                        "saveSelectedStickerListToCollection | ${addedStickerListToCollectionResult.uiText}"
                    )
                }
            }
        }
    }
}
