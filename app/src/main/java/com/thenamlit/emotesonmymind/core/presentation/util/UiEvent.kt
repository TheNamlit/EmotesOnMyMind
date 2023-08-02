package com.thenamlit.emotesonmymind.core.presentation.util

import com.ramcosta.composedestinations.spec.Direction
import com.thenamlit.emotesonmymind.core.util.Event
import com.thenamlit.emotesonmymind.core.util.UiText


sealed class UiEvent : Event() {
    data class ShowSnackbar(val uiText: UiText) : UiEvent()
    data class Navigate(val destination: Direction) : UiEvent()
    object NavigateUp : UiEvent()
    object OnLogin : UiEvent()
    object Save : UiEvent()
}
