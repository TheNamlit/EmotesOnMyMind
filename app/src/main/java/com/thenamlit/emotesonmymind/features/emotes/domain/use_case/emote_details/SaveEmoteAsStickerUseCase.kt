package com.thenamlit.emotesonmymind.features.emotes.domain.use_case.emote_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetails
import com.thenamlit.emotesonmymind.features.emotes.domain.models.EmoteDetailsHostFile
import com.thenamlit.emotesonmymind.features.emotes.domain.repository.EmoteToStickerRepository
import java.util.UUID
import javax.inject.Inject


class SaveEmoteAsStickerUseCase @Inject constructor(
    private val emoteToStickerRepository: EmoteToStickerRepository,
    private val stickerRepository: StickerRepository,
) {
    private val tag = Logging.loggingPrefix + SaveEmoteAsStickerUseCase::class.java.simpleName

    suspend operator fun invoke(emoteDetails: EmoteDetails): Resource<UUID> {
        Log.d(tag, "invoke | emoteDetails: $emoteDetails")

        when (val stickerAlreadyExistsResult =
            checkIfStickerAlreadyExists(emoteId = emoteDetails.id)) {
            is Resource.Success -> {
                Log.d(tag, "Sticker already exists - Just return and let user know")

                return Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "invoke | ${stickerAlreadyExistsResult.uiText}"
                    )
                )
            }

            is Resource.Error -> {
                Log.d(tag, "invoke | Didn't find Sticker...proceeding to save it")
            }
        }

        val saveImageToInternalStorageResult =
            saveImageToInternalStorage(emoteDetails = emoteDetails)

        return when (saveImageToInternalStorageResult) {
            is Resource.Success -> {
                Log.d(tag, "Successfully saved Image to Internal Storage and to DB")

                saveImageToInternalStorageResult
            }

            is Resource.Error -> {
                Resource.Error(
                    uiText = UiText.DynamicString(
                        value = "invoke | ${saveImageToInternalStorageResult.uiText}"
                    )
                )
            }
        }
    }

    private suspend fun checkIfStickerAlreadyExists(emoteId: String): Resource<Sticker> {
        Log.d(tag, "checkIfStickerAlreadyExists | emoteId: $emoteId")

        return stickerRepository.getStickerByRemoteEmoteId(emoteId = emoteId)
    }

    private suspend fun saveImageToInternalStorage(
        emoteDetails: EmoteDetails,
    ): Resource<UUID> {
        Log.d(tag, "saveImageToInternalStorage | emoteDetails: $emoteDetails")

        val sourceUrl = emoteDetails.host.url
        var sourceFileName = emoteDetails.host.defaultFileName
        var sourceImageToScaleSizeMultiplier = 1.0
        val destinationDirectory = "stickers"
        val destinationFileName = "${emoteDetails.id}.webp"

        // Deciding which Emote to download, because some file sizes are way too big
        // So instead of trying to Downscale & Compress large files, it's easier to just take the
        // most "fitting" Emote and adjust that one :)
        val defaultEmoteSize: Int = emoteDetails.host.files.last().size
        if (emoteDetails.animated) {
            if (defaultEmoteSize > WhatsAppSettings.ANIMATED_STICKER_SIZE) {
                val emoteDetailsAnimatedHostFile = getMatchingEmoteDetailsHostFile(
                    emoteDetails,
                    WhatsAppSettings.ANIMATED_STICKER_SIZE
                )

                sourceFileName = emoteDetailsAnimatedHostFile.name
                val sourceFileSize = emoteDetailsAnimatedHostFile.size
                val sourceFileWidth = emoteDetailsAnimatedHostFile.width
                val sourceFileHeight = emoteDetailsAnimatedHostFile.height
                sourceImageToScaleSizeMultiplier = getSourceImageToScaleSizeMultiplier(
                    fileSize = sourceFileSize,
                    width = sourceFileWidth,
                    height = sourceFileHeight,
                    maxFileSize = WhatsAppSettings.ANIMATED_STICKER_SIZE,
                    maxWidth = WhatsAppSettings.STICKER_WIDTH,
                    maxHeight = WhatsAppSettings.STICKER_HEIGHT
                )
            }

            return emoteToStickerRepository.downloadAnimatedStickerFile(
                sourceUrl = sourceUrl,
                sourceFileName = sourceFileName,
                sourceImageToScaleSizeMultiplier = sourceImageToScaleSizeMultiplier,
                destinationDirectory = destinationDirectory,
                destinationFileName = destinationFileName,
                emoteDetails = emoteDetails
            )
        } else {
            if (defaultEmoteSize > WhatsAppSettings.ANIMATED_STICKER_SIZE) {
                val emoteDetailsNotAnimatedHostFile = getMatchingEmoteDetailsHostFile(
                    emoteDetails,
                    WhatsAppSettings.NOT_ANIMATED_STICKER_SIZE
                )

                sourceFileName = emoteDetailsNotAnimatedHostFile.name
                val sourceFileSize = emoteDetailsNotAnimatedHostFile.size
                val sourceFileWidth = emoteDetailsNotAnimatedHostFile.width
                val sourceFileHeight = emoteDetailsNotAnimatedHostFile.height
                sourceImageToScaleSizeMultiplier = getSourceImageToScaleSizeMultiplier(
                    fileSize = sourceFileSize,
                    width = sourceFileWidth,
                    height = sourceFileHeight,
                    maxFileSize = WhatsAppSettings.ANIMATED_STICKER_SIZE,
                    maxWidth = WhatsAppSettings.STICKER_WIDTH,
                    maxHeight = WhatsAppSettings.STICKER_HEIGHT
                )
            }

            return emoteToStickerRepository.downloadNotAnimatedStickerFile(
                sourceUrl = sourceUrl,
                sourceFileName = sourceFileName,
                sourceImageToScaleSizeMultiplier = sourceImageToScaleSizeMultiplier,
                destinationDirectory = destinationDirectory,
                destinationFileName = destinationFileName,
                emoteDetails = emoteDetails
            )
        }
    }

    private fun getMatchingEmoteDetailsHostFile(
        emoteDetails: EmoteDetails,
        maxFileSize: Long,
    ): EmoteDetailsHostFile {
        Log.d(
            tag,
            "getMatchingEmoteHostFileName | emoteDetails: $emoteDetails, " +
                    "maxFileSize: $maxFileSize"
        )

        // Going in reverse to check the largest first, which is usually preferred
        emoteDetails.host.files.reversed().forEach { emoteDetailsHostFile: EmoteDetailsHostFile ->
            if (emoteDetailsHostFile.size <= maxFileSize) {
                return emoteDetailsHostFile
            }
        }

        // If there is no file that is smaller than the maxFileSize, return the first (smallest) one
        // This should be the best chance for a successful scaling afterwards
        return emoteDetails.host.files.first()
    }

    private fun getSourceImageToScaleSizeMultiplier(
        fileSize: Int,
        width: Int,
        height: Int,
        maxFileSize: Long,
        maxWidth: Int,
        maxHeight: Int,
    ): Double {
        Log.d(
            tag,
            "getSourceImageToScaleSizeMultiplier | fileSize: $fileSize, " +
                    "width: $width, " +
                    "height: $height,  " +
                    "maxFileSize: $maxFileSize, " +
                    "maxWidth: $maxWidth, " +
                    "maxHeight: $maxHeight"
        )

        val fileSizeMultiplier: Double = maxFileSize.toDouble() / fileSize.toDouble()
        val fileSizeMultiplierToWidth: Double = fileSizeMultiplier * width
        val fileSizeMultiplierToHeight: Double = fileSizeMultiplier * height

        val widthMultiplier: Double = 1 / (maxWidth.toDouble() / fileSizeMultiplierToWidth)
        val heightMultiplier: Double = 1 / (maxHeight.toDouble() / fileSizeMultiplierToHeight)

        return if (widthMultiplier > heightMultiplier) {
            widthMultiplier
        } else {
            heightMultiplier
        }
    }
}
