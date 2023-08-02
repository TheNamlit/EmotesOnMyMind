package com.thenamlit.emotesonmymind.features.sticker.domain.use_case.sticker_details

import android.util.Log
import com.thenamlit.emotesonmymind.core.domain.models.Sticker
import com.thenamlit.emotesonmymind.core.domain.repository.DeviceStorageRepository
import com.thenamlit.emotesonmymind.core.domain.repository.StickerCollectionRepository
import com.thenamlit.emotesonmymind.core.domain.repository.StickerRepository
import com.thenamlit.emotesonmymind.core.util.Logging
import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import javax.inject.Inject


class DeleteStickerUseCase @Inject constructor(
    private val deviceStorageRepository: DeviceStorageRepository,
    private val stickerRepository: StickerRepository,
    private val stickerCollectionRepository: StickerCollectionRepository,
) {
    private val tag = Logging.loggingPrefix + DeleteStickerUseCase::class.java.simpleName

    suspend operator fun invoke(sticker: Sticker): SimpleResource {
        Log.d(tag, "invoke | sticker: $sticker")

        val deletedFromAllCollectionsResult =
            stickerCollectionRepository.removeStickerFromEveryCollection(
                stickerId = sticker.id,
                animated = sticker.stickerImageData.animated
            )

        when (deletedFromAllCollectionsResult) {
            is Resource.Success -> {
                Log.d(
                    tag,
                    "Successfully deleted Sticker from all Collections, now deleting Sticker itself from DB"
                )

                val removedFromCollections: List<String> =
                    deletedFromAllCollectionsResult.data ?: emptyList()

                when (stickerRepository.deleteStickerById(id = sticker.id)) {
                    is Resource.Success -> {
                        val deleteStickerFileResult = deviceStorageRepository.deleteFile(
                            path = "stickers/${sticker.remoteEmoteData.id}.webp"
                        )

                        return when (deleteStickerFileResult) {
                            is Resource.Success -> {
                                Log.d(tag, "Successfully deleted Sticker-File, finished")

                                Resource.Success(data = null)
                            }

                            is Resource.Error -> {
                                // TODO: Add Sticker back in first
                                //  And then the added Sticker back into Collections
                                addStickerBack(sticker = sticker)

                                addBackToCollections(
                                    removedFromCollections = removedFromCollections,
                                    stickerId = sticker.id  // TODO: Use result from addStickerBack
                                )

                                Resource.Error(
                                    uiText = deleteStickerFileResult.uiText,
                                    logging = "invoke | ${deleteStickerFileResult.logging}"
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        addBackToCollections(
                            removedFromCollections = removedFromCollections,
                            stickerId = sticker.id
                        )

                        return Resource.Error(
                            uiText = deletedFromAllCollectionsResult.uiText,
                            logging = "invoke | ${deletedFromAllCollectionsResult.logging}"
                        )
                    }
                }
            }

            is Resource.Error -> {
                return Resource.Error(
                    uiText = deletedFromAllCollectionsResult.uiText,
                    logging = "invoke | ${deletedFromAllCollectionsResult.logging}"
                )
            }
        }
    }


    // TODO: Implement with SimpleResult as return-value
    private fun addBackToCollections(removedFromCollections: List<String>, stickerId: String) {
        Log.d(
            tag,
            "addBackToCollections | removedFromCollections: $removedFromCollections, " +
                    "stickerId: $stickerId"
        )
    }

    // TODO: Implement with SimpleResult as return-value
    //  Should maybe return new StickerId and then use that ID in addBackToCollections
    private fun addStickerBack(sticker: Sticker) {
        Log.d(tag, "addStickerBack | sticker")
    }
}
