package com.thenamlit.emotesonmymind.features.sticker.presentation.sticker_details

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.core.util.UiText


sealed class StickerDetailsScreenEvent {
    class Navigate(val navigationEvent: NavigationEvent) : StickerDetailsScreenEvent()
    class Error(val errorEvent: ErrorEvent) : StickerDetailsScreenEvent()

    fun handleEvents(
        event: StickerDetailsScreenEvent,
        onNavigate: (Direction) -> Unit,
        onNavigateUp: () -> Unit,
        onSingleError: (title: UiText?, text: UiText) -> Unit,
    ) {
        when (event) {
            is Navigate -> {
                event.navigationEvent.handleNavigation(
                    navigationEvent = event.navigationEvent,
                    onNavigate = onNavigate,
                    onNavigateUp = onNavigateUp
                )
            }

            is Error -> {
                event.errorEvent.handleError(
                    errorEvent = event.errorEvent,
                    onSingleError = onSingleError,
                )
            }
        }
    }
}
