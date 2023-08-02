package com.thenamlit.emotesonmymind.features.emotes.domain.models

import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerImageData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteData
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataHostFile
import com.thenamlit.emotesonmymind.core.domain.models.StickerRemoteEmoteDataOwner
import com.thenamlit.emotesonmymind.type.ImageFormat
import kotlinx.serialization.Serializable


@Serializable
data class EmoteDetails(
    val id: String,
    val name: String,
    val createdAt: Long,
    val listed: Boolean,
    val personalUse: Boolean,
    val animated: Boolean,
    val trending: Int?,
    val lifecycle: Int,
    val host: EmoteDetailsHost,
    val owner: EmoteDetailsOwner,
    val tags: List<String> = emptyList(),
) {
    fun toSticker(scaledWidth: Int, scaledHeight: Int, scaledSize: Int): Sticker {
        return Sticker(
            name = name,
            stickerImageData = StickerImageData(
                width = scaledWidth,
                height = scaledHeight,
                size = scaledSize,
                frameCount = host.files.last().frameCount,
                format = host.files.last().format.name,
                animated = animated,
            ),
            remoteEmoteData = StickerRemoteEmoteData(
                id = id,
                url = host.url,
                createdAt = createdAt,
                owner = StickerRemoteEmoteDataOwner(
                    id = owner.id,
                    username = owner.username,
                    avatarUrl = owner.avatarUrl
                ),
                hostFile = StickerRemoteEmoteDataHostFile(
                    width = host.files.last().width,
                    height = host.files.last().height,
                    size = host.files.last().size,
                    frameCount = host.files.last().frameCount,
                    format = host.files.last().format.name,
                    animated = animated,
                )
            )
        )
    }
}

@Serializable
data class EmoteDetailsHost(
    val url: String,
    val defaultFileName: String,
    val files: List<EmoteDetailsHostFile> = emptyList(),
)

@Serializable
data class EmoteDetailsHostFile(
    val name: String,
    val width: Int,
    val height: Int,
    val size: Int,
    val frameCount: Int,
    val format: ImageFormat,
)

@Serializable
data class EmoteDetailsOwner(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
)
