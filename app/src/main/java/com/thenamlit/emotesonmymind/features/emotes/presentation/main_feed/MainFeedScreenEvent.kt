package com.thenamlit.emotesonmymind.features.emotes.presentation.main_feed

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.core.util.UiText


sealed class MainFeedScreenEvent {
    class Navigate(val navigationEvent: NavigationEvent) : MainFeedScreenEvent()
    class Error(val errorEvent: ErrorEvent) : MainFeedScreenEvent()

    fun handleEvents(
        event: MainFeedScreenEvent,
        onNavigate: (Direction) -> Unit,
        onSingleError: (title: UiText?, text: UiText) -> Unit,
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
        }
    }
}
