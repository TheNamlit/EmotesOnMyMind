package com.thenamlit.emotesonmymind.core.util

typealias SimpleResource = Resource<Unit>

sealed class Resource<T>(
    val data: T? = null,
    val uiText: UiText? = null,
    val logging: String? = null,
) {
    class Success<T>(data: T?, uiText: UiText? = null, logging: String? = null) :
        Resource<T>(data = data, uiText = uiText, logging = logging)

    class Error<T>(uiText: UiText? = null, data: T? = null, logging: String? = null) :
        Resource<T>(data = data, uiText = uiText, logging = logging)
}
