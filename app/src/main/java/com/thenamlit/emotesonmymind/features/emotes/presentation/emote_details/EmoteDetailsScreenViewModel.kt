package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.ImageLoader
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.presentation.util.UiEvent
import com.thenamlit.emotesonmymind.core.util.DownloadAndScaleImageWorker
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.destinations.ProfileScreenDestination
import com.thenamlit.emotesonmymind.features.destinations.StickerDetailsScreenDestination
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details.GetEmoteDetailsUseCase
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details.IsEmoteAlreadySavedAsStickerUseCase
import com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details.SaveEmoteAsStickerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class EmoteDetailsScreenViewModel @Inject constructor(
    private val getEmoteDetailsUseCase: GetEmoteDetailsUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val imageLoader: ImageLoader,
    private val saveEmoteAsStickerUseCase: SaveEmoteAsStickerUseCase,
    private val isEmoteAlreadySavedAsStickerUseCase: IsEmoteAlreadySavedAsStickerUseCase,
    private val workManager: WorkManager,
) : ViewModel() {
    private val tag = Logging.loggingPrefix + EmoteDetailsScreenViewModel::class.java.simpleName

    private val _emoteDetailsStateFlow = MutableStateFlow(EmoteDetailsState())
    val emoteDetailsStateFlow: StateFlow<EmoteDetailsState> = _emoteDetailsStateFlow.asStateFlow()

    private val _emoteDetailsScreenEventFlow = MutableSharedFlow<UiEvent>()
    val emoteDetailsScreenEventFlow = _emoteDetailsScreenEventFlow.asSharedFlow()

    init {
        Log.d(tag, "init")

        getEmoteIdFromSavedState()?.let { emoteId: String ->
            getEmoteDetails(emoteId)
            isEmoteAlreadySavedAsSticker(emoteId = emoteId)
        } ?: kotlin.run {
            Log.e(tag, "Couldn't get EmoteId from SavedStateHandle")
            // TODO: Display Error
        }
    }

    fun getImageLoader(): ImageLoader {
        Log.d(tag, "getImageLoader")

        return imageLoader
    }

    private fun getEmoteIdFromSavedState(): String? {
        Log.d(tag, "getEmoteIdFromSavedState")

        return savedStateHandle.get<String>("emoteId")
    }

    private fun getEmoteDetails(emoteId: String) {
        Log.d(tag, "getEmoteDetails | emoteId: $emoteId")

        viewModelScope.launch(Dispatchers.IO) {
            val emoteDetailsResult = getEmoteDetailsUseCase(
                emoteId = emoteId,
                formats = _emoteDetailsStateFlow.value.formats
            )

            when (emoteDetailsResult) {
                is Resource.Success -> {
                    Log.d(tag, "getEmoteDetails | EmoteDetails: ${emoteDetailsResult.data}")

                    emoteDetailsResult.data?.let { emoteDetails: EmoteDetails ->
                        setEmoteDetails(emoteDetails = emoteDetails)
                    } ?: kotlin.run {
                        Log.e(tag, "getEmoteDetails | EmoteDetails undefined")
                    }
                }

                is Resource.Error -> {
                    Log.e(tag, "getEmoteDetails | Error: ${emoteDetailsResult.uiText}")
                    // TODO: Display Error
                }
            }
        }
    }

    private fun setEmoteDetails(emoteDetails: EmoteDetails) {
        Log.d(tag, "setEmoteDetails | emoteDetails: $emoteDetails")

        _emoteDetailsStateFlow.value = emoteDetailsStateFlow.value.copy(emoteDetails = emoteDetails)
    }

    fun setShowEmoteUserAlertDialog(showAlertDialog: Boolean) {
        Log.d(tag, "setShowEmoteUserAlertDialog | showAlertDialog: $showAlertDialog")

        _emoteDetailsStateFlow.value =
            emoteDetailsStateFlow.value.copy(showEmoteUserAlertDialog = showAlertDialog)
    }

    private fun setEmoteToStickerButtonState(emoteToStickerButtonState: EmoteToStickerButtonState) {
        Log.d(
            tag,
            "setEmoteToStickerButtonState | emoteToStickerButtonState: $emoteToStickerButtonState"
        )

        _emoteDetailsStateFlow.value =
            emoteDetailsStateFlow.value.copy(emoteToStickerButtonState = emoteToStickerButtonState)
    }

    private fun setSticker(sticker: Sticker) {
        Log.d(tag, "setSticker | sticker: $sticker")

        _emoteDetailsStateFlow.value = emoteDetailsStateFlow.value.copy(sticker = sticker)
    }

    fun navigateToEmoteUserProfile() {
        Log.d(tag, "navigateToEmoteUserProfile")

        viewModelScope.launch(Dispatchers.IO) {
            // TODO: To EmoteUserProfile and not own ProfileScreen
            _emoteDetailsScreenEventFlow.emit(
                UiEvent.Navigate(
                    destination = ProfileScreenDestination()
                )
            )
        }
    }

    fun navigateToStickerDetailsScreen() {
        Log.d(tag, "navigateToStickerDetailsScreen")

        _emoteDetailsStateFlow.value.sticker?.let { sticker: Sticker ->
            viewModelScope.launch(Dispatchers.IO) {
                _emoteDetailsScreenEventFlow.emit(
                    UiEvent.Navigate(
                        destination = StickerDetailsScreenDestination(
                            sticker = sticker
                        )
                    )
                )
            }
        } ?: kotlin.run {
            Log.e(tag, "navigateToStickerDetailsScreen | Sticker is undefined")
        }
    }

    fun isEmoteAlreadySavedAsSticker(emoteId: String) {
        Log.d(tag, "isEmoteAlreadySavedAsSticker | emoteId: $emoteId")

        viewModelScope.launch(Dispatchers.IO) {
            val stickerResult: Resource<Sticker> =
                isEmoteAlreadySavedAsStickerUseCase(emoteId = emoteId)

            when (stickerResult) {
                is Resource.Success -> {
                    Log.d(tag, "isEmoteAlreadySavedAsSticker | Found Sticker: $stickerResult")

                    stickerResult.data?.let { sticker: Sticker ->
                        setSticker(sticker = sticker)
                        setEmoteToStickerButtonState(
                            emoteToStickerButtonState = EmoteToStickerButtonState.GoToStickerDetails
                        )
                    } ?: kotlin.run {
                        Log.e(tag, "isEmoteAlreadySavedAsSticker | Sticker is undefined")
                    }
                }

                is Resource.Error -> {
                    Log.d(tag, "isEmoteAlreadySavedAsSticker | Didn't find Sticker")

                    setEmoteToStickerButtonState(
                        emoteToStickerButtonState = EmoteToStickerButtonState.SaveEmoteAsSticker
                    )
                }
            }
        }
    }

    fun saveEmoteAsSticker(emoteDetails: EmoteDetails) {
        Log.d(tag, "saveEmoteAsSticker | emoteDetails: $emoteDetails")

        setEmoteToStickerButtonState(
            emoteToStickerButtonState = EmoteToStickerButtonState.DownloadInProgress
        )

        viewModelScope.launch(Dispatchers.IO) {
            val workIdResult: Resource<UUID> =
                saveEmoteAsStickerUseCase(emoteDetails = emoteDetails)

            when (workIdResult) {
                is Resource.Success -> {
                    workIdResult.data?.let { workId: UUID ->
                        setWorkerState(workerState = WorkerState.Processing(workId = workId))

                        viewModelScope.launch {
                            listenForWorkerResult(workId = workId)
                        }
                    }
                }

                is Resource.Error -> {
                    Log.e(tag, "saveEmoteAsSticker | ${workIdResult.uiText}")

                    setWorkerState(
                        workerState = WorkerState.Failed(
                            message = workIdResult.uiText.toString()
                        )
                    )
                }
            }
        }
    }

    private suspend fun listenForWorkerResult(workId: UUID) {
        Log.d(tag, "listenForWorkerResult | workId: $workId")

        workManager.getWorkInfoByIdLiveData(workId).asFlow()
            .collectLatest { workInfoResult: WorkInfo? ->
                workInfoResult?.let { workInfo: WorkInfo ->
                    val resultText: String? =
                        workInfo.outputData.getString(DownloadAndScaleImageWorker.KEY_RESULT_TEXT)
                    val resultSuccess: Boolean = workInfo.outputData.getBoolean(
                        DownloadAndScaleImageWorker.KEY_RESULT_SUCCESS,
                        false
                    )
                    val progress: Int =
                        workInfo.progress.getInt(DownloadAndScaleImageWorker.KEY_RESULT_PROGRESS, 0)

                    if (resultSuccess) {
                        Log.d(tag, "listenForWorkerResult | Success! $resultText")

                        setWorkerState(workerState = WorkerState.Success)
                    } else {
                        if (progress == 100) {
                            setWorkerState(
                                workerState = WorkerState.Failed(
                                    message = "listenForWorkerResult | Worker Failed! $resultText"
                                )
                            )
                        } else {
                            Log.d(tag, "listenForWorkerResult | Still progressing...")
                        }
                    }
                } ?: kotlin.run {
                    Log.e(tag, "listenForWorkerResult | WorkInfoResult is undefined")
                }
            }
    }

    private fun setWorkerState(workerState: WorkerState) {
        Log.d(tag, "setWorkerState | workerState: $workerState")

        _emoteDetailsStateFlow.value = emoteDetailsStateFlow.value.copy(workerState = workerState)
    }
}
