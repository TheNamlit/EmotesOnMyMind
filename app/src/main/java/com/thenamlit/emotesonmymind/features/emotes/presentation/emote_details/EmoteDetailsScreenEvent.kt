package com.thenamlit.emotesonmymind.features.emotes.presentation.emote_details

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.core.util.UiText


sealed class EmoteDetailsScreenEvent {
    class Navigate(val navigationEvent: NavigationEvent) : EmoteDetailsScreenEvent()
    class Error(val errorEvent: ErrorEvent) : EmoteDetailsScreenEvent()
    class SuccessfulDownload(val uiText: UiText) : EmoteDetailsScreenEvent()

    fun handleEvents(
        event: EmoteDetailsScreenEvent,
        onNavigate: (Direction) -> Unit,
        onSingleError: (title: UiText?, text: UiText) -> Unit,
        onSuccessfulDownload: (text: UiText) -> Unit,
    ) {
        when (event) {
            is Navigate -> {
                event.navigationEvent.handleNavigation(
                    navigationEvent = event.navigationEvent,
                    onNavigate = onNavigate,
                )
            }

            is Error -> {
                event.errorEvent.handleError(
                    errorEvent = event.errorEvent,
                    onSingleError = onSingleError,
                )
            }

            is SuccessfulDownload -> {
                onSuccessfulDownload(event.uiText)
            }
        }
    }
}
