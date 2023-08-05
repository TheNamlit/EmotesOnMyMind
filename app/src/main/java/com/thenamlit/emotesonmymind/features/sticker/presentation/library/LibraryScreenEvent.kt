package com.thenamlit.emotesonmymind.features.sticker.presentation.library

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent


sealed class LibraryScreenEvent {
    class Navigate(val navigationEvent: NavigationEvent) : LibraryScreenEvent()
    class Error(val errorEvent: ErrorEvent) : LibraryScreenEvent()

    fun handleEvents(
        event: LibraryScreenEvent,
        onNavigate: (Direction) -> Unit,
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
                    errorEvent = event.errorEvent
                )
            }
        }
    }
}
