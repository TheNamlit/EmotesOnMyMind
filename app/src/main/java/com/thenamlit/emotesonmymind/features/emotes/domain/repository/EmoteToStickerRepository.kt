package com.thenamlit.emotesonmymind.features.emotes.domain.repository

import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import java.util.UUID


interface EmoteToStickerRepository {
    suspend fun downloadNotAnimatedStickerFile(
        sourceUrl: String,
        sourceFileName: String,
        sourceImageToScaleSizeMultiplier: Double,
        destinationDirectory: String,
        destinationFileName: String,
        emoteDetails: EmoteDetails,
    ): Resource<UUID>

    suspend fun downloadAnimatedStickerFile(
        sourceUrl: String,
        sourceFileName: String,
        sourceImageToScaleSizeMultiplier: Double,
        destinationDirectory: String,
        destinationFileName: String,
        emoteDetails: EmoteDetails,
    ): Resource<UUID>
}
