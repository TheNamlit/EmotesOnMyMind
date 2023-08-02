package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.GetLocalStickerImageFileUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details.DeleteStickerUseCase
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
class StickerDetailsScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageLoader: ImageLoader,
    private val getLocalStickerImageFileUseCase: GetLocalStickerImageFileUseCase,
    private val deleteStickerUseCase: DeleteStickerUseCase,
) : ViewModel() {
    private val tag = Logging.loggingPrefix + StickerDetailsScreenViewModel::class.java.simpleName

    private val _stickerDetailsStateFlow = MutableStateFlow(StickerDetailsState())
    val stickerDetailsStateFlow: StateFlow<StickerDetailsState> =
        _stickerDetailsStateFlow.asStateFlow()

    private val _stickerDetailsScreenEventFlow = MutableSharedFlow<UiEvent>()
    val stickerDetailsScreenEventFlow = _stickerDetailsScreenEventFlow.asSharedFlow()


    init {
        Log.d(tag, "init")

        getStickerFromSavedState()?.let { sticker: Sticker ->
            setSticker(sticker = sticker)
        } ?: kotlin.run {
            Log.e(tag, "init | Couldn't get Sticker from SavedStateHandle")
            // TODO
        }
    }

    private fun navigateUp() {
        Log.d(tag, "navigateUp")

        viewModelScope.launch {
            _stickerDetailsScreenEventFlow.emit(UiEvent.NavigateUp)
        }
    }

    fun getImageLoader(): ImageLoader {
        Log.d(tag, "getImageLoader")

        return imageLoader
    }

    private fun getStickerFromSavedState(): Sticker? {
        Log.d(tag, "getStickerFromSavedState")

        return savedStateHandle.get<Sticker>("sticker")
    }

    private fun setSticker(sticker: Sticker) {
        Log.d(tag, "setSticker | sticker: $sticker")

        _stickerDetailsStateFlow.value = stickerDetailsStateFlow.value.copy(sticker = sticker)
    }

    fun showAddStickerToCollectionAlertDialog() {
        Log.d(tag, "showAddStickerToCollectionAlertDialog")

        setShowAddStickerToCollectionAlertDialog(showAlertDialog = true)
    }

    fun hideAddStickerToCollectionAlertDialog() {
        Log.d(tag, "hideAddStickerToCollectionAlertDialog")

        setShowAddStickerToCollectionAlertDialog(showAlertDialog = false)
    }

    private fun setShowAddStickerToCollectionAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowAddStickerToCollectionAlertDialog | showAlertDialog: $showAlertDialog")

        _stickerDetailsStateFlow.value =
            stickerDetailsStateFlow.value.copy(
                showAddStickerToCollectionAlertDialog = showAlertDialog
            )
    }

    fun showCreateCollectionAlertDialog() {
        Log.d(tag, "showCreateCollectionAlertDialog")

        setShowCreateCollectionAlertDialog(showAlertDialog = true)
    }

    fun hideCreateCollectionAlertDialog() {
        Log.d(tag, "hideCreateCollectionAlertDialog")

        setShowCreateCollectionAlertDialog(showAlertDialog = false)
    }

    private fun setShowCreateCollectionAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowCreateCollectionAlertDialog | showAlertDialog: $showAlertDialog")

        _stickerDetailsStateFlow.value =
            stickerDetailsStateFlow.value.copy(
                showCreateCollectionAlertDialog = showAlertDialog
            )
    }

    fun showDeleteStickerAlertDialog() {
        Log.d(tag, "showDeleteStickerAlertDialog")

        setShowDeleteStickerAlertDialog(showAlertDialog = true)
    }

    fun hideDeleteStickerAlertDialog() {
        Log.d(tag, "hideDeleteStickerAlertDialog")

        setShowDeleteStickerAlertDialog(showAlertDialog = false)
    }

    private fun setShowDeleteStickerAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowDeleteStickerAlertDialog | showAlertDialog: $showAlertDialog")

        _stickerDetailsStateFlow.value =
            stickerDetailsStateFlow.value.copy(
                showDeleteStickerAlertDialog = showAlertDialog
            )
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
                Log.e(tag, "getLocalStickerImageFile | ${localStickerImageFileResult.logging}")
                null
            }
        }
    }

    fun deleteSticker() {
        Log.d(tag, "deleteSticker")

        viewModelScope.launch(Dispatchers.IO) {
            val deleteStickerResult =
                deleteStickerUseCase(sticker = _stickerDetailsStateFlow.value.sticker)

            when (deleteStickerResult) {
                is Resource.Success -> {
                    Log.d(
                        tag,
                        "Successfully deleted Sticker: ${_stickerDetailsStateFlow.value.sticker}"
                    )

                    navigateUp()
                }

                is Resource.Error -> {
                    Log.e(tag, "deleteSticker | ${deleteStickerResult.logging}")
                    // TODO
                }
            }
        }
    }
}
