package com.thenamlit.emotesonmymind.core.presentation.util

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.util.Event


sealed class NavigationEvent : Event() {
    data class Navigate(val destination: Direction) : NavigationEvent()
    object NavigateUp : NavigationEvent()

    fun handleNavigation(
        navigationEvent: NavigationEvent,
        onNavigate: (Direction) -> Unit = {},
        onNavigateUp: () -> Unit = {},
    ) {
        when (navigationEvent) {
            is Navigate -> {
                onNavigate(navigationEvent.destination)
            }

            is NavigateUp -> {
                onNavigateUp()
            }
        }
    }
}
