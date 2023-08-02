package com.thenamlit.emotesonmymind.features.emotes.domain.models

import kotlinx.serialization.Serializable


@Serializable
data class MainFeedEmoteSearchHistoryItem(
    val id: String = "",
    val value: String,
)
