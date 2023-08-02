package com.thenamlit.emotesonmymind.features.sticker.presentation.util.alert_dialog.create_sticker_collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection.CreateCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreateStickerCollectionAlertDialogViewModel @Inject constructor(
    private val createCollectionUseCase: CreateCollectionUseCase,
) : ViewModel() {
    private val tag =
        Logging.loggingPrefix + CreateStickerCollectionAlertDialogViewModel::class.java.simpleName

    private val _createStickerCollectionAlertDialogStateFlow =
        MutableStateFlow(CreateStickerCollectionAlertDialogState())
    val createStickerCollectionAlertDialogStateFlow: StateFlow<CreateStickerCollectionAlertDialogState> =
        _createStickerCollectionAlertDialogStateFlow.asStateFlow()

    fun resetInputValues() {
        Log.d(tag, "resetInputValues")

        setAlertDialogCreateCollectionNameValue(newValue = "")
        setAlertDialogCreateCollectionAnimatedValue(animated = false)
    }

    fun setAlertDialogCreateCollectionNameValue(newValue: String) {
        Log.d(tag, "setAlertDialogCreateCollectionNameValue | newValue: $newValue")

        _createStickerCollectionAlertDialogStateFlow.value =
            createStickerCollectionAlertDialogStateFlow.value.copy(
                alertDialogCreateCollectionNameValue = newValue
            )
    }

    fun setAlertDialogCreateCollectionAnimatedValue(animated: Boolean) {
        Log.d(tag, "setAlertDialogCreateCollectionAnimatedValue | animated: $animated")

        _createStickerCollectionAlertDialogStateFlow.value =
            createStickerCollectionAlertDialogStateFlow.value.copy(
                alertDialogCreateCollectionAnimatedValue = animated
            )
    }

    fun createCollection() {
        Log.d(tag, "createCollection")

        val newCollectionName =
            _createStickerCollectionAlertDialogStateFlow.value.alertDialogCreateCollectionNameValue
        val newCollectionAnimated =
            _createStickerCollectionAlertDialogStateFlow.value.alertDialogCreateCollectionAnimatedValue

        if (newCollectionName.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                val createCollectionResult =
                    createCollectionUseCase.invoke(
                        name = newCollectionName,
                        animated = newCollectionAnimated
                    )

                when (createCollectionResult) {
                    is Resource.Success -> {
                        Log.d(tag, "createCollection | Created")
                    }

                    is Resource.Error -> {
                        Log.e(tag, "createCollection | ${createCollectionResult.uiText}")
                    }
                }
            }
        }
    }
}
