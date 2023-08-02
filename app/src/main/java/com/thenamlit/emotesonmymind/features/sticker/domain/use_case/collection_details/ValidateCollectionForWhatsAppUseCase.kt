package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.collection_details

import android.util.Log
import com.thenamlit.emotesonmymind.R
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.models.StickerCollection
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText
import com.thenamlit.emotesonmymind.core.util.WhatsAppSettings
import javax.inject.Inject


class ValidateCollectionForWhatsAppUseCase @Inject constructor(
) {
    private val tag =
        Logging.loggingPrefix + ValidateCollectionForWhatsAppUseCase::class.java.simpleName

    operator fun invoke(stickerCollection: StickerCollection): List<SimpleResource> {
        Log.d(tag, "invoke | stickerCollection: $stickerCollection")

        // TODO: Build validation with:
        //  - Check if WhatsApp is installed
        //  - Dimension check
        //  - File-Size check
        //  - Collection-Icon found
        //  - More...

        return listOf(
            validateCollectionStickerAmount(stickerAmount = stickerCollection.stickers.size),
            validateFileDimensions(stickers = stickerCollection.stickers),
            validateFileSizes(
                stickers = stickerCollection.stickers,
                animated = stickerCollection.animated
            )
        )
    }

    private fun validateCollectionStickerAmount(stickerAmount: Int): SimpleResource {
        Log.d(tag, "validateCollectionStickerAmount | stickerAmount: $stickerAmount")

        if (stickerAmount !in
            WhatsAppSettings.MINIMUM_STICKER_AMOUNT..WhatsAppSettings.MAXIMUM_STICKER_AMOUNT
        ) {
            return Resource.Error(
                // TODO: Add variable to StringResource?
                uiText = UiText.StringResource(
                    id = R.string.validate_collection_for_whats_app_use_case_validate_sticker_amount_error
                ),
                logging = "validateCollectionStickerAmount | " +
                        "Failed to validate Sticker Amount: $stickerAmount"
            )
        }
        return Resource.Success(data = null)
    }

    private fun validateFileDimensions(stickers: List<Sticker>): SimpleResource {
        Log.d(tag, "validateFileDimensions | stickers: $stickers")

        stickers.forEach { sticker: Sticker ->
            if (sticker.stickerImageData.height != WhatsAppSettings.STICKER_HEIGHT ||
                sticker.stickerImageData.width != WhatsAppSettings.STICKER_WIDTH
            ) {
                return Resource.Error(
                    // TODO: Add variable to StringResource?
                    uiText = UiText.StringResource(
                        id = R.string.validate_collection_for_whats_app_use_case_validate_file_dimensions_error
                    ),
                    logging = "validateFileSizes | " +
                            "Failed to validate File Dimensions of Sticker: $sticker"
                )
            }
        }
        return Resource.Success(data = null)
    }

    private fun validateFileSizes(stickers: List<Sticker>, animated: Boolean): SimpleResource {
        Log.d(tag, "validateFileSizes | stickers: $stickers, animated: $animated")

        val maxSize = if (animated) {
            WhatsAppSettings.ANIMATED_STICKER_SIZE
        } else {
            WhatsAppSettings.NOT_ANIMATED_STICKER_SIZE
        }

        stickers.forEach { sticker: Sticker ->
            if (sticker.stickerImageData.size > maxSize) {
                return Resource.Error(
                    // TODO: Add variable to StringResource?
                    uiText = UiText.StringResource(
                        id = R.string.validate_collection_for_whats_app_use_case_validate_file_sizes_error
                    ),
                    logging = "validateFileSizes | " +
                            "Failed to validate File Size of Sticker: $sticker"
                )
            }
        }
        return Resource.Success(data = null)
    }
}
