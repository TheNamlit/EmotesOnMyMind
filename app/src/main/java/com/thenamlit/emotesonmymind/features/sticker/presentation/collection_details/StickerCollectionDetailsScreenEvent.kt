package com.thenamlit.emotesonmymind.features.sticker.presentation.collection_details

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.presentation.util.ErrorEvent
import com.thenamlit.emotesonmymind.core.presentation.util.NavigationEvent
import com.thenamlit.emotesonmymind.core.util.UiText


sealed class StickerCollectionDetailsScreenEvent {
    class Navigate(val navigationEvent: NavigationEvent) : StickerCollectionDetailsScreenEvent()
    class Error(val errorEvent: ErrorEvent) : StickerCollectionDetailsScreenEvent()

    fun handleEvents(
        event: StickerCollectionDetailsScreenEvent,
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
