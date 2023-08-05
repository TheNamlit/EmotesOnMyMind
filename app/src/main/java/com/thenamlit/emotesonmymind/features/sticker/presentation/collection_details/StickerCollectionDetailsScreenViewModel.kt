package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.thenamlit.emotesonmymind.BuildConfig
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import com.thenamlit.emotesonmymind.features.destinations.StickerDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection.DeleteCollectionUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.CheckForWhatsAppInstallationUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.GetLocalStickerImageFileUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.GetStickerCollectionUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.UpdateCollectionNameUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details.ValidateCollectionForWhatsAppUseCase
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details.RemoveStickerFromCollectionUseCase
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
class StickerCollectionDetailsScreenViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
    private val savedStateHandle: SavedStateHandle,
    private val getStickerCollectionUseCase: GetStickerCollectionUseCase,
    private val getLocalStickerImageFileUseCase: GetLocalStickerImageFileUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val updateCollectionNameUseCase: UpdateCollectionNameUseCase,
    private val removeStickerFromCollectionUseCase: RemoveStickerFromCollectionUseCase,
    private val checkForWhatsAppInstallationUseCase: CheckForWhatsAppInstallationUseCase,
    private val validateCollectionForWhatsAppUseCase: ValidateCollectionForWhatsAppUseCase,
) : ViewModel() {
    private val tag =
        Logging.loggingPrefix + StickerCollectionDetailsScreenViewModel::class.java.simpleName

    private val _stickerCollectionDetailsStateFlow =
        MutableStateFlow(StickerCollectionDetailsState())
    val stickerCollectionDetailsStateFlow: StateFlow<StickerCollectionDetailsState> =
        _stickerCollectionDetailsStateFlow.asStateFlow()

    private val _stickerCollectionDetailsScreenEventFlow =
        MutableSharedFlow<StickerCollectionDetailsScreenEvent>()
    val stickerCollectionDetailsScreenEventFlow =
        _stickerCollectionDetailsScreenEventFlow.asSharedFlow()


    init {
        Log.d(tag, "init")

        getStickerCollectionIdFromSavedState()?.let { stickerCollectionId: String ->
            setStickerCollectionId(collectionId = stickerCollectionId)
            getStickerCollection()
        } ?: kotlin.run {
            Log.e(tag, "Couldn't get StickerCollectionId from SavedStateHandle")
            // TODO:
            //  Can't emit event here because it doesn't get observed from the screen yet
            //  Could maybe display an error saved in state instead of making it an empty screen?
        }
    }

    private fun getStickerCollectionIdFromSavedState(): String? {
        Log.d(tag, "getStickerCollectionIdFromSavedState")

        return savedStateHandle.get<String>("stickerCollectionId")
    }

    fun getImageLoader(): ImageLoader {
        Log.d(tag, "getImageLoader")

        return imageLoader
    }

    private fun emitSingleError(uiText: UiText) {
        Log.d(tag, "emitSingleError | uiText: $uiText")

        viewModelScope.launch(Dispatchers.IO) {
            _stickerCollectionDetailsScreenEventFlow.emit(
                value = StickerCollectionDetailsScreenEvent.Error(
                    errorEvent = ErrorEvent.SingleError(
                        text = uiText
                    )
                )
            )
        }
    }

    fun showSnackbar(text: String) {
        Log.d(tag, "showSnackbar | text: $text")

        viewModelScope.launch {
            _stickerCollectionDetailsStateFlow.value.snackbarHostState.showSnackbar(message = text)
        }
    }


    /*
     *
     * Sticker
     *
     */

    fun getStickerCollection() {
        Log.d(tag, "getStickerCollection")

        viewModelScope.launch(Dispatchers.IO) {
            val stickerCollectionResult: Resource<StickerCollection> =
                getStickerCollectionUseCase.byId(
                    stickerCollectionId = _stickerCollectionDetailsStateFlow.value.collectionId
                )

            when (stickerCollectionResult) {
                is Resource.Success -> {
                    stickerCollectionResult.data?.let { stickerCollection: StickerCollection ->
                        setStickerCollection(stickerCollection = stickerCollection)
                        setEditModeCollectionName(name = stickerCollection.name)
                    } ?: kotlin.run {
                        Log.e(tag, "getStickerCollection | StickerCollection is undefined")
                    }
                }

                is Resource.Error -> {
                    Log.e(tag, "getStickerCollection | ${stickerCollectionResult.uiText}")
                }
            }
        }
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

    fun deleteStickerCollection() {
        Log.d(tag, "deleteStickerCollection")

        viewModelScope.launch(Dispatchers.IO) {
            val deleteStickerCollectionResult =
                deleteCollectionUseCase(
                    id =
                    _stickerCollectionDetailsStateFlow.value.collectionId
                )

            when (deleteStickerCollectionResult) {
                is Resource.Success -> {
                    Log.d(
                        tag,
                        "Successfully deleted StickerCollection: " +
                                _stickerCollectionDetailsStateFlow.value.collectionId
                    )

                    navigateUp()
                }

                is Resource.Error -> {
                    Log.e(tag, "deleteStickerCollection | ${deleteStickerCollectionResult.logging}")
                    // TODO
                }
            }
        }
    }

    fun onStickerClicked(sticker: Sticker) {
        Log.d(tag, "onStickerClicked | sticker: $sticker")

        when (_stickerCollectionDetailsStateFlow.value.mode) {
            is StickerCollectionDetailsMode.Normal -> {
                navigateToStickerDetailsScreen(sticker = sticker)
            }

            is StickerCollectionDetailsMode.Edit -> {}

            is StickerCollectionDetailsMode.DeleteSticker -> {
                clickOnStickerInDeleteStickerMode(sticker = sticker)
            }
        }
    }


    /*
     *
     * WhatsApp
     *
     */

    // TODO: TEMPORARY
    private fun checkForAnimatedStickerCollectionBeingAddedToWhatsApp(): Boolean {
        Log.d(tag, "checkForAnimatedStickerCollectionBeingAddedToWhatsApp")

        return if (_stickerCollectionDetailsStateFlow.value.collection.animated) {
            addToCanNotAddToWhatsAppInfoAlertErrors(
                error = UiText.DynamicString(
                    value = "The functionality for adding animated stickers to WhatsApp is not " +
                            "supported at this time."
                )
            )
            true
        } else {
            false
        }
    }

    fun tryToAddToWhatsApp(rememberLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        Log.d(tag, "tryToAddToWhatsApp")

        // TODO: This is just temporary until I implemented a functioning Download/Compress of animated Stickers
        if (checkForAnimatedStickerCollectionBeingAddedToWhatsApp()) {
            showCanNotAddToWhatsAppInfoAlertDialog()
            return
        }

        when (val checkForWhatsAppInstallResult = checkForWhatsAppInstall()) {
            is Resource.Success -> {
                val validateCollectionForWhatsAppTransferResult =
                    validateCollectionForWhatsAppTransfer()

                when (validateCollectionForWhatsAppTransferResult) {
                    is Resource.Success -> {
                        Log.d(tag, "tryToAddToWhatsApp | Validation succeeded")

                        val addedToWhatsAppResult =
                            addToWhatsApp(rememberLauncher = rememberLauncher)

                        when (addedToWhatsAppResult) {
                            is Resource.Success -> {
                                Log.d(tag, "tryToAddToWhatsApp | Added to WhatsApp successfully")
                            }

                            is Resource.Error -> {
                                Log.e(tag, "tryToAddToWhatsApp | ${addedToWhatsAppResult.logging}")

                                addedToWhatsAppResult.uiText?.let { uiText: UiText ->
                                    emitSingleError(uiText = uiText)
                                } ?: kotlin.run {
                                    Log.e(tag, "tryToAddToWhatsApp | UiText is undefined")
                                    emitSingleError(uiText = UiText.unknownError())
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        Log.e(
                            tag,
                            "tryToAddToWhatsApp | ${validateCollectionForWhatsAppTransferResult.logging}"
                        )

                        showCanNotAddToWhatsAppInfoAlertDialog()
                    }
                }
            }

            is Resource.Error -> {
                Log.e(tag, "tryToAddToWhatsApp | ${checkForWhatsAppInstallResult.logging}")

                checkForWhatsAppInstallResult.uiText?.let { errorUiText: UiText ->
                    addToCanNotAddToWhatsAppInfoAlertErrors(error = errorUiText)
                }

                showCanNotAddToWhatsAppInfoAlertDialog()
            }
        }
    }

    private fun checkForWhatsAppInstall(): SimpleResource {
        Log.d(tag, "checkForWhatsAppInstall")

        return checkForWhatsAppInstallationUseCase()
    }

    private fun validateCollectionForWhatsAppTransfer(): SimpleResource {
        Log.d(tag, "validateCollectionForWhatsAppTransfer")

        val validateCollectionForWhatsAppResults: List<SimpleResource> =
            validateCollectionForWhatsAppUseCase(
                stickerCollection = _stickerCollectionDetailsStateFlow.value.collection
            )
        val hasError: Boolean = validateCollectionForWhatsAppResults.any { it is Resource.Error }

        Log.d(tag, "res: $validateCollectionForWhatsAppResults")
        return if (hasError) {
            validateCollectionForWhatsAppResults.forEach { simpleResourceResult: SimpleResource ->
                if (simpleResourceResult is Resource.Error) {
                    simpleResourceResult.uiText?.let { errorUiText: UiText ->
                        addToCanNotAddToWhatsAppInfoAlertErrors(error = errorUiText)
                    }
                }
            }

            Resource.Error(logging = "validateCollectionForWhatsAppTransfer | Validation failed")
        } else {
            Resource.Success(data = null)
        }
    }

    private fun addToWhatsApp(
        rememberLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    ): SimpleResource {
        Log.d(tag, "addToWhatsApp")

        val whatsAppIntent = Intent()
        whatsAppIntent.also {
            it.`package` = WhatsAppSettings.CONSUMER_WHATSAPP_PACKAGE_NAME
            it.action = WhatsAppSettings.INTENT_ACTION_ENABLE_STICKER_PACK
            it.putExtra(
                WhatsAppSettings.EXTRA_STICKER_PACK_ID,
                _stickerCollectionDetailsStateFlow.value.collectionId
            )
            it.putExtra(
                WhatsAppSettings.EXTRA_STICKER_PACK_AUTHORITY,
                BuildConfig.CONTENT_PROVIDER_AUTHORITY
            )
            it.putExtra(
                WhatsAppSettings.EXTRA_STICKER_PACK_NAME,
                _stickerCollectionDetailsStateFlow.value.collection.name
            )
        }

        return try {
            rememberLauncher.launch(whatsAppIntent)
            Resource.Success(data = null)
        } catch (e: ActivityNotFoundException) {
            Log.e(tag, "addToWhatsApp | ${e.message}\n${e.stackTrace}")

            Resource.Error(
                uiText = UiText.DynamicString(value = "Couldn't find or open WhatsApp.")
            )
        } catch (e: Exception) {
            Log.e(tag, "addToWhatsApp | ${e.message}\n${e.stackTrace}")

            Resource.Error(
                uiText = UiText.DynamicString(value = "Couldn't open WhatsApp.")
            )
        }
    }

    private fun addToCanNotAddToWhatsAppInfoAlertErrors(error: UiText) {
        Log.d(tag, "addToCanNotAddToWhatsAppInfoAlertErrors | error: $error")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                canNotAddToWhatsAppInfoAlertErrors =
                _stickerCollectionDetailsStateFlow.value.canNotAddToWhatsAppInfoAlertErrors + error
            )
    }

    fun resetCanNotAddToWhatsAppInfoAlertErrors() {
        Log.d(tag, "resetCanNotAddToWhatsAppInfoAlertErrors")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                canNotAddToWhatsAppInfoAlertErrors = emptyList()
            )
    }


    /*
     *
     * EDIT MODE
     *
     */

    fun setEditModeCollectionName(name: String) {
        Log.d(tag, "setEditModeCollectionName | name: $name")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(editModeCollectionName = name)
    }

    fun enterEditMode() {
        Log.d(tag, "enterEditMode")

        setMode(mode = StickerCollectionDetailsMode.Edit)
    }

    fun cancelEditMode() {
        Log.d(tag, "cancelEditMode")

        setEditModeCollectionName(name = _stickerCollectionDetailsStateFlow.value.collection.name)
        setMode(mode = StickerCollectionDetailsMode.Normal)
    }

    fun saveEditMode() {
        Log.d(tag, "saveEditMode")

        // Only update if the Collection-Name actually changed, otherwise just quit EditMode
        val editModeCollectionName = _stickerCollectionDetailsStateFlow.value.editModeCollectionName
        if (editModeCollectionName != _stickerCollectionDetailsStateFlow.value.collection.name) {
            updateCollectionName(editModeCollectionName = editModeCollectionName)
        }

        cancelEditMode()
    }

    private fun updateCollectionName(editModeCollectionName: String) {
        Log.d(tag, "updateCollectionName")

        viewModelScope.launch(Dispatchers.IO) {
            val updatedCollectionNameResult: SimpleResource = updateCollectionNameUseCase(
                id = _stickerCollectionDetailsStateFlow.value.collectionId,
                name = editModeCollectionName
            )

            when (updatedCollectionNameResult) {
                is Resource.Success -> {
                    Log.d(tag, "saveEditMode | Successfully updated Collection Name")

                    getStickerCollection()
                }

                is Resource.Error -> {
                    Log.e(tag, "saveEditMode | ${updatedCollectionNameResult.logging}")

                    updatedCollectionNameResult.uiText?.let { uiText: UiText ->
                        emitSingleError(uiText = uiText)
                    } ?: kotlin.run {
                        Log.e(tag, "saveEditMode | UiText is undefined")
                        emitSingleError(uiText = UiText.unknownError())
                    }
                }
            }
        }
    }


    /*
     *
     * DELETE STICKER MODE
     *
     */

    fun enterDeleteStickerMode() {
        Log.d(tag, "enterDeleteStickerMode")

        setMode(mode = StickerCollectionDetailsMode.DeleteSticker)
    }

    fun saveDeleteStickerMode() {
        Log.d(tag, "saveDeleteStickerMode")

        val selectedStickerIdsInDeleteMode =
            _stickerCollectionDetailsStateFlow.value.selectedStickerIdsInDeleteMode
        if (selectedStickerIdsInDeleteMode.isNotEmpty()) {
            removeStickerListFromCollection(stickerIds = selectedStickerIdsInDeleteMode)
        }

        cancelDeleteStickerMode()
    }

    fun cancelDeleteStickerMode() {
        Log.d(tag, "cancelDeleteStickerMode")

        resetSelectedStickerIdsInDeleteMode()
        setMode(mode = StickerCollectionDetailsMode.Normal)
    }

    private fun resetSelectedStickerIdsInDeleteMode() {
        Log.d(tag, "resetSelectedStickerIdsInDeleteMode")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                selectedStickerIdsInDeleteMode = emptyList()
            )
    }

    private fun removeStickerListFromCollection(stickerIds: List<String>) {
        Log.d(tag, "removeStickerListFromCollection | stickerIds: $stickerIds")

        viewModelScope.launch(Dispatchers.IO) {
            stickerIds.forEach { stickerId: String ->
                val removedStickerFromCollectionResult = removeStickerFromCollectionUseCase(
                    stickerCollectionId = _stickerCollectionDetailsStateFlow.value.collectionId,
                    stickerId = stickerId
                )

                when (removedStickerFromCollectionResult) {
                    is Resource.Success -> {
                        Log.d(tag, "Removed Sticker with ID: $stickerId successfully")

                        getStickerCollection()
                    }

                    is Resource.Error -> {
                        Log.d(
                            tag, "Failed to remove Sticker with ID: $stickerId " +
                                    "-> ${removedStickerFromCollectionResult.logging}"
                        )
                    }
                }
            }
        }
    }

    private fun clickOnStickerInDeleteStickerMode(sticker: Sticker) {
        Log.d(tag, "clickOnStickerInDeleteStickerMode | sticker: $sticker")

        if (sticker.id in _stickerCollectionDetailsStateFlow.value.selectedStickerIdsInDeleteMode) {
            deleteStickerFromSelectedStickerInDeleteStickerMode(stickerId = sticker.id)
        } else {
            addStickerToSelectedStickerInDeleteStickerMode(stickerId = sticker.id)
        }
    }

    private fun addStickerToSelectedStickerInDeleteStickerMode(stickerId: String) {
        Log.d(tag, "addStickerToSelectedStickerInDeleteStickerMode | stickerId: $stickerId")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                selectedStickerIdsInDeleteMode =
                _stickerCollectionDetailsStateFlow.value.selectedStickerIdsInDeleteMode + stickerId
            )
    }

    private fun deleteStickerFromSelectedStickerInDeleteStickerMode(stickerId: String) {
        Log.d(tag, "deleteStickerFromSelectedStickerInDeleteStickerMode | stickerId: $stickerId")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                selectedStickerIdsInDeleteMode =
                _stickerCollectionDetailsStateFlow.value.selectedStickerIdsInDeleteMode
                    .filterNot { stickerIdInList: String ->
                        stickerIdInList == stickerId
                    }
            )
    }


    /*
     *
     * showDeleteStickerCollectionAlertDialog
     *
     */

    fun showDeleteStickerCollectionAlertDialog() {
        Log.d(tag, "showDeleteStickerCollectionAlertDialog")

        setShowDeleteStickerCollectionAlertDialog(showAlertDialog = true)
    }

    fun hideDeleteStickerCollectionAlertDialog() {
        Log.d(tag, "hideDeleteStickerCollectionAlertDialog")

        setShowDeleteStickerCollectionAlertDialog(showAlertDialog = false)
    }

    private fun setShowDeleteStickerCollectionAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowDeleteStickerCollectionAlertDialog | showAlertDialog: $showAlertDialog")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                showDeleteStickerCollectionAlertDialog = showAlertDialog
            )
    }


    /*
     *
     * showCanNotAddToWhatsAppInfoAlertDialog
     *
     */

    private fun showCanNotAddToWhatsAppInfoAlertDialog() {
        Log.d(tag, "showCanNotAddToWhatsAppInfoAlertDialog")

        setShowCanNotAddToWhatsAppInfoAlertDialog(showAlertDialog = true)
    }

    fun hideCanNotAddToWhatsAppInfoAlertDialog() {
        Log.d(tag, "hideCanNotAddToWhatsAppInfoAlertDialog")

        setShowCanNotAddToWhatsAppInfoAlertDialog(showAlertDialog = false)
    }

    private fun setShowCanNotAddToWhatsAppInfoAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowCanNotAddToWhatsAppInfoAlertDialog | showAlertDialog: $showAlertDialog")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                showCanNotAddToWhatsAppInfoAlertDialog = showAlertDialog
            )
    }


    /*
     *
     * showSelectStickerToAddToCollectionAlertDialog
     *
     */

    fun showSelectStickerToAddToCollectionAlertDialog() {
        Log.d(tag, "showSelectStickerToAddToCollectionAlertDialog")

        setShowSelectStickerToAddToCollectionAlertDialog(showAlertDialog = true)
    }

    fun hideSelectStickerToAddToCollectionAlertDialog() {
        Log.d(tag, "hideSelectStickerToAddToCollectionAlertDialog")

        setShowSelectStickerToAddToCollectionAlertDialog(showAlertDialog = false)
    }

    private fun setShowSelectStickerToAddToCollectionAlertDialog(showAlertDialog: Boolean) {
        Log.d(
            tag,
            "setShowSelectStickerToAddToCollectionAlertDialog | showAlertDialog: $showAlertDialog"
        )

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(
                showSelectStickerToAddToCollectionAlertDialog = showAlertDialog
            )
    }


    /*
     *
     * Navigate
     *
     */

    private fun navigateUp() {
        Log.d(tag, "navigateUp")

        viewModelScope.launch {
            _stickerCollectionDetailsScreenEventFlow.emit(
                StickerCollectionDetailsScreenEvent.Navigate(
                    navigationEvent = NavigationEvent.NavigateUp
                )
            )
        }
    }

    private fun navigateToStickerDetailsScreen(sticker: Sticker) {
        Log.d(tag, "navigateToStickerDetailsScreen | sticker: $sticker")

        viewModelScope.launch(Dispatchers.IO) {
            _stickerCollectionDetailsScreenEventFlow.emit(
                value = StickerCollectionDetailsScreenEvent.Navigate(
                    navigationEvent = NavigationEvent.Navigate(
                        destination = StickerDetailsScreenDestination(
                            sticker = sticker
                        )
                    )
                )
            )
        }
    }


    /*
     *
     * Modify state functions
     *
     */

    private fun setStickerCollectionId(collectionId: String) {
        Log.d(tag, "setStickerCollectionId | collectionId: $collectionId")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(collectionId = collectionId)
    }

    private fun setMode(mode: StickerCollectionDetailsMode) {
        Log.d(tag, "setMode | mode: $mode")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(mode = mode)
    }

    private fun setStickerCollection(stickerCollection: StickerCollection) {
        Log.d(tag, "setStickerCollection | stickerCollection: $stickerCollection")

        _stickerCollectionDetailsStateFlow.value =
            stickerCollectionDetailsStateFlow.value.copy(collection = stickerCollection)
    }
}
