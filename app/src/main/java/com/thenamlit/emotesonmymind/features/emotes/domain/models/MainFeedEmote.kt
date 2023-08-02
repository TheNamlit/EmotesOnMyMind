package com.thenamlit.emotesonmymind.features.emotes.domain.models

import kotlinx.serialization.Serializable


@Serializable
data class MainFeedEmoteQueryResult(
    val count: Int,
    val emotes: List<MainFeedEmote> = emptyList(),
)

@Serializable
data class MainFeedEmote(
    val id: String,
    val name: String,
    val createdAt: Long,
    val listed: Boolean,
    val personalUse: Boolean,
    val animated: Boolean,
    val lifecycle: Int,
    // TODO: Add trending?
    val host: MainFeedEmoteItemHost,
)

@Serializable
data class MainFeedEmoteItemHost(
    val url: String,
    val defaultFileName: String,
    val files: List<MainFeedEmoteItemHostFile> = emptyList(),
)

@Serializable
data class MainFeedEmoteItemHostFile(
    val name: String,
    val width: Int,
    val height: Int,
    val size: Int,
    val frameCount: Int,
    val format: String,
)
