package com.thenamlit.emotesonmymind.core.domain.models

import kotlinx.serialization.Serializable


@Serializable
data class StickerCollection(
    val id: String = "",
    val name: String,
    val animated: Boolean,
    val stickers: List<Sticker> = emptyList(),
)
