package com.thenamlit.emotesonmymind.core.presentation.util

import com.thenamlit.emotesonmymind.core.util.UiText


sealed class ErrorEvent {
    class SingleError(
        val title: UiText? = null,
        val text: UiText,
    ) : ErrorEvent()

    class MultipleErrors(
        val title: UiText?,
        val textList: List<UiText>,
    ) : ErrorEvent()

    fun handleError(
        errorEvent: ErrorEvent,
        onSingleError: (title: UiText?, text: UiText) -> Unit = { _, _ -> },
        onMultipleErrors: (title: UiText?, textList: List<UiText>) -> Unit = { _, _ -> },
    ) {
        when (errorEvent) {
            is SingleError -> {
                onSingleError(errorEvent.title, errorEvent.text)
            }

            is MultipleErrors -> {
                onMultipleErrors(errorEvent.title, errorEvent.textList)
            }
        }
    }
}
